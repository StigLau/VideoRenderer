package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.filter.FilterableSegment;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
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
    public final Map<String, List<ImageRepresentation>> segmentImageList = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ImageBufferStore.class);
    private int bufferSize = 1000;

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
        List<TYPE> images = segment.applyModifications(findImagesBySegmentId(segment.id(), 0, 20));
        long start = calc(segment.start(), bpm);
        double split = images.size() * (timeStamp - start) / calc(segment.duration(), bpm);
        int index = (int) Math.round(split);
        logger.debug("@{} - {} - {}/{}", timeStamp, segment.id(), index + 1, images.size());
        return Collections.singletonList(images.get(index));
    }



    public void store(TYPE instance, Long timeStamp, String segmentId) {
        ImageRepresentation imageRepresentation = new ImageRepresentation(Long.toString(timeStamp), segmentId);
        imageRepresentation.image = instance;
        if(segmentImageList.containsKey(segmentId)) {
            segmentImageList.get(segmentId).add(imageRepresentation);
        } else {
            List<ImageRepresentation> newImageList = new ArrayList<>();
            newImageList.add(imageRepresentation);
            segmentImageList.put(segmentId, newImageList);
        }
    }

    public synchronized void prune(TYPE instance) {
        for (List<ImageRepresentation> imageRepresentations : segmentImageList.values()) {
            for (ImageRepresentation imageRepresentation : imageRepresentations) {
                if(imageRepresentation.image == instance) {
                    logger.info("Pruning {} of segment: ", imageRepresentation.imageId, imageRepresentation.segmentId);
                    imageRepresentations.remove(instance);
                }
            }
        }
    }

    public synchronized List<TYPE> findImagesBySegmentId(String segmentId) {
        if(!segmentImageList.containsKey(segmentId)) {
            return Collections.emptyList();
        }else {
            return findImagesCore(segmentId);
        }
    }

    /**
     * With retries
     */
    public synchronized List<TYPE> findImagesBySegmentId(String segmentId, int retries, int maxNrOfRetries) {
        if(retries >= maxNrOfRetries) {
            throw new RuntimeException("Could not find " + segmentId + " in ImageStore");
        }
        if(!segmentImageList.containsKey(segmentId)) {
            try {
                logger.info("Fetching {} retry {}", segmentId, retries);
                Thread.sleep(1000 * ++retries);
                return findImagesBySegmentId(segmentId, retries, maxNrOfRetries);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }else {
            return findImagesCore(segmentId);
        }
    }

    List<TYPE> findImagesCore(String segmentId) {
        return segmentImageList.get(segmentId).stream()
                .map(imgRep -> (TYPE)imgRep.image)
                .collect(Collectors.toList());

    }

    public boolean readyForNewImage(String segmentId) {
        if(!segmentImageList.containsKey(segmentId))
            return true;
        else {
            List<TYPE> images = findImagesBySegmentId(segmentId);
            return images == null || images.size() < bufferSize;
        }
    }

    public void setBufferSize(int imagesBufferSize) {
        this.bufferSize = imagesBufferSize;
    }
}