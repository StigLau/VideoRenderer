package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.out.Komposition;
import java.util.List;

/**
 * @author Stig@Lau.no 12/04/15.
 */
public interface ImageStore<TYPE> {
    List<TYPE> getImageAt(Long timeStamp, Komposition komposition);

    void store(TYPE image, Long timeStamp, String segmentId);

    List<TYPE> findImagesBySegmentId(String segmentId);
}
