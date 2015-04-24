package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * Storage for images in memory between extractor and video creator
 * @author Stig@Lau.no 12/04/15.
 */
public class ImageBufferStore implements ImageStore {

    //Segment Identifier, List of buffered images
    public final Map<String, List<BufferedImage>> segmentImageList = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ImageBufferStore.class);

    public List<BufferedImage> getImageAt(Long timeStamp, Komposition komposition) {
        float bpm = komposition.bpm;
        Stream<BufferedImage> images = komposition.segments.stream()
                .filter(segment -> {
                    long start = calc(segment.start(), bpm);
                    long end = calc(segment.start(), bpm) + calc(segment.duration(), bpm);
                    return start <= timeStamp && end >= timeStamp;
                })
                .flatMap(segment -> extractImage(timeStamp, bpm, segment).stream());
        return images.collect(Collectors.toList());
    }

    /**
     * Calculate which image among a list to show from a imageSample series
     * Returns a singleton list or empty list
     */
    List<BufferedImage> extractImage(long timeStamp, float bpm, Segment segment) {
        List<BufferedImage> images = new ListModificator<BufferedImage>().applyModifications(segment, findImagesByInstructionId(segment.id()));
        long start = calc(segment.start(), bpm);
        double split = images.size() * (timeStamp - start) / calc(segment.duration(), bpm);
        int index = (int) Math.round(split);
        logger.debug("@{} - {} - {}/{}", timeStamp, segment.id(), index+1, images.size());
        return Collections.singletonList(images.get(index));
    }



    public void store(BufferedImage image, Long timeStamp, String segmentId) {
        if(segmentImageList.containsKey(segmentId)) {
            segmentImageList.get(segmentId).add(image);
        } else {
            List<BufferedImage> newImageList = new ArrayList<>();
            newImageList.add(image);
            segmentImageList.put(segmentId, newImageList);
        }
    }

    public List<BufferedImage> findImagesByInstructionId(String instructionId) {
        return segmentImageList.get(instructionId);
    }
}
