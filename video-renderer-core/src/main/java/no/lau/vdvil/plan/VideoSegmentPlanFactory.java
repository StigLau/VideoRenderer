package no.lau.vdvil.plan;

import no.lau.vdvil.collector.ImageCollector;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.creator.ImageStore;

import java.awt.image.BufferedImage;

public interface VideoSegmentPlanFactory {

    FramePlan createInstance(String collectId, SegmentWrapper wrapper);

    FramePlan createCollectPlan(Segment originalSegment, FramePlan buildFramePlan, long finalFramerate, float collectBpm);

    ImageCollector extractShim(ImageCollectable imageCollectablePlan, ImageStore<BufferedImage> imageStore, int framerateMillis, FramePlan framePlan);
}
