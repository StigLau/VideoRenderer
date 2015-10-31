package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameCalculator;
import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentFramePlan;
import no.lau.vdvil.collector.SimpleCalculator;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.*;

/**
 * @author Stig@Lau.no
 */
public class SuperPlan implements Plan, AudioPlan{

    Logger logger = LoggerFactory.getLogger(SuperPlan.class);
    final long lastTimeStamp;
    final List<SegmentFramePlan> framePlans = new ArrayList<>(); //TODO Note that frameplans contain BOTH build and collect frameplans!
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    String collectId = "";
    final MediaFile storageLocation;
    public URL audioLocation;

    public SuperPlan(Segment collectionSegment, SegmentFramePlan buildFramePlan, MediaFile storageLocation, long finalFramerate, float collectBpm) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(Collections.singletonList(collectionSegment), collectBpm);
        //this.collectId = komposition.segments.get(0).id() + "_" + Math.abs(new Random().nextInt()); //TODO Les fra Map om hva ID'en skal hete!

            collectId = buildFramePlan.originalSegment.id();
            Segment buildSegment = buildFramePlan.originalSegment;
            SegmentFramePlan framePlan = new SegmentFramePlan(collectId, collectionSegment, collectBpm, finalFramerate, new SimpleCalculator(collectionSegment.durationCalculated(collectBpm), buildSegment.durationCalculated(collectBpm)));
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
    }

    /**
     * Constructor for BuildPlan
     */
    @Deprecated
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

    public SuperPlan(List<Segment> buildSegments, MediaFile storageLocation, float buildBpm, long finalFramerate, Map<String, Segment> segmentIdCollectSegmentMap) {
        this.storageLocation = storageLocation;
        lastTimeStamp = calculateLastTimeStamp(buildSegments, buildBpm);
        for (Segment buildSegment : buildSegments) {
            FrameCalculator frameCalculator;
            { //Extract the Frame duration
                Segment collectSegment = segmentIdCollectSegmentMap.get(buildSegment.shortId());
                long collectDuration = collectSegment.durationCalculated(buildBpm);

                long buildDuration = buildSegment.durationCalculated(buildBpm);
                //frameCalculator = new SimpleCalculator(buildDuration, collectDuration);
                frameCalculator = new SimpleCalculator(collectDuration, buildDuration);
            }
            SegmentFramePlan framePlan = new SegmentFramePlan(buildSegment.id(), buildSegment, buildBpm, finalFramerate, frameCalculator);
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

    public URL audioLocation() {
        return audioLocation;
    }
}