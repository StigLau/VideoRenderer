package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.collector.plan.*;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.ImageCollectShimInterface;
import no.lau.vdvil.plan.ImageCollectable;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SegmentFramePlan;
import no.lau.vdvil.renderer.video.CreateVideoFromScratchImages;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoBuilderWrapper {
    final ImageStore<BufferedImage> imageStore;
    private SegmentFramePlan framePlanFactory = new SegmentFramePlanFactory();
    final ImageCollectShimInterface imageCollectorShim;

    public VideoBuilderWrapper(ImageStore<BufferedImage> imageStore, ImageCollectShimInterface imageCollectorShim) {
        this.imageStore = imageStore;
        this.imageCollectorShim = imageCollectorShim;
    }

    public void createVideoPart(VideoConfig videoConfig, List<Komposition> fetchKompositions, Komposition buildKomposition, Path musicUrl, boolean useAudio) {
        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, musicUrl, 24, framePlanFactory, imageCollectorShim);
        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, videoConfig, useAudio);
    }
}