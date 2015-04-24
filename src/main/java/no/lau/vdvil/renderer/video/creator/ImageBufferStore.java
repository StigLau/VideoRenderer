package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.filter.FilterableSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * Storage for images in memory between extractor and video creator
 * @author Stig@Lau.no 12/04/15.
 */
public class ImageBufferStore<TYPE> implements ImageStore<TYPE> {

    //Segment Identifier, List of buffered images
    public final Map<String, List<TYPE>> segmentImageList = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ImageBufferStore.class);

    public List<TYPE> getImageAt(Long timeStamp, Komposition komposition) {
        float bpm = komposition.bpm;
        Stream<TYPE> images = komposition.segments.stream()
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
    List<TYPE> extractImage(long timeStamp, float bpm, Segment segmentA) {
        //TODO find a better way to pass inn filter configuration
        FilterableSegment segment = (FilterableSegment) segmentA;
        List<TYPE> images = segment.applyModifications(findImagesByInstructionId(segment.id()));
        long start = calc(segment.start(), bpm);
        double split = images.size() * (timeStamp - start) / calc(segment.duration(), bpm);
        int index = (int) Math.round(split);
        logger.debug("@{} - {} - {}/{}", timeStamp, segment.id(), index+1, images.size());
        return Collections.singletonList(images.get(index));
    }



    public void store(TYPE image, Long timeStamp, String segmentId) {
        if(segmentImageList.containsKey(segmentId)) {
            segmentImageList.get(segmentId).add(image);
        } else {
            List<TYPE> newImageList = new ArrayList<>();
            newImageList.add(image);
            segmentImageList.put(segmentId, newImageList);
        }
    }

    public List<TYPE> findImagesByInstructionId(String instructionId) {
        return segmentImageList.get(instructionId);
    }
}
