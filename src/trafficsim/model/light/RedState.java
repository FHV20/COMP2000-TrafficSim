package trafficsim.model.light;

import trafficsim.util.LightPhase;

/** All-red traffic-light phase. */
public class RedState implements LightState {
    @Override
    public void update(TrafficLight light) {
        if (light.getTimer() >= light.getRedDuration()) {
            light.swapAxis();
            light.setState(new GreenState());
        }
    }

    @Override
    public LightPhase getPhase() {
        return LightPhase.RED;
    }
}
