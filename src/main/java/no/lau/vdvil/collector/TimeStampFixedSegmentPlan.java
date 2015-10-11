package no.lau.vdvil.collector;

import no.lau.vdvil.plan.Plan;

import java.util.List;

/**
 * Created by stiglau on 10/10/15.
 */
public class TimeStampFixedSegmentPlan implements Plan {
    @Override
    public boolean isFinishedProcessing(long timestamp) {
        return false;
    }

    @Override
    public List<FrameRepresentation> whatToDoAt(long timestamp) {
        return null;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public String ioFile() {
        return null;
    }
}
