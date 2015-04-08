package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.VideoStillImageRepresentation;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import static no.lau.vdvil.domain.utils.KompositionUtils.durationMillis;
import static no.lau.vdvil.domain.utils.KompositionUtils.fromMillis;

/**
 * @author Stig@Lau.no 23/03/15.
 */
public class ImageStore {

    Komposition komposition;

    private Logger logger = LoggerFactory.getLogger(ImageStore.class);

    public List<BufferedImage> getImageAt(Long timeStamp) throws IOException {
        return komposition.instructions.stream()
                .filter(instruction -> {
                    long start = fromMillis(instruction, komposition);
                    long end = fromMillis(instruction, komposition) + durationMillis(instruction, komposition);
                    return start <= timeStamp && end >= timeStamp;
                })
                .map(instruction -> extractImage(timeStamp, instruction))
                .filter(instance -> instance != null)
                .collect(Collectors.toList());
    }

    BufferedImage extractImage(long timeStamp, Instruction instruction) {
        VideoStillImageRepresentation[] stillImages = ((VideoStillImageSegment) instruction.segment).getStillImages();
        long start = fromMillis(instruction, komposition);
        double split = stillImages.length * (timeStamp - start) / durationMillis(instruction, komposition);
        int index = (int) Math.round(split);
        if (stillImages.length > index) {
            String file = stillImages[index].fileLocation;
            logger.debug("Inserting image {} at timestamp {}", file, timeStamp);
            try {
                return ImageIO.read(new File(file));
            } catch (IOException e) {
                logger.error("Did not find file " + file, e);
            }
        }
        logger.debug("Did not find image at timestamp {}", timeStamp);
        return null;
    }
}