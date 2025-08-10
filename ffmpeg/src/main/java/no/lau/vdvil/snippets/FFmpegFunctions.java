package no.lau.vdvil.snippets;

import no.lau.CommonFunctions;
import no.lau.ffmpeg.ImprovedFFMpegFunctions;
import no.lau.vdvil.renderer.video.ExtensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static no.lau.vdvil.snippets.KompositionUtils.createTempFile;
import static no.lau.vdvil.snippets.KompositionUtils.createTempFiles;

public class FFmpegFunctions {
    //References
    //https://gist.github.com/protrolium/e0dbd4bb0f1a396fcb55
    //https://stackoverflow.com/questions/9913032/ffmpeg-to-extract-audio-from-video
    //https://trac.ffmpeg.org/wiki/Concatenate
    //http://ffmpeg.org/faq.html#toc-Concatenating-using-raw-audio-and-video
    //https://stackoverflow.com/questions/35675529/using-ffmpeg-how-to-do-a-scene-change-detection-with-timecode

    private static Logger logger = LoggerFactory.getLogger(FFmpegFunctions.class);

    public static String performFFMPEG(String command) throws IOException {
        //Make sure destination folder has been created!
        logger.info("Running command: '{}'", command);

        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));


        String s;
        String result = "";
        while ((s = stdInput.readLine()) != null) {
            result+= s + "\n";
            //Printing to console, to keep end user updated
            System.out.print(s + "\r");
            System.out.print(s);
        }

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        result += "\n\n\n";
        while ((s = stdError.readLine()) != null) {
            result += s + "\n";
        }

        return result ;
    }

    public static String perform(List<List<String>> paramList) throws IOException {
        return performFFMPEG(createCommand(paramList));
    }

    public static String perform(List<List<String>> paramList, Path destinationFile) throws IOException {
        Files.createDirectories(destinationFile.getParent());
        Files.deleteIfExists(destinationFile);
        String command = createCommand(paramList) + " " + destinationFile;
        return performFFMPEG(command);
    }

    private static String createCommand(List<List<String>> paramList) {
        String command = ImprovedFFMpegFunctions.ffmpegLocation;
        //String command = "docker run jrottenberg/ffmpeg";
        for (List<String> params : paramList) {
            command += " " + String.join(" ", params);
        }
        return command;
    }

    //Hours // Minutes // Seconds // Millis
    //X // 60 // 60 // 1000
    public static  String humanReadablePeriod(long totalMillis) {
        long rest = totalMillis / 1000;
        long millis = rest % 1000;
        rest = (rest - millis)/1000;
        long seconds = rest % 60 ;
        rest = rest-seconds;
        long minutes = rest = rest / 60;
        rest = rest - minutes;
        long hours = rest / 60;
        return hours + ":" + minutes + ":" + seconds + "." + millis;
    }

    public static Path concatVideoSnippets(ExtensionType extensionType, Path... snippets) throws IOException {
        Path resultingFile = createTempFile("video_and_audio_combination", extensionType);
        Path fileList = createTempFiles(extensionType, snippets);
        String concatCommand = ImprovedFFMpegFunctions.ffmpegLocation + " -f concat -safe 0 -i "+fileList.toString()+" -c copy " + resultingFile.toString();
        logger.info(performFFMPEG(concatCommand));
        return resultingFile;
    }
/**
 * https://trac.ffmpeg.org/wiki/Concatenate#protocol
 */
    public static Path protocolConcatVideoSnippets(ExtensionType extensionType, Path... snippets) throws IOException {
        Path resultingFile = createKompostTempFile("video_and_audio_combination", extensionType);
        logger.info("Concatenating files into {}", resultingFile);
        List<String> convertedSnippets = Arrays.stream(snippets)
            .map(path -> convertVideoTypes(ExtensionType.ts, path).toString())
            .collect(Collectors.toList());
        String concatCommand = ImprovedFFMpegFunctions.ffmpegLocation + " -y -i \"concat:"+String.join("|", convertedSnippets)+"\" -c copy " + resultingFile;
        logger.info(performFFMPEG(concatCommand));
        return resultingFile;
    }

    static Path convertVideoTypes(ExtensionType extensionType, Path snippet) {
        try {
            Path convertedTempFile = createKompostTempFile("", extensionType);
            String concatCommand =
                ImprovedFFMpegFunctions.ffmpegLocation + " -i " + snippet.toString() + " -c copy "
                    + convertedTempFile;
            logger.debug(performFFMPEG(concatCommand));
            return convertedTempFile;
        } catch (Exception e) {
            throw new RuntimeException("Error:"+e.getMessage() + " converting file, " + snippet + " to " + extensionType, e);
        }
    }

    public static Path combineAudioAndVideo(Path inputVideo, Path music) throws IOException {
        Path target = createTempFile("videoAudioConcat", ExtensionType.mp4);
        logger.info(performFFMPEG(ImprovedFFMpegFunctions.ffmpegLocation + " -i "+inputVideo.toString()+" -i "+music.toString()+" -c:v copy -c:a aac -strict experimental " + target.toString()));
        return target;
    }

    public static void snippetSplitter(Path downloadUrl, long timestampStart, long timestampEnd, Path destinationFile) throws IOException {
        logger.info("Performing modifications on {}", destinationFile);
        List<List<String>> props = new ArrayList<>();
        props.add(List.of("-i", downloadUrl.toString()));
        props.add(List.of("-ss", humanReadablePeriod(timestampStart)));
        props.add(List.of("-t", humanReadablePeriod(timestampEnd - timestampStart)));
        //props.add(List.of("-r", "24")); //24 frames per second
        props.add(List.of("-an")); //No Audio
        FFmpegFunctions.perform(props, destinationFile);
        logger.info("Finished converting {}", destinationFile);
    }

    public static Path stretchSnippet(Path inputVideo, double targetDuration) throws IOException {
        ExtensionType extensionType = ExtensionType.typify(ImprovedFFMpegFunctions.getFileExtension(inputVideo));
        Path destinationFile = CommonFunctions.createTempPath("stretched", extensionType);

        double snippetDuration = ImprovedFFMpegFunctions.ffmpegFormatInfo(inputVideo).duration;
        float percentageChange = (float) (targetDuration / snippetDuration);

        List<List<String>> props = new ArrayList<>();
        props.add(List.of("-i", inputVideo.toString()));
        props.add(List.of("-filter:v setpts=" + percentageChange + "*PTS"));
        //        props.add(List.of("-filter:v \"setpts="+percentageChange+"*PTS\" -vcodec copy -acodec copy -movflags " ));

        logger.info(perform(props, destinationFile));
        return destinationFile;
    }

    //Docker alternative: docker run --entrypoint='ffprobe' jrottenberg/ffmpeg
    public static long countNumberOfFrames(Path destinationFile) throws IOException {
        String additionalCommands = "-show_entries stream=nb_read_frames -count_frames ";
        String command = ImprovedFFMpegFunctions.ffprobeLocation + " -v error -select_streams v:0 -of default=noprint_wrappers=1:nokey=1 " + additionalCommands  + destinationFile.toString();
        return Long.parseLong(performFFMPEG(command).trim());
    }

    public static String fetchFrameInfo(Path destinationFile) throws IOException {
        String additionalCommands = "-show_entries stream=avg_frame_rate ";
        String command = ImprovedFFMpegFunctions.ffprobeLocation + " -v error -select_streams v:0 -of default=noprint_wrappers=1:nokey=1 " + additionalCommands + destinationFile.toString();
        return performFFMPEG(command).trim();
    }

    public static Path createKompostTempFile(String prefixIfAny, ExtensionType extensionType) throws IOException {
        Files.createDirectories(Path.of("/tmp/work"));
        Path tempfile = Files.createTempFile(Path.of("/tmp/work"), prefixIfAny, "." + extensionType.name());
        Files.deleteIfExists(tempfile);
        tempfile.toFile().deleteOnExit();
        return tempfile;
    }

    // FRAME ALIGNMENT SOLUTIONS - Three implementation options

    /**
     * Option 3: Crossfade Transitions (TESTING FIRST)
     * Adds short crossfade transitions between segments to mask timing mismatches
     */
    public static Path concatVideoSnippetsWithCrossfade(ExtensionType extensionType, double crossfadeDuration, Path... snippets) throws IOException {
        if (snippets.length < 2) {
            // Fall back to regular concat for single video
            return concatVideoSnippets(extensionType, snippets);
        }

        Path resultingFile = createKompostTempFile("crossfade_concat", extensionType);
        logger.info("Concatenating {} files with {}s crossfades into {}", snippets.length, crossfadeDuration, resultingFile);

        if (snippets.length == 2) {
            // Simple two-video crossfade
            // MUSIC VIDEOS: Video crossfade only - audio comes from separate source
            String crossfadeCommand = ImprovedFFMpegFunctions.ffmpegLocation + 
                " -i " + snippets[0] + 
                " -i " + snippets[1] + 
                " -filter_complex \"[0:v][1:v]xfade=transition=fade:duration=" + crossfadeDuration + ":offset=" + (getTotalDuration(snippets[0]) - crossfadeDuration) + "[outv]\" " +
                "-map \"[outv]\" -an " + // -an removes audio - music videos use separate audio track
                resultingFile;
            logger.info(performFFMPEG(crossfadeCommand));
        } else {
            // Multiple video crossfade (complex filter)
            StringBuilder filterComplex = new StringBuilder();
            StringBuilder inputs = new StringBuilder(ImprovedFFMpegFunctions.ffmpegLocation);
            
            // Add all input files
            for (Path snippet : snippets) {
                inputs.append(" -i ").append(snippet);
            }
            
            // Build complex filter for crossfading between all segments
            for (int i = 0; i < snippets.length - 1; i++) {
                double offset = getTotalDuration(snippets[i]) - crossfadeDuration;
                if (i == 0) {
                    filterComplex.append(String.format("[0:v][1:v]xfade=transition=fade:duration=%.3f:offset=%.3f[v01];", crossfadeDuration, offset));
                } else if (i == snippets.length - 2) {
                    filterComplex.append(String.format("[v0%d][%d:v]xfade=transition=fade:duration=%.3f:offset=%.3f[outv];", i, i + 1, crossfadeDuration, offset));
                } else {
                    filterComplex.append(String.format("[v0%d][%d:v]xfade=transition=fade:duration=%.3f:offset=%.3f[v0%d];", i, i + 1, crossfadeDuration, offset, i + 1));
                }
            }
            
            // MUSIC VIDEOS: Video-only crossfade - audio comes from separate source
            // (Audio crossfade implementation preserved in AUDIO_CROSSFADE_IMPLEMENTATION.md)
            
            String command = inputs + " -filter_complex \"" + filterComplex + "\" -map \"[outv]\" -an " + resultingFile;
            logger.info(performFFMPEG(command));
        }
        
        return resultingFile;
    }

    /**
     * Helper method to get video duration in seconds
     */
    private static double getTotalDuration(Path videoFile) {
        try {
            return ImprovedFFMpegFunctions.ffmpegFormatInfo(videoFile).duration;
        } catch (IOException e) {
            logger.warn("Could not determine duration for {}, using default", videoFile);
            return 10.0; // Default fallback
        }
    }

    /**
     * Option 1: Frame-Accurate Seeking (Highest Precision)
     * Uses exact frame calculations instead of time-based seeking
     */
    public static void frameAccurateSnippetSplitter(Path downloadUrl, long startFrame, long frameCount, double fps, Path destinationFile) throws IOException {
        logger.info("Frame-accurate splitting: {} frames starting at frame {} ({}fps)", frameCount, startFrame, fps);
        
        double startSeconds = startFrame / fps;
        double durationSeconds = frameCount / fps;
        
        List<List<String>> props = new ArrayList<>();
        props.add(List.of("-i", downloadUrl.toString()));
        props.add(List.of("-ss", String.valueOf(startSeconds)));
        props.add(List.of("-frames:v", String.valueOf(frameCount))); // Exact frame count
        props.add(List.of("-vsync", "cfr")); // Constant frame rate
        props.add(List.of("-an")); // No audio for now
        
        FFmpegFunctions.perform(props, destinationFile);
        logger.info("Frame-accurate conversion completed: {}", destinationFile);
    }

    /**
     * Convert beat timing to frame-accurate parameters
     */
    public static FrameTimingParams beatToFrameTiming(long beatStart, long beatDuration, double bpm, double videoFps) {
        double secondsStart = (beatStart * 60.0) / bpm;
        double secondsDuration = (beatDuration * 60.0) / bpm;
        
        long startFrame = Math.round(secondsStart * videoFps);
        long frameCount = Math.round(secondsDuration * videoFps);
        
        return new FrameTimingParams(startFrame, frameCount, videoFps);
    }

    /**
     * Data class for frame timing parameters
     */
    public static class FrameTimingParams {
        public final long startFrame;
        public final long frameCount;
        public final double fps;
        
        public FrameTimingParams(long startFrame, long frameCount, double fps) {
            this.startFrame = startFrame;
            this.frameCount = frameCount;
            this.fps = fps;
        }
    }
}
