package no.lau.ffmpeg;

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
import static no.lau.vdvil.snippets.KompositionUtils.createTempFile;
import static no.lau.vdvil.snippets.KompositionUtils.createTempFiles;
import static no.lau.vdvil.snippets.FFmpegFunctions.humanReadablePeriod;

public class ImprovedFFMpegFunctions {

    static Logger logger = LoggerFactory.getLogger(ImprovedFFMpegFunctions.class);

    public static String ffmpegLocation = envOrDefault("ffmpeg", "/usr/bin/ffmpeg");

    public static String ffprobeLocation = envOrDefault("ffprobe", "/usr/bin/ffprobe");

    public static String youtubedl = envOrDefault("yt-dlp", "/usr/local/bin/yt-dlp");


    static FFmpegExecutor createExecutor() {
        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegLocation);
            FFprobe ffprobe = new FFprobe(ffprobeLocation);
            return new FFmpegExecutor(ffmpeg, ffprobe);
        } catch (IOException e) {
            throw new RuntimeException("Could not start FFMPEG or FFProbe", e);
        }
    }


    public static Path snippetSplitter(Path downloadUrl, ExtensionType extensionType, long timestampStart, long timestampEnd) {
        Path destinationFile = CommonFunctions.createTempPath("snippet", extensionType);
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(downloadUrl.toString())
                .addExtraArgs("-ss", humanReadablePeriod(timestampStart))
                .addExtraArgs("-t", humanReadablePeriod(timestampEnd - timestampStart))
                .addOutput(destinationFile.toString())
                .addExtraArgs("-an")
                .done();
        createExecutor().createJob(builder).run();
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
        createExecutor().createJob(builder).run();
        return destinationFile;
    }

    public static FFmpegFormat ffmpegFormatInfo(Path target) throws IOException {
        FFmpegProbeResult probeResult = new FFprobe(ffprobeLocation).probe(target.toString());
        FFmpegFormat format = probeResult.getFormat();
        System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs",
                format.filename,
                format.format_long_name,
                format.duration
        );
        return format;
    }

    public static FFmpegStream ffmpegStreamInfo(Path target) throws IOException {
        FFmpegProbeResult probeResult = new FFprobe(ffprobeLocation).probe(target.toString());
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
        FFmpegJob job = createExecutor().createJob(builder);
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
        FFmpegProbeResult probeResult = new FFprobe(ffprobeLocation).probe(destinationFile.toString());

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
        createExecutor().createJob(builder).run();
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
        createExecutor().createJob(builder).run();
        return target;
    }

    public static String getFileExtension(Path Path) {
        String fileName = Path.toString();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    // FRAME ALIGNMENT SOLUTIONS - Option 2: Source Normalization

    /**
     * Option 2: Normalize video to Komposteur Master Format
     * Pre-processes all sources to consistent format for frame-perfect timing
     */
    public static Path normalizeToMasterFormat(Path sourceVideo) throws IOException {
        logger.info("Normalizing {} to Komposteur master format", sourceVideo);
        
        ExtensionType outputType = ExtensionType.mp4;
        Path normalizedFile = CommonFunctions.createTempPath("normalized_master", outputType);
        
        FFmpegBuilder builder = new FFmpegBuilder()
            .setInput(sourceVideo.toString())
            .addOutput(normalizedFile.toString())
            // Video normalization
            .setVideoFrameRate(30.0)          // Force 30fps CFR
            .addExtraArgs("-vsync", "cfr")    // Constant frame rate
            .addExtraArgs("-r", "30")         // Ensure 30fps output
            .setVideoCodec("libx264")         // Standard H.264
            .addExtraArgs("-preset", "medium") // Balance quality/speed
            .addExtraArgs("-crf", "23")       // Good quality
            .addExtraArgs("-pix_fmt", "yuv420p") // Universal compatibility
            // Audio normalization
            .setAudioCodec("aac")             // Standard AAC
            .setAudioSampleRate(48000)        // 48kHz standard
            .addExtraArgs("-ac", "2")         // Stereo
            .addExtraArgs("-b:a", "128k")     // Good quality bitrate
            // Timing precision
            .addExtraArgs("-avoid_negative_ts", "make_zero") // Normalize timestamps
            .addExtraArgs("-fflags", "+genpts") // Generate presentation timestamps
            .done();
            
        createExecutor().createJob(builder).run();
        logger.info("Normalized master format: {}", normalizedFile);
        return normalizedFile;
    }

    /**
     * Detect video frame rate for frame-accurate processing
     */
    public static double detectFrameRate(Path videoFile) throws IOException {
        FFmpegProbeResult probeResult = new FFprobe(ffprobeLocation).probe(videoFile.toString());
        FFmpegStream videoStream = probeResult.getStreams().stream()
            .filter(stream -> "video".equals(stream.codec_type))
            .findFirst()
            .orElseThrow(() -> new IOException("No video stream found in " + videoFile));
            
        // Parse frame rate (avg_frame_rate is a Fraction object)
        if (videoStream.avg_frame_rate != null) {
            return videoStream.avg_frame_rate.doubleValue();
        }
        
        logger.warn("Could not detect frame rate for {}, using 30fps default", videoFile);
        return 30.0; // Default fallback
    }

    /**
     * Validate if video is already in master format
     */
    public static boolean isMasterFormat(Path videoFile) throws IOException {
        FFmpegProbeResult probeResult = new FFprobe(ffprobeLocation).probe(videoFile.toString());
        FFmpegStream videoStream = probeResult.getStreams().stream()
            .filter(stream -> "video".equals(stream.codec_type))
            .findFirst()
            .orElse(null);
            
        if (videoStream == null) return false;
        
        double frameRate = detectFrameRate(videoFile);
        boolean is30fps = Math.abs(frameRate - 30.0) < 0.1;
        boolean isH264 = "h264".equals(videoStream.codec_name);
        boolean isYuv420p = "yuv420p".equals(videoStream.pix_fmt);
        
        logger.debug("Master format check for {}: fps={} (is30fps={}), codec={} (isH264={}), pix_fmt={} (isYuv420p={})", 
            videoFile, frameRate, is30fps, videoStream.codec_name, isH264, videoStream.pix_fmt, isYuv420p);
            
        return is30fps && isH264 && isYuv420p;
    }
}
