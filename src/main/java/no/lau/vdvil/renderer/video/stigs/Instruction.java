package no.lau.vdvil.renderer.video.stigs;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    public final String id;
    final double from;
    final double duration;
    final double bpm;
    public final double framesPerBeat;

    public List<String> relevantFiles = new ArrayList<>();

    public Instruction(String id, int from, int duration, double bpm, double framesPerBeat) {
        this.id = id;
        this.from = from;
        this.duration = duration;
        this.bpm = bpm;
        this.framesPerBeat = framesPerBeat;
    }

    public long fromMillis(double bpm) {
        return calc(from, bpm);
    }
    public long fromMillis() {
        return calc(from, bpm);
    }

    public long durationMillis(double bpm) {
        return calc(duration, bpm);
    }

    public long durationMillis() {
        return calc(duration, bpm);
    }

    public long calc(double time, double bpm) {
        return (long) (time  * 60 * 1000 * 1000 / bpm);
    }

    public boolean contains(long timestamp) {
        return (timestamp > fromMillis(bpm) && timestamp < (fromMillis(bpm) + durationMillis(bpm)));
    }
}
