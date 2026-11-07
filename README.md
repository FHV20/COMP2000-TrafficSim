# COMP2000 Traffic Simulation

A Java Swing simulation of traffic moving through a three-by-three signalised road grid. Cars, trucks, buses and emergency vehicles follow the same road network while reacting to traffic lights, nearby vehicles, occupied intersections and bus stops. These rules produce queues that build and clear while the program is running.

## Team

- Henry
- Jacob

## Run the simulation

### VS Code

1. Open the repository folder in VS Code with JDK 17 or newer and the Extension Pack for Java.
2. Open [Main.java](src/trafficsim/Main.java).
3. Click **Run** above the `main` method.

Press **Space** in the simulation window to pause or resume.

### Terminal

With JDK 22 or newer, run this from the repository folder:

```sh
java src/trafficsim/Main.java
```

The program loads [grid.txt](networks/grid.txt) by default. You can supply another network file as the first argument. The dashboard displays each run's seed in hexadecimal. To repeat a run using a known decimal seed, add `--seed=<number>`:

```sh
java src/trafficsim/Main.java --seed=42
```

## Main features

- Cars, trucks, buses and emergency vehicles with different speeds and behaviour
- Same-lane spacing and intersection collision avoidance
- Traffic lights with adaptive green extensions
- Emergency priority while occupied crossings remain protected
- Directional bus stops and dwell times
- Random traffic with repeatable seeded runs
- A live dashboard showing traffic totals, queues and speed statistics

## Dashboard

The city panel shows the simulation state, tick, vehicle count, average speed, queued vehicles and seed. The directional table shows total, queued and moving vehicles for north, south, east and west, together with average, minimum and maximum speed.

A vehicle counts as queued when its speed is 0.5 world units per tick or lower. [TrafficFlowCollector](src/trafficsim/engine/TrafficFlowCollector.java) calculates each directional result and returns an immutable [TrafficFlowSummary](src/trafficsim/engine/TrafficFlowSummary.java). The city queue is the sum of the four directional queues.

## Object-oriented design

- **Inheritance:** `Vehicle` contains shared movement behaviour for `Car`, `Truck`, `Bus` and `EmergencyVehicle`.
- **Generics:** `WeightedRandom<T>` selects vehicle constructors and driver profiles by weight.
- **Exceptions:** `InvalidNetworkException` reports malformed or unreadable network files.
- **State:** `TrafficLight` delegates phase transitions to `GreenState`, `YellowState` and `RedState` through `LightState`.
- **Observer:** `SimulationEngine` notifies `SimulationObserver` after a completed tick, and `SimulationDisplay` requests a repaint.
- **Streams and collectors:** `Statistics` groups vehicles by direction and uses `TrafficFlowCollector` to produce the dashboard summaries.
- **Threading:** the simulation runs on a background thread while Swing paints on its event thread. Both use the road-network lock when accessing live model state.

The [State and Observer diagram](COMP2000_Pattern_Implementation_Diagram.pdf) shows the participants and their run-time relationships.

## Project structure

```text
src/trafficsim/
  Main.java                  Entry point and Swing window
  SimConstants.java          Shared simulation settings
  engine/                    Simulation loop, sensors, spawning and statistics
  factory/                   Network loading
  model/
    light/                   Traffic-light State pattern
    road/                    Roads, lanes, intersections and bus stops
    vehicle/                 Vehicle hierarchy and driver profiles
  strategy/                  Generic weighted selection
  util/                      Direction, axis and light-phase enums
  view/                      Swing renderer, dashboard and keyboard control
networks/
  grid.txt                   Default road network
```

## Submission documents

- [Worksheet 2](COMP2000_Worksheet_2_Final.md)
- [Log book, Weeks 1–13](COMP2000_Log_Book_Weeks_1-13.md)
- [State and Observer pattern diagram](COMP2000_Pattern_Implementation_Diagram.pdf)

## AI use acknowledgement

Generative AI tools were used to help review, explain, debug and refine parts of the code and documentation. The submitted work was checked by the student, who remains responsible for understanding and explaining it.
