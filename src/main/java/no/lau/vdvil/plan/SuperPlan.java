package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentFramePlan;
import no.lau.vdvil.collector.SimpleCalculator;
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
    String collectId = "";
    public Map<String, String> referenceIdSegmentIdMap = new HashMap<>();

    public SuperPlan(Komposition komposition, List<Segment> collectionSegments, List<Segment> buildSegments, long finalFramerate) {
        this.komposition = komposition;
        lastTimeStamp = calculateLastTimeStamp(komposition.segments, komposition.bpm);
        //this.collectId = komposition.segments.get(0).id() + "_" + Math.abs(new Random().nextInt()); //TODO Les fra Map om hva ID'en skal hete!

        Map<String ,Segment> segmentMap = asSegmentMap(buildSegments);

        for (Segment segment : collectionSegments) {
            collectId += segment.id() + " ";
            String referenceId = segment.id() + "_"+ Math.abs(new Random().nextInt());
            referenceIdSegmentIdMap.put(referenceId, segment.id());
            Segment buildSegment = segmentMap.get(segment.id());
            SegmentFramePlan framePlan = new SegmentFramePlan(referenceId, segment, komposition.bpm, finalFramerate, new SimpleCalculator(segment.durationCalculated(komposition.bpm), buildSegment.durationCalculated(komposition.bpm)));
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
        }
        //TODO Sort framePlans
    }

    private Map<String, Segment> asSegmentMap(List<Segment> segments) {
        HashMap<String, Segment> result = new HashMap<>();
        for (Segment segment : segments) {
            result.put(segment.id(), segment);
        }
        return result;
    }

    /**
     * Constructor for BuildPlan
     */
    public SuperPlan(Komposition komposition, Map<String, String> segmentIdReferenceIdMap, long finalFramerate) {
        this.komposition = komposition;
        lastTimeStamp = calculateLastTimeStamp(komposition.segments, komposition.bpm);

        for (Segment segment : komposition.segments) {
            String id = getUnusedPipeBySegmentId(segment.id(), segmentIdReferenceIdMap);

            SegmentFramePlan framePlan = new SegmentFramePlan(id, segment, komposition.bpm, finalFramerate, new SimpleCalculator(1, 1));
            framePlans.add(framePlan);
            frameRepresentations.addAll(framePlan.frameRepresentations);
            logger.info("Build id = " + id);
        }
    }

    List<String> usedIds = new ArrayList<>();


    /**
     * Responsible for dealing out references to Pipes, and making sure that they are not reused
     */
    private String getUnusedPipeBySegmentId(String segmentId, Map<String, String> segmentIdReferenceIdMap) {
        Set<String> allIds = segmentIdReferenceIdMap.keySet();
        for (String plausibleId : allIds) {
            if(!usedIds.contains(plausibleId) && segmentIdReferenceIdMap.get(plausibleId).equals(segmentId)) {
                usedIds.add(plausibleId);
                return plausibleId;
            }
        }
        throw new RuntimeException("Did not find a usabe ID");
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