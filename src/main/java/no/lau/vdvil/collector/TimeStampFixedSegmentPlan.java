package no.lau.vdvil.collector;

import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import java.util.Collections;
import java.util.List;

/**
 * @author Stig@Lau.no
 * Plan for building media file based on a timestamp-fixed segment
 * Responsible for finding the start end points of a built video
 */
public class TimeStampFixedSegmentPlan implements Plan {
    private final String mediaFile;
    private TimeStampFixedImageSampleSegment segment;
    private final long length;

    public TimeStampFixedSegmentPlan(TimeStampFixedImageSampleSegment segment, String resultingMediaFile) {
        this.segment = segment;
        this.mediaFile = resultingMediaFile;
        length = segment.timestampEnd - segment.timestampStart;
    }

    @Override
    public boolean isFinishedProcessing(long timestamp) {
        return timestamp > length;
    }

    @Override
    public List<FrameRepresentation> whatToDoAt(long timestamp) {
        //Assumes that segments will be written at start
        if(timestamp >= 0 && timestamp < length) {
            return Collections.singletonList(new FrameRepresentation(timestamp, segment.id(), segment));
        } else
            return Collections.emptyList();

    }

    @Override
    public String id() {
        return segment.id();
    }

    @Override
    public String ioFile() {
        return mediaFile;
    }
}
