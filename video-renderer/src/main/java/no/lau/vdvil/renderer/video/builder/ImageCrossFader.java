package no.lau.vdvil.renderer.video.builder;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.TransitionSegment;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stig@Lau.no 18/08/16.
 * Simple implementation of a crossfader
 */
public class ImageCrossFader implements ImageBuilder {

    private final ImageStore<BufferedImage> imageStore;

    //Cached representation of this and/or cachedImage
    ImageRepresentation imageRepIn = null;
    ImageRepresentation imageRepOut = null;
    Logger logger = LoggerFactory.getLogger(getClass());

    public ImageCrossFader(ImageStore<BufferedImage> imageStore) {
        this.imageStore = imageStore;
    }

    public BufferedImage perform(TransitionSegment transitionSegment, List<FrameRepresentation> frameRepresentations) {
        for (FrameRepresentation frameRepresentation : frameRepresentations) {
            //Find the latest framerepresentation or used cached version
            if(frameRepresentation.referenceId().equals(transitionSegment.references().get(0))) {
                ImageRepresentation tempImageRepIn = imageStore.getNextImageRepresentation(frameRepresentation.referenceId());
                if(tempImageRepIn != null) {
                    imageRepIn = tempImageRepIn;
                }
            }
            if(frameRepresentation.referenceId().equals(transitionSegment.references().get(1))) {
                ImageRepresentation tempImageRepOut = imageStore.getNextImageRepresentation(frameRepresentation.referenceId());
                if(tempImageRepOut != null) {
                    imageRepOut = tempImageRepOut;
                }
            }
            frameRepresentation.use();
        }

        //Extract build framerepresentation
        for (FrameRepresentation crossover : frameRepresentations) {
            if(transitionSegment.id().equals(crossover.referenceId())) {
                float progress = (float)(crossover.frameNr +1) / crossover.numberOfFrames;
                return crossfade((BufferedImage) imageRepIn.image, (BufferedImage) imageRepOut.image, progress);
            }
        }
        throw new RuntimeException("Didn't find a transitionsegment for crossfading");
    }

    public static List<TransitionSegment> extractTransitionSegment(long timestamp, SuperPlan buildPlan) {
        List<TransitionSegment> foundSegments = new ArrayList<>();
        for (FramePlan plan : buildPlan.getMetaPlansAt(timestamp)) {
            if (plan != null && plan.wrapper().segment instanceof TransitionSegment) {
                foundSegments.add((TransitionSegment) plan.wrapper().segment);
            }
        }
        return foundSegments;
    }

    private BufferedImage crossfade(BufferedImage inImage, BufferedImage outImage, float alpha) {
        Graphics2D g2d = inImage.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(1f - alpha));
        g2d.drawImage(inImage, 0, 0, null);
        g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2d.drawImage(outImage, 0, 0, null);
        return inImage;
    }

    public void build(Plan buildPlan, List<FrameRepresentation> frameRepresentations, long nextFrameTime, VideoWriter writeCallback) throws Exception {
        List<TransitionSegment> transitionSegments = buildPlan instanceof SuperPlan ?
                ImageCrossFader.extractTransitionSegment(nextFrameTime, (SuperPlan) buildPlan):
                Collections.EMPTY_LIST;

        List<TransitionSegment> applicableTransitionSegments = containsMetaSegment(frameRepresentations, transitionSegments);
        if (applicableTransitionSegments.size() >= 1) {
            for (TransitionSegment segment : applicableTransitionSegments) {
                logger.trace("Performing crossover");
                BufferedImage theImage = perform(segment, frameRepresentations);
                writeCallback.write(theImage);
                for (FrameRepresentation frameRepresentation : frameRepresentations) {
                    frameRepresentation.use();
                }
            }
            return;
        }
        throw new Exception("Could not process frame with this builder");
    }

    /**
     * Find metaSegments related to framerepresentatives
     * TODO simplify this implementation
     */
    public List<TransitionSegment> containsMetaSegment(List<FrameRepresentation> frameRepresentations, List<TransitionSegment> transitionSegments) {
        List<TransitionSegment> metasegmentsFound = new ArrayList<>();

        for (TransitionSegment segment : transitionSegments) {
            for (FrameRepresentation frameRepresentation : frameRepresentations) {
                if(segment.id().equals(frameRepresentation.referenceId())) {
                    metasegmentsFound.add(segment);
                }
            }
        }
        List<TransitionSegment> filteredSegments = new ArrayList<>();

        for (TransitionSegment segment : metasegmentsFound) {
            int framesFound = 0;
            for (Object idref : segment.references()) {
                for (FrameRepresentation frameRepresentation : frameRepresentations) {
                    if (frameRepresentation.referenceId().equals(idref)) {
                        framesFound++;
                    }
                }
                if(framesFound == segment.references().size() ) {
                    filteredSegments.add(segment);
                }
            }
        }
        return filteredSegments;
    }
}
