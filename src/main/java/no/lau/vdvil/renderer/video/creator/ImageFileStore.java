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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static no.lau.vdvil.domain.utils.KompositionUtils.durationMillis;
import static no.lau.vdvil.domain.utils.KompositionUtils.fromMillis;

/**
 * @author Stig@Lau.no 23/03/15.
 */
public class ImageFileStore implements ImageStore {

    private final Komposition komposition;
    private String outputFilePrefix;
    public final Map<ImageSampleInstruction, List<String>> instructionListMap = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(ImageFileStore.class);

    public ImageFileStore(Komposition komposition, String outputFilePrefix) {
        this.komposition = komposition;
        this.outputFilePrefix = outputFilePrefix;

        File destinationFolder = new File(outputFilePrefix);
        if(!destinationFolder.exists() && !destinationFolder.mkdirs()) {
            throw new RuntimeException("Could not create " + outputFilePrefix);
        };

    }

    public List<BufferedImage> getImageAt(Long timeStamp, float bpm) {
        return komposition.instructions.stream()
                .filter(instruction -> {
                    long start = fromMillis(instruction, komposition);
                    long end = fromMillis(instruction, komposition) + durationMillis(instruction, komposition);
                    System.out.println("Timestamp - Komposition Starts at " + start + " : " + end );
                    return start <= timeStamp && end >= timeStamp;
                })
                .map(instruction -> extractImage(timeStamp, instruction))
                .filter(instance -> instance != null)
                .collect(Collectors.toList());
    }

    public void store(BufferedImage image, Long timeStamp, ImageSampleInstruction instruction) {
        String outputFilename = outputFilePrefix + timeStamp + ".png";
        if(image == null) {
            logger.debug("No image to write at {}", timeStamp);
            return;
        }
        else {
            try {
                ImageIO.write(image, "png", new File(outputFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(instructionListMap.containsKey(instruction)) {
            instructionListMap.get(instruction).add(outputFilename);
        } else {
            List<String> newImageList = new ArrayList<>();
            newImageList.add(outputFilename);
            instructionListMap.put(instruction, newImageList);
        }
    }

    @Override
    public Stream<BufferedImage> findImagesByInstructionId(String instructionId) {
        return instructionListMap.entrySet().stream()
                .filter(entry -> entry.getKey().id().equals(instructionId))
                .flatMap(entry -> entry.getValue().stream())
                .map(this::getAsFile);
    }

    BufferedImage getAsFile(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (IOException e) {
            logger.error("Did not find image {}", filename);
            return null;
        }
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