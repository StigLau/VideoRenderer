package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Stig@Lau.no 12/04/15.
 */
public interface ImageStore {
    List<BufferedImage> getImageAt(Long timeStamp, float bpm);

    void store(BufferedImage image, Long timeStamp, ImageSampleInstruction instruction);

    Stream<BufferedImage> findImagesByInstructionId(String instructionId);
}
