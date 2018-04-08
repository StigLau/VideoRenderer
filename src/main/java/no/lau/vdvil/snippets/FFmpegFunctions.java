package no.lau.vdvil.snippets;

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
import static no.lau.vdvil.domain.utils.KompositionUtils.createTempFile;
import static no.lau.vdvil.domain.utils.KompositionUtils.createTempFiles;

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
        logger.info("Running command: {}", command);

        Process p = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));


        String s;
        String result = "";
        while ((s = stdInput.readLine()) != null) {
            result+= s + "\n";
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
        Files.createDirectories((destinationFile.getParent()));
        Files.deleteIfExists(destinationFile);
        String command = createCommand(paramList) + " " + destinationFile.toString();
        return performFFMPEG(command);
    }

    private static String createCommand(List<List<String>> paramList) {
        String command = "ffmpeg";
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
        String concatCommand = "ffmpeg -f concat -safe 0 -i "+fileList.toString()+" -c copy " + resultingFile.toString();
        logger.info(performFFMPEG(concatCommand));
        return resultingFile;
    }

    public static Path combineAudioAndVideo(Path inputVideo, Path music) throws IOException {
        Path target = createTempFile("videoAudioConcat", ExtensionType.mp4);
        Files.delete(target);
        logger.info(performFFMPEG("ffmpeg -i "+inputVideo.toString()+" -i "+music.toString()+" -c:v copy -c:a aac -strict experimental " + target.toString()));
        return target;
    }

    public static void snippetSplitter(String downloadUrl, long timestampStart, long timestampEnd, Path destinationFile) throws IOException {
        logger.info("Performing modifications on {}", destinationFile);
        List<List<String>> props = new ArrayList<>();
        props.add(Arrays.asList("-i", downloadUrl));
        props.add(Arrays.asList("-ss", humanReadablePeriod(timestampStart)));
        props.add(Arrays.asList("-t", humanReadablePeriod(timestampEnd - timestampStart)));
        //props.add(Arrays.asList("-r", "24")); //24 frames per second
        props.add(Arrays.asList("-an")); //No Audio
        FFmpegFunctions.perform(props, destinationFile);
        logger.info("Finished converting {}", destinationFile);
    }

    //Docker alternative: docker run --entrypoint='ffprobe' jrottenberg/ffmpeg
    public static long countNumberOfFrames(Path destinationFile) throws IOException {
        String additionalCommands = "-show_entries stream=nb_read_frames -count_frames ";
        String command = "ffprobe -v error -select_streams v:0 -of default=noprint_wrappers=1:nokey=1 " + additionalCommands  + destinationFile.toString();
        return Long.parseLong(performFFMPEG(command).trim());
    }

    public static String fetchFrameInfo(Path destinationFile) throws IOException {
        String additionalCommands = "-show_entries stream=avg_frame_rate ";
        String command = "ffprobe -v error -select_streams v:0 -of default=noprint_wrappers=1:nokey=1 " + additionalCommands + destinationFile.toString();
        return performFFMPEG(command).trim();
    }
}
