package no.lau.vdvil.plan;

import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.Segment;

public interface SegmentFramePlan {
    FramePlan createInstance(String collectId, SegmentWrapper wrapper);

    FramePlan createCollectPlan(Segment originalSegment, FramePlan buildFramePlan, long finalFramerate, float collectBpm);
}
