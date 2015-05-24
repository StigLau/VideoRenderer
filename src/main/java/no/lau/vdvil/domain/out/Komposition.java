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
    public final long lastInstruction;
    public final float bpm;

    //Where the final komposition is to be stored
    public MediaFile storageLocation;
    public long framerate;

    public Komposition(int bpm, Segment... segments) {
        this.segments = Arrays.asList(segments);
        this.bpm = bpm;
        long last = 0;
        for (Segment segment : segments) {
            long newTime = segment.startCalculated(bpm) + segment.durationCalculated(bpm);
            if(newTime > last) {
                last = newTime;
            }
        }
        lastInstruction = last;
    }
}