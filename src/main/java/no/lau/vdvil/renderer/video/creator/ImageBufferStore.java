package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.filter.FilterableSegment;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * Storage for images in memory between extractor and video creator
 * @author Stig@Lau.no 12/04/15.
 */
public class ImageBufferStore<TYPE> implements ImageStore<TYPE> {

    //Segment Identifier, List of buffered images
    public final Map<String, BlockingQueue<ImageRepresentation>> segmentImageList = new HashMap<>();
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
        return findImagesBySegmentId(segment.id(), 0, 20);
    }


    /**
     * Note the usage of BlockingQueue.put, to block if queue is full!
     */
    public void store(TYPE instance, Long timeStamp, String segmentId) {
        ImageRepresentation imageRepresentation = new ImageRepresentation(Long.toString(timeStamp), segmentId);
        imageRepresentation.image = instance;
        try {
            if (segmentImageList.containsKey(segmentId)) {
                segmentImageList.get(segmentId).put(imageRepresentation);
            } else {
                BlockingQueue<ImageRepresentation> newImageList = new ArrayBlockingQueue<>(bufferSize);
                newImageList.put(imageRepresentation);
                segmentImageList.put(segmentId, newImageList);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Blocked queue insertion interrupted!", e);
        }
    }

    public synchronized List<TYPE> findImagesBySegmentId(String segmentId) {
        return findImagesCore(segmentId);
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

    List<TYPE> findImagesCore(String segmentId){
        try {
            BlockingQueue<ImageRepresentation> blockingQueue = segmentImageList.get(segmentId);
            ImageRepresentation representation = blockingQueue.poll(1000, TimeUnit.MILLISECONDS);
            return Collections.singletonList((TYPE) representation.image);
        } catch (Exception e) {
            logger.debug("Fuck? {}, {}",segmentId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void setBufferSize(int imagesBufferSize) {
        this.bufferSize = imagesBufferSize;
    }
}