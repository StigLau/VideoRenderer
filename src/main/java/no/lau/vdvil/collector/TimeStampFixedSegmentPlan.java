package no.lau.vdvil.collector;

import no.lau.vdvil.plan.FrameRepresentationsPlan;
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
    private transient int frameNrLopenr = 0;

    public TimeStampFixedSegmentPlan(TimeStampFixedImageSampleSegment segment, String resultingMediaFile) {
        this.segment = segment;
        this.mediaFile = resultingMediaFile;
        length = segment.timestampEnd - segment.timestampStart;
    }

    public boolean isFinishedProcessing(long timestamp) {
        return timestamp > length;
    }

    public List<FrameRepresentation> whatToDoAt(long timestamp) {
        //Assumes that segments will be written at start
        if(timestamp >= 0 && timestamp < length) {
            FrameRepresentation fr = new FrameRepresentation(timestamp, segment.id(), segment);
            fr.frameNr = frameNrLopenr++;
            return Collections.singletonList(fr);
        } else
            return Collections.emptyList();

    }

    public String id() {
        return segment.id();
    }

    public String ioFile() {
        return mediaFile;
    }

}
