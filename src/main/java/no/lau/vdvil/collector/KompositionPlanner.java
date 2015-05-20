package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class KompositionPlanner {
    public final List<SegmentFramePlan> plans = new ArrayList<>();
    final long lastTimeStamp;
    final float bpm;
    public final Segment builderSegment;
    public final Komposition fetchKomposition;

    public KompositionPlanner(Segment originalSegment, Segment builderSegment, float bpm, long framerate, Komposition fetchKomposition) {
        this.builderSegment = builderSegment;
        this.fetchKomposition = fetchKomposition;
        if(framerate <= 0) {
            throw new RuntimeException("Komposition Framerate not for komposition");
        }
        this.bpm = bpm;
        plans.add(new SegmentFramePlan(originalSegment, builderSegment, bpm, framerate));
        lastTimeStamp = originalSegment.startCalculated(bpm) + originalSegment.durationCalculated(bpm);
        Collections.sort(plans);
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

    public boolean isFinishedProcessing(long timestamp) {
        return timestamp >= lastTimeStamp;
    }

    public List<SegmentFramePlan> plansAt(long timestamp) {
        List<SegmentFramePlan> matchingPlans = new ArrayList<>();
        for (SegmentFramePlan plan : plans) {
            long start = plan.originalSegment.startCalculated(bpm);
            long duration = plan.originalSegment.durationCalculated(bpm);
            if(timestamp >= start && timestamp < start + duration) {
                matchingPlans.add(plan);
            }

        }
        return matchingPlans;
    }
}