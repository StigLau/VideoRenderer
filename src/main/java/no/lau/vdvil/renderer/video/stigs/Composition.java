package no.lau.vdvil.renderer.video.stigs;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by stiglau on 14/03/15.
 */
public class Composition {
    final List<Instruction> instructions;
    long lastInstruction;

    public Composition(List<Instruction> instructions) {
        this.instructions = instructions;
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
