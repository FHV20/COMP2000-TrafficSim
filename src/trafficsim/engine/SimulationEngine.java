package trafficsim.engine;

import trafficsim.SimConstants;
import trafficsim.model.road.Intersection;
import trafficsim.model.road.Lane;
import trafficsim.model.road.Road;
import trafficsim.model.road.RoadNetwork;
import trafficsim.model.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

/** Runs the traffic simulation. */
public class SimulationEngine {

    private final RoadNetwork network;
    private final List<SimulationObserver> observers = new ArrayList<>();
    private final VehicleSpawner spawner;
    private final long runSeed;

    private final int tickRate;
    private volatile boolean running;
    private volatile boolean paused;
    private long tickCount;

    public SimulationEngine(
            RoadNetwork network, int tickRate, VehicleSpawner spawner, long runSeed) {
        this.network = network;
        this.tickRate = tickRate;
        this.spawner = spawner;
        this.runSeed = runSeed;
    }

    public void addObserver(SimulationObserver observer) { observers.add(observer); }
    private void notifyObservers() { for (SimulationObserver o : observers) o.onSimulationStep(); }

    public void step() {
        if (paused) return;
        doStep();
    }

    private void doStep() {
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
    }

    private void updateLights() {
        for (Intersection intersection : network.getIntersections()) {
            intersection.update();
        }
    }

    private Map<Vehicle, SensorReading> readSensors(List<Vehicle> vehicles) {
        Map<Vehicle, SensorReading> readings = new LinkedHashMap<>();
        for (Vehicle vehicle : vehicles) {
            readings.put(vehicle, TrafficSensor.read(vehicle, network));
        }
        return readings;
    }

    private void moveLaneVehicles(Map<Vehicle, SensorReading> readings) {
        for (Map.Entry<Vehicle, SensorReading> entry : readings.entrySet()) {
            entry.getKey().move(entry.getValue());
        }
    }

    private List<Vehicle> collectVehicles() {
        List<Vehicle> all = new ArrayList<>();
        for (Road road : network.getRoads())
            for (Lane lane : road.getLanes())
                all.addAll(lane.getVehicles());
        return all;
    }

    public void run() {
        running = true;
        Thread t = new Thread(() -> {
            long nextTick = System.nanoTime();
            while (running) {
                long tickNanos = 1_000_000_000L / Math.max(1, tickRate);
                step();
                nextTick += tickNanos;
                long waitNanos = nextTick - System.nanoTime();
                if (waitNanos > 0) LockSupport.parkNanos(waitNanos);
                else nextTick = System.nanoTime();
            }
        }, "sim-loop");
        t.setDaemon(true);
        t.start();
    }

    public void shutdown() {
        running = false;
    }

    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused() { return paused; }
    public long getTickCount() { return tickCount; }
    public long getRunSeed() { return runSeed; }
    public RoadNetwork getNetwork() { return network; }
}
