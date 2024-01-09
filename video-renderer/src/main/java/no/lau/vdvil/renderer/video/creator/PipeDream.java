package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.collector.FrameRepresentation;
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
public class PipeDream<TYPE> implements ImageStore<TYPE> {

    //Segment Identifier, List of buffered images
    public final Map<String, BlockingQueue<ImageRepresentation>> segmentImageList = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(PipeDream.class);
    //Number of images to buffer before blocking
    private final int bufferSize;
    private final int waitPeriod;
    private final int queueBlockWait;
    private final int numberOfRetryAttempts;

    //Standard constructor
    public PipeDream() {
        this.waitPeriod = 2000;
        bufferSize = 15;
        queueBlockWait = 10000;
        numberOfRetryAttempts = 10;
    }

    public PipeDream(int bufferSize, int retryAttemptWaitPeriodMillis, int queueBlockWait, int numberOfRetryAttempts) {
        this.waitPeriod = retryAttemptWaitPeriodMillis;
        this.bufferSize = bufferSize;
        this.queueBlockWait = queueBlockWait;
        this.numberOfRetryAttempts = numberOfRetryAttempts;
    }

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

    @Override @Deprecated
    public void store(TYPE image, Long timeStamp, String segmentId) {
        throw new RuntimeException("Not implemented - go away!");
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
    public void store(TYPE instance, Long timeStamp, FrameRepresentation frameRepresentation) {
        String referenceId = frameRepresentation.referenceId();
        ImageRepresentation imageRepresentation = new ImageRepresentation(Long.toString(timeStamp), referenceId, frameRepresentation);
        imageRepresentation.image = instance;
        try {
            logger.debug("Piping img@{} {}@{}\t{}/{} \t Clock: {} from source to PipeDream", Integer.toHexString(instance.hashCode()), frameRepresentation.referenceId(), timeStamp, frameRepresentation.frameNr +1, frameRepresentation.numberOfFrames, frameRepresentation.timestamp);

            if (segmentImageList.containsKey(referenceId)) {
                segmentImageList.get(referenceId).put(imageRepresentation);
            } else {
                logger.info("Creating queue {} from frameRepresentationId", referenceId);
                BlockingQueue<ImageRepresentation> newImageList = new ArrayBlockingQueue<>(bufferSize);
                newImageList.put(imageRepresentation);
                segmentImageList.put(referenceId, newImageList);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Blocked queue insertion interrupted!", e);
        }
    }

    public synchronized List<TYPE> findImagesBySegmentId(String segmentId) {
        return findImagesCore(segmentId);
    }

    public ImageRepresentation getNextImageRepresentation(String referenceId) {
        logger.trace("Looking for BlockQueue id = " + referenceId);
        for (int retryNr = 0; retryNr < numberOfRetryAttempts; retryNr++) {
            if (!segmentImageList.containsKey(referenceId)) {
                try {
                    logger.warn("PipeDream waiting for {} - {}", referenceId, numberOfRetryAttempts - retryNr);
                    Thread.sleep(waitPeriod);
                } catch (InterruptedException e) {

                }
            }else {
                break;
            }
        }
        BlockingQueue<ImageRepresentation> blockingQueue = segmentImageList.get(referenceId);
        try {
        return blockingQueue.poll(queueBlockWait, TimeUnit.MILLISECONDS);
            //return blockingQueue.take();
        } catch (Exception e) {
            logger.error("Could not fetch data from buffer {}. Errormessage: '{}'. Temp fix: increase buffer size.", referenceId, e.getMessage());
            return null;
        }
    }

    /**
     * With retries
     */
    synchronized List<TYPE> findImagesBySegmentId(String segmentId, int retries, int maxNrOfRetries) {
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

    public void emptyCache() {
        for (String segmentKey : segmentImageList.keySet()) {
            logger.info("Removing {} entries from queue {}. {} ", segmentImageList.get(segmentKey).size(), segmentKey);
        }
        segmentImageList.clear();
    }
}