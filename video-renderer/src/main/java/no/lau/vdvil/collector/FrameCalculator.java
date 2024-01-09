package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;

/**
 * @author Stig@Lau.no 30/05/15.
 * The FrameCalculator tackles the differences in number of frames between collector and builder,
 * as well as handling special circumstances such as build-segment showing at much lower framerates
 */
public interface FrameCalculator {
    long calculateNumberOfFrames(Segment segment, float bpm, long framerate);
}

