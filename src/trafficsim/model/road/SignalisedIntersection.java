package trafficsim.model.road;

import trafficsim.SimConstants;
import trafficsim.util.Axis;
import trafficsim.util.LightPhase;
import trafficsim.model.light.TrafficLight;
import trafficsim.model.vehicle.Vehicle;

/** Intersection with adaptive traffic lights. */
public final class SignalisedIntersection extends Intersection {

    private final TrafficLight light;
    private int extensionsThisPhase = 0;

    public SignalisedIntersection(int x, int y, TrafficLight light) {
        super(x, y);
        this.light = light;
    }

    @Override
    public void update() {
        light.update();
        maybeExtendGreen();
    }

    @Override
    public TrafficLight getLight() {
        return light;
    }

    private void maybeExtendGreen() {
        if (light.getPhase() != LightPhase.GREEN) {
            extensionsThisPhase = 0;
            return;
        }
        int remaining = light.getGreenDuration() - light.getTimer();
        if (remaining > 4) return;
        if (extensionsThisPhase >= SimConstants.ADAPTIVE_MAX_EXTENSIONS) return;
        int queued = queuedOnGreenAxis();
        if (queued < SimConstants.ADAPTIVE_QUEUE_HIGH) return;
        light.extendCurrentPhase(SimConstants.ADAPTIVE_EXTEND_TICKS);
        extensionsThisPhase++;
    }

    private int queuedOnGreenAxis() {
        Axis green = light.getGreenAxis();
        int count = 0;
        for (Road road : connectedRoads) {
            for (Lane lane : road.getLanes()) {
                if (Axis.of(lane.getDirection()) != green) continue;
                for (Vehicle v : lane.getVehicles()) {
                    if (v.getSpeed() > SimConstants.QUEUED_SPEED_THRESHOLD) continue;
                    double dx = v.getX() - x, dy = v.getY() - y;
                    if (Math.hypot(dx, dy) < 180) count++;
                }
            }
        }
        return count;
    }
}
