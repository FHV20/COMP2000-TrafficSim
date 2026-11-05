package trafficsim.engine;

import trafficsim.SimConstants;
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

        void add(Vehicle vehicle) {
            double speed = vehicle.getSpeed();
            vehicleCount++;
            if (speed <= SimConstants.QUEUED_SPEED_THRESHOLD) queuedCount++;
            totalSpeed += speed;
        }

        Accumulator combine(Accumulator other) {
            vehicleCount += other.vehicleCount;
            queuedCount += other.queuedCount;
            totalSpeed += other.totalSpeed;
            return this;
        }

        TrafficFlowSummary finish() {
            if (vehicleCount == 0) return TrafficFlowSummary.empty();
            return new TrafficFlowSummary(
                    vehicleCount,
                    queuedCount,
                    totalSpeed / vehicleCount);
        }
    }
}
