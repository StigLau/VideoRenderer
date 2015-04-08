package no.lau.vdvil.domain.out;

import no.lau.vdvil.domain.Segment;


/**
 * @author Stig@Lau.no 07/04/15.
 */
public class Instruction {
    public final String id;
    public final double from;
    public final double duration;
    public final Segment segment;

    public Instruction(String id, long from, long duration, Segment segment) {
        this.id = id;
        this.from = from;
        this.duration = duration;
        this.segment = segment;
    }
}
