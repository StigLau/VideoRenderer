package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.ImageCollector;
import no.lau.vdvil.collector.WaitingVideoThumbnailsCollector;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TimestampPlan implements FrameRepresentationsPlan,  ImageCollectable{

    final long startTimeStamp;
    final long endTimeStamp;
    final String referenceId;

    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    final Path localFileRef;

    public TimestampPlan(Segment segment, int framerate, Path ioFile) {
        this.startTimeStamp = segment.start();
        this.endTimeStamp = segment.start() + segment.duration();
        this.referenceId = segment.id();

        this.localFileRef = ioFile;
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

    public Path localStorage() {
        return localFileRef;
    }

    public ImageCollector collector(ImageStore<BufferedImage> imageStore, int framerateMillis) {
        return new WaitingVideoThumbnailsCollector(this, imageStore, true);
    }

    public List<FrameRepresentation> getFrameRepresentations() {
        return frameRepresentations;
    }

}
