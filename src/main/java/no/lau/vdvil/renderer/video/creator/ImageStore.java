package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
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

    private final Komposition komposition;

    private Logger logger = LoggerFactory.getLogger(ImageStore.class);

    public ImageStore(Komposition komposition) {
        this.komposition = komposition;
    }

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
        if (instruction.segment instanceof ImageSampleInstruction) {
            ImageSampleInstruction segment = (ImageSampleInstruction) instruction.segment;

            List<String> stillImages = segment.collectedImages();
            long start = fromMillis(instruction, komposition);
            double split = stillImages.size() * (timeStamp - start) / durationMillis(instruction, komposition);
            int index = (int) Math.round(split);
            if (stillImages.size() > index) {
                String file = stillImages.get(index);
                if(file != null) {
                    logger.debug("Inserting image {} at timestamp {}", file, timeStamp);
                    try {
                        return ImageIO.read(new File(file));
                    } catch (IOException e) {
                        logger.error("Did not find file " + file, e);
                    }
                }
            }
        }
        logger.debug("Did not find image at timestamp {}", timeStamp);
        return null;
    }
}