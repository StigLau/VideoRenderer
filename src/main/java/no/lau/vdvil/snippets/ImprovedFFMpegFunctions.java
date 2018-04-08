package no.lau.vdvil.snippets;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import no.lau.vdvil.renderer.video.ExtensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import static no.lau.vdvil.domain.utils.KompositionUtils.createTempFile;
import static no.lau.vdvil.domain.utils.KompositionUtils.createTempFiles;
import static no.lau.vdvil.snippets.FFmpegFunctions.humanReadablePeriod;

public class ImprovedFFMpegFunctions {

    static Logger logger = LoggerFactory.getLogger(ImprovedFFMpegFunctions.class);

    static FFprobe ffprobe;
    static FFmpeg ffmpeg;
    static FFmpegExecutor executor;


    static {
        try {
            ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
            ffprobe = new FFprobe("/usr/local/bin/ffprobe");
            executor = new FFmpegExecutor(ffmpeg, ffprobe);
        } catch (IOException e) {
            throw new RuntimeException("Could not start FFMPEG or FFProbe", e);
        }
    }

    public static Path snippetSplitter(Path downloadUrl, long timestampStart, long timestampEnd) throws IOException {
        ExtensionType extensionType = ExtensionType.typify(getFileExtension(downloadUrl));
        Path destinationFile = createTempFile("snippet", extensionType);
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

    public static FFmpegFormat getFormatInfo(Path target) throws IOException {
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

    public static Path concatVideoSnippets(ExtensionType extensionType, Path... snippets) throws IOException {
        Path target = createTempFile("video_and_audio_combination", extensionType);
        Path fileList = createTempFiles(extensionType, snippets);

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
    public static Path combineAudioAndVideo(Path inputVideo, Path music, ExtensionType extensionType) throws IOException {
        Path target = createTempFile("video_and_audio_combination", extensionType);
        FFmpegBuilder builder = new FFmpegBuilder()
                .addExtraArgs("-c:v copy -c:a aac -strict experimental")
                .setInput(inputVideo.toString() + " -i " + music.toString())
                //.addInput()


//                .addExtraArgs("-c:v", "copy")
//                .addExtraArgs("-c:a", "aac")
//                .addExtraArgs("-strict", "experimental")


                .addOutput(target.toString())

                .done();
        executor.createJob(builder).run();
        return target;
    }

    private static String getFileExtension(Path path) {
        String fileName = path.toFile().getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
