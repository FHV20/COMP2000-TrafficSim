package trafficsim.engine;

/**
 * A snapshot of the traffic travelling in one direction.
 */
public record TrafficFlowSummary(
        long totalVehicles,
        long queuedVehicles,
        long movingVehicles,
        double averageSpeed,
        double minimumSpeed,
        double maximumSpeed) {

    public static TrafficFlowSummary empty() {
        return new TrafficFlowSummary(0, 0, 0, 0.0, 0.0, 0.0);
    }
}
