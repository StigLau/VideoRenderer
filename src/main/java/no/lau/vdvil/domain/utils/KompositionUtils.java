package no.lau.vdvil.domain.utils;

import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stig@Lau.no 08/04/15.
 */
public class KompositionUtils {
    public static long fromMillis(Instruction instruction, Komposition komposition) {
        return calc(instruction.from, komposition.bpm);
    }


    public static long durationMillis(Instruction instruction, Komposition komposition) {
        return calc(instruction.duration, komposition.bpm);
    }

    public static long calc(double time, double bpm) {
        return (long) (time * 60 * 1000 * 1000 / bpm);
    }

    public static boolean contains(Instruction instruction, Komposition komposition, long timestamp) {
        return (timestamp > fromMillis(instruction, komposition) &&
                timestamp < (fromMillis(instruction, komposition) + durationMillis(instruction, komposition)));
    }

    public static long lastInstruction(Komposition komposition) {
        long lastInstruction = 0;
        for (Instruction instruction : komposition.instructions) {
            long thisLength = fromMillis(instruction, komposition) + durationMillis(instruction, komposition);
            if(thisLength > lastInstruction) {
                lastInstruction = thisLength;
            }
        }
        return lastInstruction;
    }

    public static boolean isFinishedProcessing(Komposition komposition, Long timeStamp) {
        return timeStamp > lastInstruction(komposition);
    }

    public static List<Instruction> isInterestedInThisPicture(Komposition komposition, long timestamp) {
        return komposition.instructions.stream().filter(instruction -> contains(instruction, komposition, timestamp))
                .collect(Collectors.toList());
    }
}
