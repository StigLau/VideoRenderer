package no.lau.vdvil.renderer.video;

class Instruction {
    final int id;
    final int from;
    final int duration;

    public Instruction(int id, int from, int duration) {
        this.id = id;
        this.from = from;
        this.duration = duration;
    }

    public long fromMillis(int bpm) {
        return calc(from, bpm);
    }

    public long durationMillis(int bpm) {
        return calc(duration, bpm);
    }

    public long calc(int time, int bpm) {
        return (long) (time  * 60 * 1000 * 1000 / bpm);
    }
}
