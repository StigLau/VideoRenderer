package no.lau.vdvil.renderer.video.builder;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.TransitionSegment;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 18/08/16.
 * Simple implementation of a crossfader
 */
public class ImageCrossFader {

    private final ImageStore<BufferedImage> imageStore;

    //Cached representation of this and/or previous
    ImageRepresentation imageRepIn = null;
    ImageRepresentation imageRepOut = null;

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

        //Todo use the buildPlan to calculate the progress
        float progress = (float)(imageRepIn.frameRepresentation.frameNr +1) / imageRepIn.frameRepresentation.numberOfFrames;
        return crossfade((BufferedImage) imageRepIn.image, (BufferedImage) imageRepOut.image, progress);
    }

    public static List<TransitionSegment> extractTransitionSegment(long clock, SuperPlan buildPlan) {
        List<TransitionSegment> foundSegments = new ArrayList<>();
        for (FramePlan plan : buildPlan.getMetaPlansAt(clock)) {
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
}
