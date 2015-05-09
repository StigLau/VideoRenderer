package no.lau.vdvil.domain.utils;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stig@Lau.no 08/04/15.
 */
public class KompositionUtils {
    public static long fromMillis(Segment segment, Komposition komposition) {
        return calc(segment.start(), komposition.bpm);
    }

    public static long fromMillis(Segment segment, float bpm) {
        return calc(segment.start(), bpm);
    }


    public static long durationMillis(Segment segment, float bpm) {
        return calc(segment.duration(), bpm);
    }
    public static long durationMillis(Segment segment, Komposition komposition) {
        return calc(segment.duration(), komposition.bpm);
    }

    public static long calc(double time, double bpm) {
        return (long) (time * 60 * 1000 * 1000 / bpm);
    }

    public static boolean contains(Segment segment, Komposition komposition, long timestamp) {
        if (segment instanceof TimeStampFixedImageSampleSegment) {
            return (timestamp > segment.start() && timestamp < segment.start() + segment.duration());
        } else
            return contains(segment, komposition.bpm, timestamp);
    }

    public static boolean contains(Segment segment, float bpm, long timestamp) {
        return (timestamp > fromMillis(segment, bpm) &&
                timestamp < (fromMillis(segment, bpm) + durationMillis(segment, bpm)));
    }

    public static long lastSegment(List<Segment> segments, float bpm) {
        long endTimeStamp = 0;
        for (Segment segment : segments) {
            long thisLength = fromMillis(segment, bpm) + durationMillis(segment, bpm);
            if(thisLength > endTimeStamp) {
                endTimeStamp = thisLength;
            }
        }
        return endTimeStamp;
    }

    public static boolean isFinishedProcessing(List<Segment> segments, Long timeStamp, float bpm) {
        return timeStamp > lastSegment(segments, bpm);
    }

    public static List<Segment> isInterestedInThisPicture(List<Segment> segments, float bpm, long timestamp) {
        return segments.stream().filter(segment -> {
                    if(segment instanceof ImageSampleInstruction) {
                        return timestamp > calc(segment.start(), bpm) &&
                                timestamp < calc((segment.start() + segment.duration()),bpm);
                    }else {
                        return timestamp > segment.start() &&
                                timestamp < (segment.start() + segment.duration());
                    }
                }
        )
                .collect(Collectors.toList());
    }

    @Deprecated
    public static Stream<BufferedImage> streamImages(Komposition komposition, int frameRate) {
        ImageStore<BufferedImage> imageStore = new ImageFileStore<>(komposition, "/tmp/snaps/");
        List<BufferedImage> buff = new ArrayList<>();
        for (int frame = 0; ; frame++) {
            long timestamp = findTimeStamp(frame, frameRate, komposition);
            System.out.println("Timestamp: " + timestamp);

            List<BufferedImage> images = imageStore.getImageAt(timestamp, komposition);
            if(!images.isEmpty()) {
                System.out.println("Found images: " + images.size());
                buff.addAll(images);
            }
            if(isFinishedProcessing(komposition.segments, findTimeStamp(frame, frameRate, komposition), komposition.bpm)) {
                break;
            }
        }
        return buff.stream();
    }

    public static long findTimeStamp(int frame, int frameRate, Komposition komposition) {
        return calc(frame, komposition.bpm * frameRate);
    }
}
