package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static no.lau.vdvil.domain.utils.KompositionUtils.durationMillis;
import static no.lau.vdvil.domain.utils.KompositionUtils.fromMillis;

/**
 * @author Stig@Lau.no 23/03/15.
 */
public class ImageFileStore<TYPE> implements ImageStore<TYPE> {

    private final Komposition komposition;
    private String outputFilePrefix;
    public final Map<String, List<String>> segmentImageList = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(ImageFileStore.class);

    public ImageFileStore(Komposition komposition, String outputFilePrefix) {
        this.komposition = komposition;
        this.outputFilePrefix = outputFilePrefix;

        File destinationFolder = new File(outputFilePrefix);
        if(!destinationFolder.exists() && !destinationFolder.mkdirs()) {
            throw new RuntimeException("Could not create " + outputFilePrefix);
        }
    }

    public List<TYPE> getImageAt(Long timeStamp, Komposition komposition) {
        return komposition.segments.stream()
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

    public void store(TYPE image, Long timeStamp, String segmentId) {
        throw new RuntimeException("Not implemented - go away!");
    }

    public void store(TYPE image, Long timeStamp, FrameRepresentation frameRepresentation) {
        String segmentId = frameRepresentation.referenceId();
        String outputFilename = outputFilePrefix + "/" + frameRepresentation.getSegmentShortId() + "_" + timeStamp + ".png";
        if(image == null) {
            logger.debug("No image to write at {}", timeStamp);
            return;
        }
        else {
            try {
                ImageIO.write((RenderedImage) image, "png", new File(outputFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(segmentImageList.containsKey(segmentId)) {
            segmentImageList.get(segmentId).add(outputFilename);
        } else {
            List<String> newImageList = new ArrayList<>();
            newImageList.add(outputFilename);
            segmentImageList.put(segmentId, newImageList);
        }
    }

    public List<TYPE> findImagesBySegmentId(String instructionId) {
        return segmentImageList.get(instructionId).stream()
                .map(this::getAsFile)
                .collect(Collectors.toList());
    }

    public TYPE findImagesByFramePlan(FramePlan framePlan, FrameRepresentation frameRepresentation) {
        throw new RuntimeException("Not implemented - go away!");
    }

    @Override
    public ImageRepresentation getNextImageRepresentation(String id) {
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * The fileStore doesn't need to monitor number of files in cache at this stage
     */
    public boolean readyForNewImage(String segmentId) {
        return true;
    }

    public void prune(TYPE instance) {
        logger.info("Not implemented");
    }

    TYPE getAsFile(String filename) {
        try {
            return (TYPE) ImageIO.read(new File(filename));
        } catch (IOException e) {
            logger.error("Did not find image {}", filename);
            return null;
        }
    }

    TYPE extractImage(long timeStamp, Segment segment) {
        if (segment instanceof ImageSampleInstruction) {
            ImageSampleInstruction imageSampleSegment = (ImageSampleInstruction) segment;

            List<String> stillImages = imageSampleSegment.collectedImages();
            long start = fromMillis(segment, komposition);
            double split = stillImages.size() * (timeStamp - start) / durationMillis(segment, komposition);
            int index = (int) Math.round(split);
            if (stillImages.size() > index) {
                String file = stillImages.get(index);
                if(file != null) {
                    logger.debug("Inserting image {} at timestamp {}", file, timeStamp);
                    try {
                        return (TYPE) ImageIO.read(new File(file));
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