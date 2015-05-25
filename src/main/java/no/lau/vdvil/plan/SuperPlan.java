package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentFramePlan;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * @author Stig@Lau.no
 */
public class SuperPlan implements Plan{

    Logger logger = LoggerFactory.getLogger(SuperPlan.class);
    final Komposition komposition;
    final long lastTimeStamp;
    final List<SegmentFramePlan> framePlans = new ArrayList<>();
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    String collectId;

    public SuperPlan(Komposition komposition) {
        this.komposition = komposition;
        lastTimeStamp = calculateLastTimeStamp(komposition.segments, komposition.bpm);
        this.collectId = komposition.segments.get(0).id() + "_" + Math.abs(new Random().nextInt()); //TODO Les fra Map om hva ID'en skal hete!
        for (Segment segment : komposition.segments) {
            SegmentFramePlan framePlan = new SegmentFramePlan(this.collectId, segment, komposition.bpm, komposition.framerate);
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
        }
        logger.info("Collect id = " + collectId);



        //TODO Sort framePlans
    }

    /**
     * Constructor for BuildPlan
     */
    public SuperPlan(Komposition komposition, Map<String, String> segmentIdReferenceIdMap) {
        this.komposition = komposition;
        lastTimeStamp = calculateLastTimeStamp(komposition.segments, komposition.bpm);
        for (Segment segment : komposition.segments) {
            String id = segmentIdReferenceIdMap.get(segment.id());

            SegmentFramePlan framePlan = new SegmentFramePlan(id, segment, komposition.bpm, komposition.framerate);
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
            logger.info("Build id = " + id);
        }
    }

    @Override
    public boolean isFinishedProcessing(long timestamp) {
        return timestamp >= lastTimeStamp;
    }

    @Override
    public FrameRepresentation whatToDoAt(long timestamp) {
        for (FrameRepresentation frameRepresentation : frameRepresentations) {
            if(frameRepresentation.timestamp >= timestamp)
                return frameRepresentation;
        }
        return null;
    }

    @Override
    public String id() {
        return collectId;
    }

    @Override
    public String ioFile() {
        return komposition.storageLocation.fileName.toString();
    }

    @Override
    public float bpm() {
        return komposition.bpm;
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