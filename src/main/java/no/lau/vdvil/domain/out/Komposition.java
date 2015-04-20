package no.lau.vdvil.domain.out;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;

import java.util.Arrays;
import java.util.List;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class Komposition {
    public final List<Segment> segments;
    long lastInstruction;
    public final float bpm;

    //Width and height of the final Kompost to be produced
    public int height;
    public int width;
    //Where the final komposition is to be stored
    public MediaFile storageLocation;

    public Komposition(int bpm, Segment... segments) {
        this.segments = Arrays.asList(segments);
        this.bpm = bpm;
        lastInstruction = 0;
    }
}