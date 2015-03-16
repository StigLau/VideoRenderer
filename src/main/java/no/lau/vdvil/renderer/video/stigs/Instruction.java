package no.lau.vdvil.renderer.video.stigs;

import java.util.ArrayList;
import java.util.List;

public class Instruction {
    public final String id;
    final int from;
    final int duration;
    final int bpm;

    public List<String> relevantFiles = new ArrayList<>();

    public Instruction(String id, int from, int duration, int bpm) {
        this.id = id;
        this.from = from;
        this.duration = duration;
        this.bpm = bpm;
    }

    public long fromMillis(int bpm) {
        return calc(from, bpm);
    }
    public long fromMillis() {
        return calc(from, bpm);
    }

    public long durationMillis(int bpm) {
        return calc(duration, bpm);
    }

    public long durationMillis() {
        return calc(duration, bpm);
    }

    public long calc(int time, int bpm) {
        return (long) (time  * 60 * 1000 * 1000 / bpm);
    }

    public boolean contains(long timestamp) {
        return (timestamp > fromMillis(bpm) && timestamp < (fromMillis(bpm) + durationMillis(bpm)));
    }
}
