package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.*;
import no.lau.vdvil.renderer.video.CreateVideoFromScratchImages;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoBuilderWrapper {
    final ImageStore<BufferedImage> imageStore;
    final VideoSegmentPlanFactory videoShim;

    public VideoBuilderWrapper(ImageStore<BufferedImage> imageStore, VideoSegmentPlanFactory videoShim) {
        this.imageStore = imageStore;
        this.videoShim = videoShim;
    }

    public void createVideoPart(VideoConfig videoConfig, List<Komposition> fetchKompositions, Komposition buildKomposition, Path musicUrl, boolean useAudio) {
        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, musicUrl, 24, videoShim);
        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, videoConfig, useAudio);
    }
}