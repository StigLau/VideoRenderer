package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.collector.CollectorWrapper;
import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.collector.WaitingVideoThumbnailsCollector;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.ImageCollectable;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.CreateVideoFromScratchImages;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoBuilderWrapper {
    final ImageStore<BufferedImage> imageStore;

    public VideoBuilderWrapper(ImageStore<BufferedImage> imageStore) {
        this.imageStore = imageStore;
    }

    public void createVideoPart(VideoConfig videoConfig, List<Komposition> fetchKompositions, Komposition buildKomposition, URL musicUrl, boolean useAudio) {
        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, musicUrl, 24);
        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, videoConfig, useAudio);
    }
}