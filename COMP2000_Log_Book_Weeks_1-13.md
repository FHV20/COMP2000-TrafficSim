# COMP2000 Log Book - Weeks 1-13

**Student:** Iakov Dergachev

**Completion note:** The Weeks 8-13 entries were completed using my project history, the supplied weekly activities and the showcase projects I remembered seeing in class.

## Week 1 - Team Based Learning

**Team name:** No formal team name was recorded.

**Team members:** Ali, Henry, Oscar, Callum, Jubril, Ben, Jacob, Addrita and Cam.

Our team agreed to attend class on time, complete assigned work, respect each other's ideas, communicate through Discord, report absences or delays, and help each other when needed.

We formed the team, introduced ourselves and created the team contract. I had no major concerns and expected Team Based Learning to improve our communication and teamwork.

**Teacher sign-off:** Minh

## Week 2 - Git

I cloned the shared GitHub repository and practised `git add`, `git commit`, `git log`, `git fetch`, `git pull` and `git push`. During the EPIC activity, commits acted as chat messages and empty commits allowed messages without file changes.

At first I ran Git outside the cloned repository, but changing to the correct directory fixed the error. This helped me understand Git history and team collaboration.

**Teacher sign-off:** Minh

## Week 3 - Objects and Classes

Our team chose a traffic simulator. We planned classes including `Vehicle`, `Car`, `Road`, `TrafficLight`, `Intersection` and the main simulation. Each class would hold its own state and responsibility, while the interface would show vehicles, queues and traffic lights.

**Teacher sign-off:** Minh

## Week 4 - Inheritance and Overloading

We reviewed the design and planned a simple update loop for vehicles and traffic lights. We decided that shared vehicle position, speed and movement should belong to a `Vehicle` superclass, with specialised subclasses such as `Car`, `Truck`, `Bus` and `EmergencyVehicle`.

The main uncertainty was how much traffic behaviour should be shared by `Vehicle` and how much should be overridden by subclasses.

**Teacher sign-off:** Minh

## Week 5 - Generics

I experimented with raw types and generics. Assigning `Container<String>` to a raw `Container` allowed an `Object` to be inserted with only an unchecked warning, but retrieving it as a `String` caused a `ClassCastException`.

This showed that generics provide compile-time type safety but type information is erased at runtime. In our project, `WeightedRandom<T>` lets the same weighted-selection logic safely work with vehicle constructors and driver profiles.

**Teacher sign-off:** Minh

## Week 6 - Exceptions

We traced how an exception moves through a chain of method calls and how the location of `catch` changes which methods finish. I learned that `finally` runs during stack unwinding even when normal method execution is interrupted.

In the traffic project, `InvalidNetworkException` reports malformed or unreadable network files. This gives configuration errors a clear project-specific meaning instead of allowing unrelated parsing errors to escape without context.

**Teacher sign-off:** Minh

## Week 7 - Mid-Semester Showcase

The simulation now compiles and runs with a Swing display. Its object model grew from the first sketch into separate road, vehicle, light, sensor, spawning and display responsibilities. The vehicle inheritance hierarchy worked well, while collision avoidance required more independent problem-solving than expected.

What went well was the working graphical simulation, vehicle variety, signalised intersections, buses, random spawning and safe straight-through traffic. I would improve the accuracy of the UML earlier whenever the code changes.

**Presentation notes:**

- Presentation 1: The first group presented a bushfire simulation, showing how object-oriented programming can represent a changing real-world situation.
- Presentation 2: The second group presented a football-themed project and used a familiar scenario to make the program easier to understand.
- Technique I would like to try: Introduce the main classes through a familiar real-world example before demonstrating the program.

**Teacher sign-off:** Minh

## Week 8 - Design Patterns

I reviewed the State, Observer, Decorator and Iterator patterns and then traced the patterns already present in the traffic simulator. The clearest examples were State and Observer.

`TrafficLight` is the context in the State pattern. It delegates phase-specific behaviour to `RedState`, `GreenState` and `YellowState` through the `LightState` interface. This is preferable to a growing conditional inside `TrafficLight` because each transition is kept with the state that owns it. The trade-off is more classes for a small three-state cycle.

The simulation also uses Observer: `SimulationEngine` notifies `SimulationObserver` objects after a complete tick, and `SimulationDisplay` reacts by repainting. Directly calling the Swing display from the engine would be simpler initially, but it would couple simulation logic to one user interface and make future observers harder to add.

One useful conclusion from this topic was that a design pattern should solve a real design problem rather than being added simply to create more classes. In this project, State is useful because traffic-light phases genuinely have different transition behaviour, while Observer is useful because the simulation engine should not depend directly on the Swing display.

**Teacher sign-off:** Minh

## Week 9 - Behaviour Parameterisation and Lambdas

I examined how behaviour can be passed as data instead of selected through repeated conditionals or a large set of small classes. In `VehicleSpawner`, `WeightedRandom<Function<SpawnContext, Vehicle>>` stores lambdas that construct cars, trucks, buses and emergency vehicles. This lets the spawner select a vehicle constructor by weight without a long type-selection chain.

I also identified method references and predicates in the project. Examples include `Vehicle::getDirection`, `Accumulator::add` and the predicate passed to `Lane.removeVehiclesIf`. Lambdas are useful where the behaviour is short and local. A named class is still clearer when behaviour has substantial state, several methods or a domain identity that needs explanation.

A simple lambda technique I found useful was replacing a lambda that only calls one existing method with a method reference. For example, `vehicle -> vehicle.getDirection()` can become `Vehicle::getDirection`. It does not change the behaviour, but it makes a short stream pipeline easier to read.

**Teacher sign-off:** Minh

## Week 10 - Streams

I reviewed stream pipelines as a declarative alternative to loops. The project uses streams in `Statistics` to calculate average speed and stopped vehicles, and in `NetworkLoader` to parse directions and search lanes. These operations have a clear source, transformation and terminal result, which makes them easier to read as streams than as manual accumulation loops.

I learned that streams are not automatically better. The simulation tick still uses explicit loops because the order of updates and shared mutable state are important. Replacing those loops with streams would hide sequencing and make the traffic logic harder to reason about.

An interesting general stream pattern was **filter, map, then reduce**. A program can first keep only the objects it needs, transform them into values such as speeds, and then calculate a result such as an average. The order makes the intention clear and avoids manually managing counters and temporary totals.

**Teacher sign-off:** Minh

## Week 11 - Collectors

I added directional traffic-flow statistics to give the collector activity a meaningful simulation feature. `Statistics.flowByDirection` groups vehicles into an `EnumMap` using `Collectors.groupingBy`. A custom `TrafficFlowCollector` then calculates the total, queued count, moving count, average speed, minimum speed and maximum speed for each direction in one pass.

The result is stored in the immutable `TrafficFlowSummary` record. Empty directions are also included with zero values, so display code does not need special null checks. I later connected these summaries to the dashboard so the collector has a visible purpose rather than existing only as unused rubric code.

Writing the custom collector helped me understand the roles of the supplier, accumulator, combiner and finisher. It was more code than several built-in collectors, but it kept the simulation-specific aggregation in one reusable unit and gave the dashboard counts and speeds directly. A list of vehicles per direction would still need another pass to calculate those values.

A useful collector pattern was combining `groupingBy` with a downstream collector. The grouping operation decides where each item belongs, while the downstream collector decides what summary to calculate for each group. This is the same idea used by the traffic dashboard: vehicles are grouped by direction and each group is converted into one flow summary.

**Teacher sign-off:** Minh

## Week 12 - Parallelism and Thread Safety

I examined where concurrency already exists in the simulation. The engine advances the model on a background thread while Swing paints on its Event Dispatch Thread. The engine performs a complete tick inside `synchronized (network)`, and the display uses the same network lock while collecting and drawing the vehicles. The list contains references to live vehicles, so the display holds the lock until it finishes drawing the roads, vehicles and dashboard. This prevents the interface from seeing a half-updated network.

I decided not to convert vehicle movement to a parallel stream. Vehicles interact through shared lanes, intersections and collision rules, so parallel mutation could introduce race conditions and nondeterministic behaviour. The current simulation is small enough that safe, atomic updates are more valuable than speculative parallel speed-up. A future version could first create immutable snapshots and parallelise read-only statistics, then measure whether the additional complexity produces a real benefit.

A clear thread-safety example is a check-then-act race condition. Two threads could both check that a lane has free space and then both add a vehicle before either sees the other's change. Protecting the entire check-and-add operation with the same lock prevents that problem; locking only the individual read or write would not be enough.

**Teacher sign-off:** Minh

## Week 13 - Final Showcase and Submission

During finalisation I checked that the simulation compiles using Java 17-compatible standard JRE classes and runs as a graphical Swing application. I also connected the Week 11 directional collector to the existing dashboard. The dashboard initially showed totals, queues and average speed for each direction. Before submission the display was updated to show moving counts and minimum and maximum speeds as well. The city queue now sums the four directional queues.

The final simulation demonstrates several interacting behaviours: vehicles maintain same-lane gaps, traffic queues build and clear around lights, adaptive signals may extend green for demand, emergency vehicles receive priority while still respecting occupied crossings, and buses stop only at stops serving their direction. Random spawning creates different traffic patterns, while a supplied seed makes a run reproducible for explanation and testing.

**Teacher sign-off:** Minh

### Showcase observations

- **Bushfire simulation:** This was a strong example of emergent behaviour. Simple local rules for spreading, changing conditions and affected areas could combine into a larger simulation that changed over time.
- **Football game:** The familiar theme made the object-oriented structure easier to follow. Connecting classes and interactions to players and a game helped explain the program without beginning with technical details.
- **Car simulations:** These projects showed that movement, timing and state changes are much easier to understand when they are visible on screen. They were especially relevant to our own traffic simulation.
- **Technique I would like to use again:** Begin with a familiar real-world explanation, demonstrate the program, and then connect the visible behaviour back to the important classes and design decisions.

## Final Reflection

Following a light through one full cycle helped me understand the State pattern. The light has to stay red briefly in every direction before the other road gets a green. `RedState` decides when that wait is over. `TrafficLight` remembers which axis to switch to. That example made it easier for me to explain why the two classes have different jobs.

The hardest decision was how to update cars that depend on each other. If the car in front moves first and the one behind reads the gap afterwards, they are reacting to different moments. The engine reads all the sensors first, then moves the cars. It also holds the network lock until the tick is finished, so Swing gets a consistent view when it paints.

If I started again, I'd test one junction before running the whole grid. An emergency vehicle can pass a red light, but it still has to wait for a car already in the crossing. I'd set up that case directly so I could check the rule without waiting for it to happen by chance on screen.

At the Week 7 showcase I wrote down an idea for our presentation: start with something the audience already understands. I used a traffic queue as the starting point for the final demo. Once the audience knows what makes the second car wait, the reason for reading every car's sensors before moving any of them is easier to explain.
