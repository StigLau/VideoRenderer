package no.lau.vdvil.domain.out;

import no.lau.vdvil.domain.MediaFile;

import java.util.Arrays;
import java.util.List;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class Komposition {
    public final List<Instruction> instructions;
    long lastInstruction;
    public final int bpm;

    //Width and height of the final Kompost to be produced
    public int height;
    public int width;
    //Where the final komposition is to be stored
    public MediaFile storageLocation;

    public Komposition(int bpm, Instruction... instructions) {
        this.instructions = Arrays.asList(instructions);
        this.bpm = bpm;
        lastInstruction = 0;
    }
}