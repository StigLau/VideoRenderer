package no.lau.vdvil.renderer.video.builder;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.plan.Plan;
import java.util.List;

/**
 * @author Stig@Lau.no 26/08/16.
 * ImageBuilders are used during writing of images, to select and modify images as well as writing
 */
public interface ImageBuilder {
    void build(Plan buildPlan, List<FrameRepresentation> frameRepresentations, long nextFrameTime, VideoWriter writeCallback) throws Exception;
}
