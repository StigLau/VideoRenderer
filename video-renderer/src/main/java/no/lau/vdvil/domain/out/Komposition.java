package no.lau.vdvil.domain.out;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import java.util.Arrays;
import java.util.List;
import static no.lau.vdvil.domain.out.KompositionUtil.filterByTime;

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

    public Komposition(float bpm, Segment... segments) {
        this(bpm, Arrays.asList(segments));
    }

    public Komposition(float bpm, List<Segment> segments) {
        this.segments = segments;
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

    public Komposition filter(long start, long duration) {
        List<Segment> filteredSegments = filterByTime(segments, start, duration);
        return new Komposition(bpm, filteredSegments);
    }

    public Komposition applyStorageLocation(MediaFile mediaFile) {
        storageLocation = mediaFile;
        return this;
    }
}