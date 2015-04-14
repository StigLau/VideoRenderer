package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
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

    public final Map<Segment, List<BufferedImage>> instructionListMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ImageBufferStore.class);

    public List<BufferedImage> getImageAt(Long timeStamp, Komposition komposition) {
        float bpm = komposition.bpm;
        for (Instruction instruction : komposition.instructions) {
            Segment segment = instruction.segment;
            long start = calc(segment.start(), bpm);
            long end = calc(segment.start(), bpm) + calc(segment.duration(), bpm);
            if (start <= timeStamp && end >= timeStamp) {
                Stream<BufferedImage> imageStream = findImagesByInstructionId(instruction.segment.id());
                List<BufferedImage> images = imageStream.collect(Collectors.toList());
                BufferedImage b = extractImage(timeStamp, bpm, images, segment);
                return Collections.singletonList(b);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Calculate which image among a list to show from a imageSample series
     * May return null if no relevant images were found
     */
    BufferedImage extractImage(long timeStamp, float bpm, List<BufferedImage> images, Segment instruction) {
        long start = calc(instruction.start(), bpm);
        double split = images.size() * (timeStamp - start) / calc(instruction.duration(), bpm);
        int index = (int) Math.round(split);
        if (images.size() > index && images.get(index) != null) {
            logger.debug("@{} - {} - {}/{}", timeStamp, instruction.id(), index+1, images.size());
            return images.get(index);
        }
        logger.error("Did not find image at timestamp {}", timeStamp);
        return null;
    }

    public void store(BufferedImage image, Long timeStamp, Segment instruction) {
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
