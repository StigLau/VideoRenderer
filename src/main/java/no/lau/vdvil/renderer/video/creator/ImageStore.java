package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Stig@Lau.no 12/04/15.
 */
public interface ImageStore {
    List<BufferedImage> getImageAt(Long timeStamp, Komposition komposition);

    void store(BufferedImage image, Long timeStamp, Segment instruction);

    Stream<BufferedImage> findImagesByInstructionId(String instructionId);
}
