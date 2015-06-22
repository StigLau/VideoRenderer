package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentFramePlan;
import no.lau.vdvil.collector.SimpleCalculator;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * @author Stig@Lau.no
 */
public class SuperPlan implements Plan{

    Logger logger = LoggerFactory.getLogger(SuperPlan.class);
    final long lastTimeStamp;
    final List<SegmentFramePlan> framePlans = new ArrayList<>(); //TODO Note that frameplans contain BOTH build and collect frameplans!
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    String collectId = "";
    final MediaFile storageLocation;

    public SuperPlan(Segment collectionSegment, SegmentFramePlan buildFramePlan, MediaFile storageLocation, long finalFramerate, float collectBpm) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(Collections.singletonList(collectionSegment), collectBpm);

            collectId = buildFramePlan.originalSegment.id();
            Segment buildSegment = buildFramePlan.originalSegment;
            SegmentFramePlan framePlan = new SegmentFramePlan(collectId, collectionSegment, collectBpm, finalFramerate, new SimpleCalculator(collectionSegment.durationCalculated(collectBpm), buildSegment.durationCalculated(collectBpm)), buildFramePlan.reversed, buildFramePlan.pipe);
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
    }

    /**
     * Constructor for BuildPlan
     */
    public SuperPlan(List<Segment> buildSegments, MediaFile storageLocation, float buildBpm, long finalFramerate) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(buildSegments, buildBpm);
        for (Segment buildSegment : buildSegments) {
            SegmentFramePlan framePlan = new SegmentFramePlan(buildSegment.id(), buildSegment, buildBpm, finalFramerate, new SimpleCalculator(1, 1));
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
            logger.info("Build: " + buildSegment.id());
        }
    }

    @Override
    public boolean isFinishedProcessing(long timestamp) {
        return timestamp > lastTimeStamp;
    }

    @Override
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

    @Override
    @Deprecated //Use toString. This is no real id!
    public String id() {
        return collectId;
    }

    @Override
    public String ioFile() {
        return storageLocation.fileName.toString();
    }

    static long calculateLastTimeStamp(List<Segment> segments, float bpm) {
        long lastTimeStamp = 0;
        for (Segment segment : segments) {
            long current = segment.startCalculated(bpm) + segment.durationCalculated(bpm);
            if (current > lastTimeStamp) {
                lastTimeStamp = current;
            }
        }
        return lastTimeStamp;
    }

    public List<SegmentFramePlan> getFramePlans() {
        return framePlans;
    }

    public List<FrameRepresentation> getFrameRepresentations() {
        return frameRepresentations;
    }

    public String toString() {
        return collectId;
    }
}