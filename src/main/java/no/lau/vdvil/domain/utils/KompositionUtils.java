package no.lau.vdvil.domain.utils;

import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stig@Lau.no 08/04/15.
 */
public class KompositionUtils {
    public static long fromMillis(Instruction instruction, Komposition komposition) {
        return calc(instruction.from, komposition.bpm);
    }

    public static long fromMillis(Instruction instruction, float bpm) {
        return calc(instruction.from, bpm);
    }


    public static long durationMillis(Instruction instruction, float bpm) {
        return calc(instruction.duration, bpm);
    }
    public static long durationMillis(Instruction instruction, Komposition komposition) {
        return calc(instruction.duration, komposition.bpm);
    }

    public static long calc(double time, double bpm) {
        return (long) (time * 60 * 1000 * 1000 / bpm);
    }

    public static boolean contains(Instruction instruction, Komposition komposition, long timestamp) {
        return contains(instruction, komposition.bpm, timestamp);
    }

    public static boolean contains(Instruction instruction, float bpm, long timestamp) {
        return (timestamp > fromMillis(instruction, bpm) &&
                timestamp < (fromMillis(instruction, bpm) + durationMillis(instruction, bpm)));
    }

    public static long lastInstruction(Komposition komposition) {
        long endTimeStamp = 0;
        for (Instruction instruction : komposition.instructions) {
            long thisLength = fromMillis(instruction, komposition) + durationMillis(instruction, komposition);
            if(thisLength > endTimeStamp) {
                endTimeStamp = thisLength;
            }
        }
        return endTimeStamp;
    }

    public static boolean isFinishedProcessing(Komposition komposition, Long timeStamp) {
        return timeStamp > lastInstruction(komposition);
    }

    public static List<Instruction> isInterestedInThisPicture(Komposition komposition, long timestamp) {
        return komposition.instructions.stream().filter(instruction -> contains(instruction, komposition, timestamp))
                .collect(Collectors.toList());
    }

    public static Stream<BufferedImage> streamImages(Komposition komposition, int frameRate) {
        ImageStore imageStore = new ImageFileStore(komposition, "/tmp/snaps/");
        List<BufferedImage> buff = new ArrayList<>();
        for (int frame = 0; ; frame++) {
            long timestamp = findTimeStamp(frame, frameRate, komposition);
            System.out.println("Timestamp: " + timestamp);

            List<BufferedImage> images = imageStore.getImageAt(timestamp, komposition.bpm);
            if(!images.isEmpty()) {
                System.out.println("Found images: " + images.size());
                buff.addAll(images);
            }
            if(isFinishedProcessing(komposition, findTimeStamp(frame, frameRate, komposition))) {
                break;
            }
        }
        return buff.stream();
    }

    public static long findTimeStamp(int frame, int frameRate, Komposition komposition) {
        return calc(frame, komposition.bpm * frameRate);
    }
}
