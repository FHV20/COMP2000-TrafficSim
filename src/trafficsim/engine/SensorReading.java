package trafficsim.engine;

/** Vehicle sensor data for one tick. */
public record SensorReading(
        double distToVehicleAhead,
        double distToRedStop) {

    public static SensorReading clear() {
        return new SensorReading(
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }

    public double effectiveStopDistance() {
        return Math.min(distToVehicleAhead, distToRedStop);
    }
}
