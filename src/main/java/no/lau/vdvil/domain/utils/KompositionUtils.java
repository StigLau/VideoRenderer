package no.lau.vdvil.domain.utils;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stig@Lau.no 08/04/15.
 */
public class KompositionUtils {
    public static Logger logger = LoggerFactory.getLogger(KompositionUtils.class);

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
            logger.info("Timestamp: " + timestamp);

            List<BufferedImage> images = imageStore.getImageAt(timestamp, komposition);
            if(!images.isEmpty()) {
                logger.info("Found images: " + images.size());
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

    public static void printImageRepresentationImages(KompositionPlanner planner) {
        Map<Long, List<FrameRepresentation>> reps = new TreeMap<>();
        for (FrameRepresentation rep : ((SuperPlan) planner.buildPlan()).getFrameRepresentations()) {
            if(reps.containsKey(rep.timestamp)) {
                reps.get(rep.timestamp).add(rep);
            } else {
                List<FrameRepresentation> gots  = new ArrayList<>();
                gots.add(rep);
                reps.put(rep.timestamp, gots);
            }
        }
        for (Long tstamp : reps.keySet()) {
            logger.info(tstamp + " \t" + reps.get(tstamp).size());
            if(reps.get(tstamp).size() > 1) {
                logger.info("Crossfading");
            }
        }
    }

    public static Path fetchRemoteFile(String basePathLocalStorage, String remoteUrl) throws IOException {
        return fetchRemoteFile(basePathLocalStorage, remoteUrl, false);
    }

    public static Path fetchRemoteFile(String basePathLocalStorage, String remoteUrl, boolean force) throws IOException {
        String fileName = Paths.get(remoteUrl).getFileName().toString();
        Path resultingLocalPath = Paths.get(basePathLocalStorage + fileName);

        if (!Files.exists(resultingLocalPath) || force) {
            if (!Files.exists(resultingLocalPath.getParent())) {
                Files.createDirectories(resultingLocalPath.getParent());
            }
            OutputStream outputStream = null;
            logger.info("Downloading {} to {}", remoteUrl, resultingLocalPath);

            File asFile = new File(resultingLocalPath.toString());
            try (InputStream inputStream = new URL(remoteUrl).openStream()) {
                outputStream = new FileOutputStream(asFile);

                byte[] bytes = new byte[1024];
                int read;
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (Exception e) {
                logger.error("Fetching remote file failed. Deleting local cached copy", resultingLocalPath, e);
                Files.delete(resultingLocalPath);
            } finally {
                outputStream.flush();
            }
        }
        return resultingLocalPath;
    }

}
