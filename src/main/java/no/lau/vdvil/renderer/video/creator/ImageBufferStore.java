package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * @author Stig@Lau.no 12/04/15.
 */
public class ImageBufferStore implements ImageStore {


    public final Map<ImageSampleInstruction, List<BufferedImage>> instructionListMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ImageBufferStore.class);

    public List<BufferedImage> getImageAt(Long timeStamp, float bpm) {
        return instructionListMap.entrySet().stream()
                .filter(instImages -> {
                    ImageSampleInstruction instruction = instImages.getKey();
                    long start = calc(instruction.start(), bpm);
                    long end = calc(instruction.start(), bpm) + calc(instruction.duration(), bpm);
                    return start <= timeStamp && end >= timeStamp;
                })
                .map(images -> extractImage(timeStamp, bpm, images))
                .filter(instance -> instance != null)
                .collect(Collectors.toList());
    }

    /**
     * Calculate which image among a list to show from a imageSample series
     * May return null if no relevant images were found
     */
    BufferedImage extractImage(long timeStamp, float bpm, Map.Entry<ImageSampleInstruction, List<BufferedImage>> imageSampleInstructionListEntry) {
        ImageSampleInstruction instruction = imageSampleInstructionListEntry.getKey();
        List<BufferedImage> images = imageSampleInstructionListEntry.getValue();

        long start = calc(instruction.start(), bpm);
        double split = images.size() * (timeStamp - start) / calc(instruction.duration(), bpm);
        int index = (int) Math.round(split);
        if (images.size() > index && images.get(index) != null) {
            logger.debug("Inserting image at timestamp {}", timeStamp);
            return images.get(index);
        }
        logger.debug("Did not find image at timestamp {}", timeStamp);
        return null;
    }


    public void store(BufferedImage image, Long timeStamp, ImageSampleInstruction instruction) {
        if(instructionListMap.containsKey(instruction)) {
            instructionListMap.get(instruction).add(image);
        } else {
            List<BufferedImage> newImageList = new ArrayList<>();
            newImageList.add(image);
            instructionListMap.put(instruction, newImageList);
        }
    }

    public Stream<BufferedImage> findImagesByInstructionId(String instructionId) {
        return instructionListMap.entrySet().stream()
                .filter(entry -> entry.getKey().id().equals(instructionId))
                .flatMap(entry -> entry.getValue().stream())
                .filter(asd -> asd != null);
    }
}
