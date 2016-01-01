package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.domain.Segment;
import java.util.ArrayList;
import java.util.List;

/**
 * Todo Merge with SuperPlan!!!
 */
public class TimestampPlan implements FrameRepresentationsPlan {

    final long startTimeStamp;
    final long endTimeStamp;
    final String referenceId;
    private final Segment segment;

    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    final String ioFile;

    public TimestampPlan(Segment segment, int framerate, String ioFile) {
        this.segment = segment;
        this.startTimeStamp = segment.start();
        this.endTimeStamp = segment.start() + segment.duration();
        this.referenceId = segment.id();

        this.ioFile = ioFile;
        long frames = segment.duration() / framerate;
        for (int i = 0; i < frames; i++) {
            long iTimeStamp = startTimeStamp + i * framerate;
            frameRepresentations.add(new FrameRepresentation(iTimeStamp, referenceId, segment));
        }
    }

    public boolean isFinishedProcessing(long timestamp) {
        return timestamp > endTimeStamp;
    }

    public List<FrameRepresentation> whatToDoAt(long timestamp) {
        List<FrameRepresentation> foundFrames = new ArrayList<>();
        for (FrameRepresentation frameRepresentation : frameRepresentations) {
            if(!frameRepresentation.used && frameRepresentation.timestamp <= timestamp) {
                frameRepresentation.use();
                foundFrames.add(frameRepresentation);
            }
        }
        return foundFrames;
    }

    public String id() {
        return referenceId;
    }

    public String ioFile() {
        return ioFile;
    }

    public List<FrameRepresentation> getFrameRepresentations() {
        return frameRepresentations;
    }

    public Segment originalSegment() {
        return segment;
    }
}
