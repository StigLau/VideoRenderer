package no.lau.vdvil.snippets;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import no.lau.CommonFunctions;
import no.lau.vdvil.renderer.video.ExtensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import static no.lau.CommonFunctions.envOrDefault;
import static no.lau.vdvil.domain.utils.KompositionUtils.createTempFile;
import static no.lau.vdvil.domain.utils.KompositionUtils.createTempFiles;
import static no.lau.vdvil.snippets.FFmpegFunctions.humanReadablePeriod;

public class ImprovedFFMpegFunctions {

    static Logger logger = LoggerFactory.getLogger(ImprovedFFMpegFunctions.class);

    static FFprobe ffprobe;
    static FFmpeg ffmpeg;
    static FFmpegExecutor executor;


    public static String ffmpegLocation() {
        return envOrDefault("ffmpeg", "/usr/local/bin/ffmpeg");
    }

    public static String ffprobeLocation() {
        return envOrDefault("ffprobe", "/usr/local/bin/ffprobe");
    }

    static {
        try {
            ffmpeg = new FFmpeg(ffmpegLocation());
            ffprobe = new FFprobe(ffprobeLocation());
            executor = new FFmpegExecutor(ffmpeg, ffprobe);
        } catch (IOException e) {
            throw new RuntimeException("Could not start FFMPEG or FFProbe", e);
        }
    }

    public static Path snippetSplitter(Path downloadUrl, long timestampStart, long timestampEnd) {
        ExtensionType extensionType = ExtensionType.typify(getFileExtension(downloadUrl));
        Path destinationFile = CommonFunctions.createTempPath("snippet", extensionType);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(downloadUrl.toString())
                .addExtraArgs("-ss", humanReadablePeriod(timestampStart))
                .addExtraArgs("-t", humanReadablePeriod(timestampEnd - timestampStart))
                .addOutput(destinationFile.toString())
                .addExtraArgs("-an")
                .done();
        executor.createJob(builder).run();
        return destinationFile;
    }

    @Deprecated //Doesnt work as intended. Use FFMpegFunctions
    public static Path stretchSnippet(Path inputVideo, double targetDuration) throws IOException {
        ExtensionType extensionType = ExtensionType.typify(getFileExtension(inputVideo));
        Path destinationFile = createTempFile("stretched", extensionType);

        double snippetDuration = ffmpegFormatInfo(inputVideo).duration;
        float percentageChange = (float) (targetDuration / snippetDuration);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inputVideo.toString())
                //.addOutput(destinationFile.toString())
                .addExtraArgs("-filter:v setpts="+percentageChange+"*PTS")
                .addOutput(destinationFile.toString())
                .done();
        executor.createJob(builder).run();
        return destinationFile;
    }

    public static FFmpegFormat ffmpegFormatInfo(Path target) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(target.toString());
        FFmpegFormat format = probeResult.getFormat();
        System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
                format.filename,
                format.format_long_name,
                format.duration
        );
        return format;
    }

    public static FFmpegStream ffmpegStreamInfo(Path target) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(target.toString());
        FFmpegStream stream = probeResult.getStreams().get(0);
        System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",
                stream.codec_long_name,
                stream.width,
                stream.height
        );
        return stream;
    }

    public static String detectSceneChanges(Path target, double ratio) {
        FFmpegBuilder builder = (new FFmpegBuilder()).setInput(target.toString()).addExtraArgs(new String[]{"-filter:v ", "\"select='gt(scene,0.1)',showinfo\""}).addOutput("/tmp/heia.txt").done();
        FFmpegJob job = executor.createJob(builder);
        job.run();
        return "some results";
    }

    public static String detectSceneChangesProbe(Path target, double ratio) throws IOException {
        String file = "/tmp/komposttest/Bergen_In_Motion-Sigurd_Svidal_Randal.mp4";
        String doStuff = "ffprobe -show_frames -of compact=p=0 -f lavfi \"movie=/tmp/komposttest/Bergen_In_Motion-Sigurd_Svidal_Randal.mp4,select=gt(scene\\,0.4)";
        String doStuff3 = "ffprobe -show_frames -of compact=p=0 -f lavfi \"movie=/tmp/komposttest/Bergen_In_Motion-Sigurd_Svidal_Randal.mp4,select=gt(scene\\,0.4)\"";
        String doStuff2 = "ffmpeg -i /tmp/komposttest/Bergen_In_Motion-Sigurd_Svidal_Randal.mp4 -vf select='gt(scene\\,0.4)',scale=160:120,tile -frames:v 1 preview.png";
        String doStuff4 = "ffmpeg -i " + file + "  -filter:v \"select='gt(scene,0.4)',showinfo\" -f null - 2> /tmp/ffout";
        String doStuff5 = "ffmpeg -i " + file + " -filter:v \"select='gt(scene,0.4)',showinfo\" -f null - 2> /tmp/ffout";
        String doStuff6 = "ffmpeg -i " + file + " -filter:v \"select='gt(scene,0.4)',showinfo\" -f null ffout";
        Process asd = Runtime.getRuntime().exec(doStuff6);
        try {
            asd.waitFor();
        } catch (InterruptedException var12) {
            var12.printStackTrace();
        }

        PrintStream var10000 = System.out;
        String var10001 = new String(asd.getInputStream().readAllBytes());
        var10000.println("InputStream " + var10001);
        var10000 = System.out;
        var10001 = new String(asd.getErrorStream().readAllBytes());
        var10000.println("ErrorStream " + var10001);
        return new String(asd.getInputStream().readAllBytes());
    }

    //Docker alternative: docker run --entrypoint='ffprobe' jrottenberg/ffmpeg
    public static long countNumberOfFrames(Path destinationFile) throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(destinationFile.toString());

        FFmpegFormat format = probeResult.getFormat();
        System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
                format.filename,
                format.format_long_name,
                format.duration
        );

        FFmpegStream stream = probeResult.getStreams().get(0);
        System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx",
                stream.codec_long_name,
                stream.width,
                stream.height
        );

        return stream.nb_frames;
    }

    public static Path concatVideoSnippets(Path... snippets) throws IOException {
        if(snippets.length < 2) {
            logger.error("To few snippets as input to concatenation");
        }
        ExtensionType extensionType = ExtensionType.typify(getFileExtension(snippets[0]));
        Path target = CommonFunctions.createTempPath("video_and_audio_combination", extensionType);
        Path fileList = createTempFiles(ExtensionType.txt, snippets);
        logger.info("Storing snippet list: {}", fileList);

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(fileList.toString())
                .addExtraArgs("-f", "concat")
                .addExtraArgs("-safe", "0")
                .addOutput(target.toString())
                .addExtraArgs("-c", "copy")
                .done();
        executor.createJob(builder).run();
        return target;
    }

    @Deprecated //Not functional
    public static Path combineAudioAndVideo(Path inputVideo, Path music) throws IOException {
        ExtensionType extensionType = ExtensionType.typify(getFileExtension(inputVideo));
        Path target = createTempFile("video_and_audio_combination", extensionType);
        FFmpegBuilder builder = new FFmpegBuilder()
                .addExtraArgs("-c:v copy -c:a aac -strict experimental")
                .setInput(inputVideo + " -i " + music.toString())
                //.addInput()


//                .addExtraArgs("-c:v", "copy")
//                .addExtraArgs("-c:a", "aac")
//                .addExtraArgs("-strict", "experimental")


                .addOutput(target.toString())

                .done();
        executor.createJob(builder).run();
        return target;
    }

    public static String getFileExtension(Path Path) {
        String fileName = Path.toString();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
