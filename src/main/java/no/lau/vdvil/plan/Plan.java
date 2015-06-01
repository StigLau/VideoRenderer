package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import java.util.List;

/**
 * @author stiglau on 22/05/15.
 * A Plan contains all the segmentplans needed to collect frames or build with frameses
 */
public interface Plan {
    boolean isFinishedProcessing(long timestamp);

    List<FrameRepresentation> whatToDoAt(long timestamp);

    String id();

    String ioFile();

    float bpm();
}
