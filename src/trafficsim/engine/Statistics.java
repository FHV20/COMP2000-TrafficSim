package trafficsim.engine;

import trafficsim.model.vehicle.Vehicle;
import trafficsim.util.Direction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Statistics {

    private Statistics() {}

    public static double averageSpeed(List<Vehicle> vehicles) {
        return vehicles.stream()
                .mapToDouble(Vehicle::getSpeed)
                .average()
                .orElse(0.0);
    }

    public static long stoppedCount(List<Vehicle> vehicles) {
        return vehicles.stream()
                .filter(v -> v.getSpeed() == 0.0)
                .count();
    }

    /** Returns traffic totals for every direction. */
    public static Map<Direction, TrafficFlowSummary> flowByDirection(
            List<Vehicle> vehicles) {
        Objects.requireNonNull(vehicles, "Vehicle list cannot be null");

        EnumMap<Direction, TrafficFlowSummary> summaries = vehicles.stream()
                .collect(Collectors.groupingBy(
                        Vehicle::getDirection,
                        () -> new EnumMap<>(Direction.class),
                        new TrafficFlowCollector()));

        for (Direction direction : Direction.values()) {
            summaries.putIfAbsent(direction, TrafficFlowSummary.empty());
        }
        return Collections.unmodifiableMap(summaries);
    }
}
