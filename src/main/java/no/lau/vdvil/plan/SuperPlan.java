package no.lau.vdvil.plan;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.StaticImagesSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.*;

/**
 * @author Stig@Lau.no
 */
public class SuperPlan implements FrameRepresentationsPlan, AudioPlan{

    Logger logger = LoggerFactory.getLogger(SuperPlan.class);
    final long lastTimeStamp;
    final List<SegmentWrapper> framePlans = new ArrayList<>(); //TODO Note that frameplans contain BOTH build and collect frameplans!
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    String collectId = "";
    final MediaFile storageLocation;
    public URL audioLocation;
    Segment originalSegment;

    public SuperPlan(Segment originalSegment, SegmentWrapper buildFramePlan, MediaFile storageLocation, long finalFramerate, float collectBpm) {
        this.originalSegment = originalSegment;
        this.storageLocation = storageLocation;
        if (originalSegment instanceof StaticImagesSegment) {
            lastTimeStamp = calculateLastTimeStamp(Collections.singletonList(buildFramePlan.segment()), buildFramePlan.bpm());
        } else {
            lastTimeStamp = calculateLastTimeStamp(Collections.singletonList(originalSegment), collectBpm);
        }
        collectId = buildFramePlan.segment().id();
        Segment buildSegment = buildFramePlan.segment();
        SegmentWrapper framePlan = SegmentWrapperParent.chooseForMe(collectId, originalSegment, collectBpm, finalFramerate, new SimpleCalculator(originalSegment.durationCalculated(collectBpm), buildSegment.durationCalculated(buildFramePlan.bpm())));
        framePlans.add(framePlan);
        frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
    }

    /**
     * Constructor for BuildPlan
     */
    @Deprecated
    public SuperPlan(List<Segment> buildSegments, MediaFile storageLocation, float buildBpm, long finalFramerate) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(buildSegments, buildBpm);
        for (Segment buildSegment : buildSegments) {
            SegmentWrapper framePlan = SegmentWrapperParent.chooseForMe(buildSegment.id(), buildSegment, buildBpm, finalFramerate, new SimpleCalculator(1, 1));
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
            logger.info("Build: " + buildSegment.id());
        }
    }

    public SuperPlan(List<Segment> buildSegments, MediaFile storageLocation, float buildBpm, long finalFramerate, Map<String, Segment> segmentIdCollectSegmentMap) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(buildSegments, buildBpm);
        for (Segment buildSegment : buildSegments) {
            FrameCalculator frameCalculator;
            { //Extract the Frame duration
                Segment collectSegment = segmentIdCollectSegmentMap.get(buildSegment.shortId());
                if(collectSegment == null) {
                    throw new RuntimeException("Could not find buildSegment with id: " + buildSegment.shortId()+" in collectSegmentList " + segmentIdCollectSegmentMap.keySet());
                } else {
                    long collectDuration = collectSegment.durationCalculated(buildBpm);

                    long buildDuration = buildSegment.durationCalculated(buildBpm);
                    frameCalculator = new SimpleCalculator(collectDuration, buildDuration);
                }
            }
            SegmentWrapper framePlan = SegmentWrapperParent.chooseForMe(buildSegment.id(), buildSegment, buildBpm, finalFramerate, frameCalculator);
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
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

    public List<SegmentWrapper> getFramePlans() {
        return framePlans;
    }

    public List<FrameRepresentation> getFrameRepresentations() {
        return frameRepresentations;
    }

    public String toString() {
        return collectId;
    }

    public URL audioLocation() {
        return audioLocation;
    }

    public Segment originalSegment() {
        return originalSegment;
    }
}