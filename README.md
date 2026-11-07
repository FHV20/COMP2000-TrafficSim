# COMP2000 Traffic Simulation

A Java Swing traffic simulator by Henry and Jacob. Cars, trucks, buses and emergency vehicles share a grid of signalised roads. Queues form as vehicles wait for lights, leave space for the vehicle ahead or stop behind a bus.

## Run in VS Code

1. Open the repository folder in VS Code. Use a JDK 17 or newer and the Extension Pack for Java.
2. Open [Main.java](src/trafficsim/Main.java) and click **Run** above the `main` method.
3. Press **Space** in the simulation window to pause or resume.

With JDK 22 or newer, you can also run this from the repository folder:

```sh
java src/trafficsim/Main.java
```

The dashboard displays the seed in hexadecimal. To repeat a run with a known decimal seed, use `java src/trafficsim/Main.java --seed=42`. The default road layout is in [grid.txt](networks/grid.txt).

## Reading the dashboard

Each direction shows total, queued and moving vehicles, followed by average, minimum and maximum speed in world units per tick. A vehicle counts as queued at 0.5 or below. The city queue adds the four directional counts. [TrafficFlowCollector](src/trafficsim/engine/TrafficFlowCollector.java) calculates the figures for each direction and returns a [TrafficFlowSummary](src/trafficsim/engine/TrafficFlowSummary.java).

## Design patterns

`TrafficLight` delegates each phase to a `LightState`. At the end of the all-red phase, `RedState` switches the active axis before the next green. `SimulationEngine` notifies its observers after each tick, and `SimulationDisplay` requests a repaint. [State and Observer diagram](COMP2000_Pattern_Implementation_Diagram.pdf)

## AI Use Acknowledgement

Generative AI tools were used to help review, explain, debug, and refine parts of the code and documentation. The submitted work was checked by the student, who remains responsible for understanding and explaining it.
