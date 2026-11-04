package trafficsim.model.vehicle;

import trafficsim.engine.SensorReading;
import trafficsim.model.road.BusStop;
import trafficsim.util.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class Bus extends Vehicle {

    private final List<BusStop> stops = new ArrayList<>();
    private final Set<BusStop> servedStops =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static final double STOP_RADIUS = 8.0;
    private static final int DWELL_TICKS = 30;
    private int dwellTicksRemaining;

    public Bus(double x, double y, Direction direction) {
        super(x, y, 2.5, 32.0, direction);
    }

    @Override
    public void move(SensorReading r) {
        if (dwellTicksRemaining > 0) {
            dwellTicksRemaining--;
            brake();
            return;
        }

        for (BusStop stop : stops) {
            if (servedStops.contains(stop)) continue;
            int[] p = stop.getPosition();
            if (Math.hypot(p[0] - x, p[1] - y) < STOP_RADIUS) {
                servedStops.add(stop);
                dwellTicksRemaining = DWELL_TICKS - 1;
                brake();
                return;
            }
        }
        super.move(r);
    }

    public void addStop(BusStop stop) {
        if (stop != null && !stops.contains(stop)) stops.add(stop);
    }
}
