package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.renderer.video.stigs.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 23/03/15.
 */
public class ImageStore {

    public List<Instruction> instructions = new ArrayList<>();

    private Logger logger = LoggerFactory.getLogger(ImageStore.class);

    public BufferedImage getImageAt(Long timeStamp) throws IOException {
        for (Instruction instruction : instructions) {
            long start = instruction.fromMillis();
            long end = instruction.fromMillis() + instruction.durationMillis();
            if(start <= timeStamp && end >= timeStamp) {
                double split = instruction.relevantFiles.size() * (timeStamp - start) / instruction.durationMillis();
                int index = (int) Math.round(split);
                if(instruction.relevantFiles.size() > index) {
                    String file = instruction.relevantFiles.get(index);
                    logger.debug("Inserting image {} at timestamp {}", file, timeStamp);
                    return ImageIO.read(new File(file));
                }
            }
        }
        logger.trace("No image to insert at timestamp {}", timeStamp);
        return null;
    }
}