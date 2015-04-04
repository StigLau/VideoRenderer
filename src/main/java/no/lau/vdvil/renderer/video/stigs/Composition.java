package no.lau.vdvil.renderer.video.stigs;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stig Lau 14/03/15.
 */
public class Composition {
    public final List<Instruction> instructions;
    long lastInstruction;
    public final int bpm;

    //Width and height of the final Kompost to be produced
    public int height;
    public int width;
    //Where the final komposition is to be stored
    public String storageLocation;

    /**
     *  @param instructions set of collector instructions to collect images for
     * @param bpm
     */
    public Composition(List<Instruction> instructions, int bpm) {
        this.instructions = instructions;
        this.bpm = bpm;
        lastInstruction = 0;
        for (Instruction instruction : instructions) {
            long thisLength = instruction.fromMillis() + instruction.durationMillis();
            if(thisLength > lastInstruction) {
                lastInstruction = thisLength;
            }
        }
    }

    public boolean isFinishedProcessing(Long timeStamp) {
        return timeStamp > lastInstruction;
    }

    public List<Instruction> isInterestedInThisPicture(long timestamp) {
        return instructions.stream().filter(instruction -> instruction.contains(timestamp))
                .collect(Collectors.toList());
    }
}
