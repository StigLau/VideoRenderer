package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 12.05.2015.
 */
public class FrameRepresentation implements Comparable{
    public final long timestamp;
    public boolean used;
    private String referenceId;
    //Reference to Original segment for debugging purposes
    private Segment originalSegment;
    //To keep track of number of frames during building
    public long numberOfFrames = 0;
    public long frameNr = 0;
    URL imageUrl;

    public FrameRepresentation(long timestamp, String referenceId, Segment originalSegment) {
        this.timestamp = timestamp;
        this.referenceId = referenceId;
        this.originalSegment = originalSegment;
        used = false;
    }

    public void use() {
        this.used = true;
    }

    public boolean used() {
        return this.used;
    }

    public String referenceId() {
        return referenceId;
    }

    public String toString() {
        return "@" + timestamp + " " + originalSegment.id() + " - " + referenceId;
    }

    public String getSegmentShortId(){
        return originalSegment.shortId();
    }

    public URL imageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        try {
            this.imageUrl = new URL(imageUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Naugty imageURL " + imageUrl);
        }
    }

    public static FrameRepresentation createFrameRepresentation(String collectId, Segment segment, long numberOfAvailableFrames, long start, int i, long thisDuration) {
        FrameRepresentation frame = new FrameRepresentation(start + thisDuration, collectId, segment);
        frame.numberOfFrames = numberOfAvailableFrames;
        frame.frameNr = i;
        return frame;
    }

    public static List<FrameRepresentation> calculateFramesFromSegment(String collectSegmentId, Segment segment, long start, long frameRateMillis, long numberOfAvailableFrames, SimpleCalculator frameCalculator, Logger origLogger) {
        List<FrameRepresentation> frameRepresentations = new ArrayList<>();
        long numberOfCollectFrames = frameCalculator.collectRatio / frameRateMillis;
        //Logic for adding empty frames in case of more build frames is than collect frames
        long lastUsedFrame = 0; //Always starts at 0 for static images and collect
        origLogger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, collectSegmentId);

        for (int i = 0; i < numberOfAvailableFrames; i++) {
            long thisDuration = frameRateMillis * i;

            if (segment instanceof TimeStampFixedImageSampleSegment) {
                if (i > lastUsedFrame * (float) numberOfAvailableFrames / numberOfCollectFrames) {
                    lastUsedFrame++;
                }
            }

            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, collectSegmentId, segment);
            frame.numberOfFrames = numberOfAvailableFrames;
            frame.frameNr = i;
            frameRepresentations.add(frame);

            origLogger.trace(collectSegmentId + " #" + (i + 1) + " duration:" + thisDuration);
        }
        return frameRepresentations;
    }

    @Override
    public int compareTo(Object o) {
        return ((Long)this.timestamp).compareTo(((FrameRepresentation)o).timestamp);
    }
}
