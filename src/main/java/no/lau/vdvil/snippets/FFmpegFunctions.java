package no.lau.vdvil.snippets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    public static void concatVideoSnippets(Path resultingFile, Path... snippets) throws IOException {
        Path fileList = Files.createTempFile("fileList", "." + "txt");
        String strung = "";
        for (Path snipppet : snippets) {
            strung += "file '" + snipppet.toString() + "'\n";
        }
        Files.write(fileList, strung.getBytes());
        Files.deleteIfExists(resultingFile);
        //String concatCommand = "docker run jrottenberg/ffmpeg -f concat -safe 0 -i "+fileList.toString()+" -c copy " + resultingFile.toString();
        String concatCommand = "ffmpeg -f concat -safe 0 -i "+fileList.toString()+" -c copy " + resultingFile.toString();

        logger.info(performFFMPEG(concatCommand));
    }

    public static String combineAudioAndVideo(Path inputVideo, Path music, Path target) throws IOException {
        if(Files.exists(target)) {
            Files.delete(target);
        }
        return performFFMPEG("ffmpeg -i "+inputVideo.toString()+" -i "+music.toString()+" -c:v copy -c:a aac -strict experimental " + target.toString());
    }

    //Docker alternative: docker run --entrypoint='ffprobe' jrottenberg/ffmpeg
    public static long countNumberOfFrames(Path destinationFile) throws IOException {
        String command = "ffprobe -v error -count_frames -select_streams v:0 -show_entries stream=nb_read_frames -of default=nokey=1:noprint_wrappers=1 " + destinationFile.toString();
        return Long.parseLong(performFFMPEG(command).trim());
    }
}
