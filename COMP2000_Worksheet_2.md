# COMP2000 Worksheet 2

**Student name:** Iakov Dergachev

**GitHub repo URL:** https://github.com/Jacob5511/COMP2000-TrafficSim

---

## 1. Design Patterns

The pattern relationships are shown below. The [pattern diagram in my fork](https://github.com/Jacob5511/COMP2000-TrafficSim/blob/main/COMP2000_Pattern_Implementation_Diagram.pdf) shows how each pattern works at run time.

```text
TrafficLight  -->  LightState  <..  GreenState, YellowState, RedState
SimulationEngine  -->  SimulationObserver  <..  SimulationDisplay

-->  holds a reference and calls the interface
<..  the classes on the right implement the interface
```

### 1.1 Which design patterns did you implement?

I implemented the **State** and **Observer** patterns.

- **State:** `src/trafficsim/model/light/LightState.java` defines the state interface. `GreenState`, `YellowState` and `RedState` are the concrete states, while `TrafficLight` is the context that stores the current state and delegates phase behaviour to it.
- **Observer:** `src/trafficsim/engine/SimulationObserver.java` defines the observer interface. `SimulationEngine` is the subject that stores and notifies observers. `SimulationDisplay` is the concrete observer that requests a Swing repaint after each completed simulation tick.

### 1.2 Code showing the key pattern interactions

#### State

```java
// TrafficLight - context
public void update() {
    timer++;
    state.update(this);
}

void setState(LightState next) {
    this.state = next;
    this.timer = 0;
}

// GreenState - concrete state
public void update(TrafficLight light) {
    if (light.getTimer() >= light.getGreenDuration()) {
        light.setState(new YellowState());
    }
}
```

`TrafficLight` delegates the transition decision to its current `LightState`. The concrete state changes the context to the next state when its duration expires.

#### Observer

```java
// SimulationEngine - subject
public void addObserver(SimulationObserver observer) {
    observers.add(observer);
}

private void notifyObservers() {
    for (SimulationObserver observer : observers) {
        observer.onSimulationStep();
    }
}

// SimulationDisplay - concrete observer
@Override
public void onSimulationStep() {
    repaint();
}
```

The engine completes the locked model update before calling `notifyObservers()`. The display receives the event through the interface and asks Swing to repaint the completed state.

### 1.3 Why these patterns were chosen and their trade-offs

I chose **State** instead of keeping a phase enum and a large `switch` inside `TrafficLight`. Each phase has different transition behaviour, so keeping the rule inside the corresponding state class makes the cycle easier to extend and test. I also considered a Strategy-style design, but State is a better match because the active object changes over time and each state controls the transition to the next one. The trade-off is three extra small classes for a cycle that could initially be written as one conditional method.

I chose **Observer** instead of letting `SimulationEngine` call `SimulationDisplay` directly. The engine therefore depends only on `SimulationObserver`, not on Swing. A logger, recorder or test observer could be added without changing the engine. The trade-off is that the event flow is less obvious than a direct method call, and observers must respect the engine/UI thread boundary. In this implementation, the model update finishes and releases the network lock before observers are notified.

---

## 2. Streams / Lambdas / Parallelism

### 2.1 Files that use lambda expressions or the Streams API

- `src/trafficsim/Main.java` - lambda passed to `SwingUtilities.invokeLater`.
- `src/trafficsim/engine/SimulationEngine.java` - lambda used as the background simulation thread body.
- `src/trafficsim/engine/Statistics.java` - stream pipelines, method references, `groupingBy`, an `EnumMap` supplier and the custom downstream collector.
- `src/trafficsim/engine/TrafficFlowCollector.java` - custom `Collector` implementation using method references for the supplier, accumulator, combiner and finisher.
- `src/trafficsim/engine/VehicleSpawner.java` - vehicle-constructor lambdas stored in `WeightedRandom<Function<SpawnContext, Vehicle>>`, plus a predicate lambda for removing off-map vehicles.
- `src/trafficsim/view/SimulationDisplay.java` - sums the four queue counts using `mapToLong(TrafficFlowSummary::queuedVehicles)`.
- `src/trafficsim/factory/NetworkLoader.java` - `Arrays.stream`, mapping, `flatMap`, method references and a matching predicate.

The arrows in Java switch expressions are not lambdas and are not included in this list.

### 2.2 Most sophisticated use

```java
EnumMap<Direction, TrafficFlowSummary> summaries = vehicles.stream()
        .collect(Collectors.groupingBy(
                Vehicle::getDirection,
                () -> new EnumMap<>(Direction.class),
                new TrafficFlowCollector()));
```

`TrafficFlowCollector` implements:

```java
Collector<Vehicle, TrafficFlowCollector.Accumulator, TrafficFlowSummary>
```

Its supplier creates an empty accumulator, its accumulator reads one vehicle, its combiner merges partial accumulators, and its finisher creates an immutable `TrafficFlowSummary`. In one traversal it calculates total, queued and moving vehicle counts plus average, minimum and maximum speed. `groupingBy` applies that collector separately to NORTH, SOUTH, EAST and WEST.

This is better than a manual loop because the grouping rule and the summary rule remain separate and reusable. A loop would need manual map creation, missing-direction checks, counters, totals and minimum/maximum handling in one method. The stream version states the operation directly: group by direction, then summarise each group. The collector counts a vehicle as queued at a speed of 0.5 or below. The city queue sums the four directional queue counts, so a vehicle travelling at exactly 0.5 counts once in its direction and once in the city total. Empty directions are filled with `TrafficFlowSummary.empty()` and the returned map is unmodifiable, so the display has a complete and safe snapshot.

### 2.3 Parallelism and thread safety

I used a **custom collector**, but deliberately did not call `parallelStream()` or use Fork/Join for vehicle movement. Vehicles share mutable lanes and intersections, and the result of one movement can affect the safe movement of another. Parallel mutation could create race conditions, change collision outcomes and make seeded runs nondeterministic.

The custom collector is safe because each collection uses its own accumulator and the combiner merges separate partial accumulators; it does not modify vehicles or shared global state. In the current display, statistics are calculated from a local vehicle list while `paintComponent` holds the same `RoadNetwork` lock used by the simulation tick.

The program still uses concurrency appropriately: `SimulationEngine` runs on a background `sim-loop` thread while Swing paints on the Event Dispatch Thread. Both model updates and painting synchronise on the same network object, so the display sees either the previous completed tick or the next completed tick, never a half-updated model.

If further parallelism were justified by profiling, I would first create an immutable vehicle snapshot and parallelise read-only analytics on that snapshot. I would not parallelise movement without redesigning the update model.

---

## 3. Uniqueness and Creativity

### 3.1 Features beyond the in-class activities

My individual extension was the **directional traffic-flow dashboard**. I added `TrafficFlowCollector`, the immutable `TrafficFlowSummary` record and `Statistics.flowByDirection`. I then connected the results to `SimulationDisplay`, where NORTH, SOUTH, EAST and WEST each show total, queued and moving vehicles, plus average, minimum and maximum speed. The city queue is the sum of those four rows.

The feature was inspired by the Week 11 collector activity. I wanted the collector to affect the running simulation instead of existing only as unused assessment code. The simulation also supports seeded runs, weighted vehicle and driver types, buses with directional stops, emergency priority, adaptive signals and safe intersection behaviour.

### 3.2 Emergent behaviour and non-trivial complexity

There is no class that directly creates a traffic jam. Congestion emerges from the interaction of random arrivals, driver profiles, speed limits, safe gaps, signal phases, occupied intersections, bus dwell times, emergency priority and adaptive green extensions.

The tick order is important:

```java
synchronized (network) {
    tickCount++;
    updateLights();
    List<Vehicle> vehicles = collectVehicles();
    Map<Vehicle, SensorReading> readings = readSensors(vehicles);
    moveLaneVehicles(readings);
    spawner.despawnOffMap(network);
    if (vehicles.size() < SimConstants.SPAWN_CAP) spawner.tick(network);
}
notifyObservers();
```

Every sensor reading is calculated before any vehicle moves. A following car therefore reads the gap before the car ahead moves, even if the engine moves the leader first. Movement still runs in a fixed order, and vehicles draw slowdown values from a shared seeded random generator. Changing that order could change which vehicle gets each value. Multiple simple local rules then combine into queues that build and clear visibly across the road network.

### 3.3 What I would add with another six weeks

I would add automated tests for state transitions, collision distances, emergency priority, bus-stop dwelling and deterministic seeded spawning. I would then add route choice and safe turning or lane-changing behaviour. For analytics, I would retain time-series flow data and graph queue lengths over time. Only after profiling would I consider parallel read-only analysis using immutable snapshots.

---

## 4. Log Book and Final Reflection

### 4.1 Log book

[My combined log book](COMP2000_Log_Book_Weeks_1-13.md) includes the requested Weeks 7–13. The Week 7 and Week 13 entries cover the showcases. The final reflection follows Week 13.

### 4.2 Semester reflection

**Most important concept learned.** I understood why we split the traffic light into classes when I followed one change from red to green. `RedState` decides when the all-red wait is over. `TrafficLight` keeps track of which axis gets the next green. I can find the rule for that transition in one place.

**Design trade-off.** Cars share lanes and intersections, so the order of a tick matters. The engine reads every car's sensors before it moves any of them. I kept that work on one thread and used the same network lock for updating and drawing. That way the display can show a completed tick even when several cars are approaching the same junction.

**What I would do differently.** I would test one intersection before trying the whole grid. For example, I'd put an emergency vehicle at a red light with another vehicle still in the crossing and check that it waits. Watching traffic on the full map made that particular rule harder to check.

**Week 7 feedback I acted on.** In my Week 7 showcase notes I wrote that I wanted to introduce the classes through a familiar example. I used that idea when preparing the final demo. I started with what a driver sees when a queue forms at a red light, then explained how the engine reads the gaps and moves the cars.
