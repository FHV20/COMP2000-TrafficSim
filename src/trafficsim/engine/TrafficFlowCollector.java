package trafficsim.engine;

import trafficsim.model.vehicle.Vehicle;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collects a stream of vehicles into a traffic-flow summary in one pass.
 */
final class TrafficFlowCollector
        implements Collector<Vehicle, TrafficFlowCollector.Accumulator, TrafficFlowSummary> {

    private static final double QUEUED_SPEED_THRESHOLD = 0.5;

    @Override
    public Supplier<Accumulator> supplier() {
        return Accumulator::new;
    }

    @Override
    public BiConsumer<Accumulator, Vehicle> accumulator() {
        return Accumulator::add;
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return Accumulator::combine;
    }

    @Override
    public Function<Accumulator, TrafficFlowSummary> finisher() {
        return Accumulator::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    static final class Accumulator {
        private long vehicleCount;
        private long queuedCount;
        private double totalSpeed;
        private double minimumSpeed = Double.POSITIVE_INFINITY;
        private double maximumSpeed = Double.NEGATIVE_INFINITY;

        void add(Vehicle vehicle) {
            double speed = vehicle.getSpeed();
            vehicleCount++;
            if (speed <= QUEUED_SPEED_THRESHOLD) queuedCount++;
            totalSpeed += speed;
            minimumSpeed = Math.min(minimumSpeed, speed);
            maximumSpeed = Math.max(maximumSpeed, speed);
        }

        Accumulator combine(Accumulator other) {
            vehicleCount += other.vehicleCount;
            queuedCount += other.queuedCount;
            totalSpeed += other.totalSpeed;
            minimumSpeed = Math.min(minimumSpeed, other.minimumSpeed);
            maximumSpeed = Math.max(maximumSpeed, other.maximumSpeed);
            return this;
        }

        TrafficFlowSummary finish() {
            if (vehicleCount == 0) return TrafficFlowSummary.empty();
            return new TrafficFlowSummary(
                    vehicleCount,
                    queuedCount,
                    vehicleCount - queuedCount,
                    totalSpeed / vehicleCount,
                    minimumSpeed,
                    maximumSpeed);
        }
    }
}
