package no.lau.vdvil.collector;

import no.lau.vdvil.domain.utils.MaxSizeHashMap;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class FromImageFileCollector implements ImageCollector{

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Plan collectPlan;
    private final ImageStore<BufferedImage> imageStore;
    private final int framerate;
    private MaxSizeHashMap<URL, BufferedImage> imageCache;

    public FromImageFileCollector(Plan collectPlan, ImageStore<BufferedImage> imageStore, int framerate, int cacheSize) {
        this.collectPlan = collectPlan;
        this.imageStore = imageStore;
        this.framerate = framerate;
        imageCache = new MaxSizeHashMap<>(cacheSize);
    }


    public void runSingle() {
        logger.info("Starting capture {}", collectPlan.id());

        long startFrame = 0;

        for (long i = 0, timestamp = 0;  !collectPlan.isFinishedProcessing(timestamp); i++) {
            timestamp = startFrame + i * framerate;

            List<FrameRepresentation> frameRepresentations = collectPlan.whatToDoAt(timestamp);
            for (FrameRepresentation frameRepresentation : frameRepresentations) {
                try {
                    BufferedImage image = imageCache.getOrCache(frameRepresentation.imageUrl(), key -> {
                        try {
                            return ImageIO.read(key);
                        } catch (IOException e) {
                            throw new RuntimeException("Could not download image " + key);
                        }
                    });
                    imageStore.store(image, timestamp, frameRepresentation);
                    frameRepresentation.use();
                    logger.trace("Storing image {}@{}", frameRepresentation.referenceId(), timestamp);
                } catch (IOException e) {
                    logger.error("Problems collecting {}", frameRepresentation.referenceId());
                }
            }
        }
    }
}