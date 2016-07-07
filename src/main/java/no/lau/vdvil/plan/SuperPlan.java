package no.lau.vdvil.plan;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.collector.plan.SegmentFramePlanFactory;
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
    final List<FramePlan> framePlans = new ArrayList<>(); //TODO Note that frameplans contain BOTH build and collect frameplans!
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    final MediaFile storageLocation;
    public URL audioLocation;
    Segment originalSegment;

    public SuperPlan(Segment originalSegment, FramePlan buildFramePlan, MediaFile storageLocation, long finalFramerate, float collectBpm) {
        this.originalSegment = originalSegment;
        Segment buildSegment = buildFramePlan.wrapper().segment;
        float buildBpm = buildFramePlan.wrapper().bpm;
        this.storageLocation = storageLocation;
        long buildCalculatedBpm = buildSegment.durationCalculated(buildBpm);
        FramePlan framePlan = SegmentFramePlanFactory.createInstance(buildSegment.id(), new SegmentWrapper(originalSegment, collectBpm, finalFramerate, new SimpleCalculator(originalSegment.durationCalculated(collectBpm), buildCalculatedBpm)));
        framePlans.add(framePlan);
        frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
        if (originalSegment instanceof StaticImagesSegment) {
            lastTimeStamp = calculateLastTimeStamp(Collections.singletonList(buildSegment), buildBpm);
        } else {
            lastTimeStamp = calculateLastTimeStamp(Collections.singletonList(originalSegment), collectBpm);
        }
    }

    /**
     * Constructor for BuildPlan
     */
    @Deprecated
    public SuperPlan(List<Segment> buildSegments, MediaFile storageLocation, float buildBpm, long finalFramerate) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(buildSegments, buildBpm);
        for (Segment buildSegment : buildSegments) {
            FramePlan framePlan = SegmentFramePlanFactory.createInstance(buildSegment.id(), new SegmentWrapper(buildSegment, buildBpm, finalFramerate, new SimpleCalculator(1, 1)));
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
            logger.info("Build: " + buildSegment.id());
        }
    }

    public SuperPlan(List<Segment> buildSegments, MediaFile storageLocation, float buildBpm, long finalFramerate, Map<String, Segment> segmentIdCollectSegmentMap) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(buildSegments, buildBpm);
        for (Segment buildSegment : buildSegments) {
            SimpleCalculator frameCalculator;
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
            FramePlan framePlan = SegmentFramePlanFactory.createInstance(buildSegment.id(), new SegmentWrapper(buildSegment, buildBpm, finalFramerate, frameCalculator));
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
    public String id() {
        return toString();
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

    public List<FramePlan> getFramePlans() {
        return framePlans;
    }

    public List<FrameRepresentation> getFrameRepresentations() {
        return frameRepresentations;
    }

    public URL audioLocation() {
        return audioLocation;
    }

    public Segment originalSegment() {
        return originalSegment;
    }
}