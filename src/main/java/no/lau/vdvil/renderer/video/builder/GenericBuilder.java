package no.lau.vdvil.renderer.video.builder;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author Stig@Lau.no 19/08/16.
 * The generic builder for piecing together an image from the rest of Kompost
 */
public class GenericBuilder {
    Logger logger = LoggerFactory.getLogger(GenericBuilder.class);
     final ImageStore imageStore;
    BufferedImage previous = null;

    public GenericBuilder(ImageStore imageStore) {
        this.imageStore = imageStore;
    }

    public BufferedImage build(long nextFrameTime, List<FrameRepresentation> frameRepresentations) {
        for (FrameRepresentation frameRepresentation : frameRepresentations) {
            ImageRepresentation imageRep = imageStore.getNextImageRepresentation(frameRepresentation.referenceId());

            if (imageRep != null || previous != null) {
                String imgid;
                BufferedImage theImage;
                if (imageRep != null) {
                    imgid = String.valueOf(imageRep.frameRepresentation.frameNr);
                    theImage = (BufferedImage) imageRep.image;
                } else {
                    imgid = "UNKNOWN IMAGE DUE TO NULL";
                    theImage = previous;
                }

                logger.debug("Flushing {}@image#{}\t{}/{} \t Clock:{} from pipedream to video", frameRepresentation.referenceId(), imgid, frameRepresentation.frameNr + 1, frameRepresentation.numberOfFrames, nextFrameTime);
                frameRepresentation.use();
                //In some circumstances, one must reuse the previous image

                previous = theImage;
                return theImage;
            } else {
                logger.error("OMG OMG!!! Imagerep was null after waiting 10 seconds!! - {} - {}/{} is this related to division rest error?" + frameRepresentation.referenceId(), frameRepresentation.frameNr, frameRepresentation.numberOfFrames);
            }

        }

        return previous;
    }
}
