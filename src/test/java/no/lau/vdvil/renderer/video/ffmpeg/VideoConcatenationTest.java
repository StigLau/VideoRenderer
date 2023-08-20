package no.lau.vdvil.renderer.video.ffmpeg;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import no.lau.vdvil.domain.PathRef;
import no.lau.vdvil.renderer.video.TestData;
import no.lau.vdvil.snippets.FFmpegFunctions;
import no.lau.vdvil.snippets.ImprovedFFMpegFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static no.lau.vdvil.renderer.video.TestData.*;
import static no.lau.vdvil.snippets.FFmpegFunctions.combineAudioAndVideo;
import static no.lau.vdvil.snippets.ImprovedFFMpegFunctions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("IntegrationTest")
class VideoConcatenationTest {
    FFprobe ffprobe = new FFprobe(ImprovedFFMpegFunctions.ffprobeLocation());
    FFmpeg ffmpeg = new FFmpeg(ImprovedFFMpegFunctions.ffmpegLocation());
    PathRef norwayDarkLakeLocalStorage = fetch(TestData.norwayDarkLakeRemoteUrl);

    public VideoConcatenationTest() throws IOException {
    }

    @Test
    void testProbingFile() throws IOException {
        FFmpegProbeResult probeResult = ffprobe.probe(norwayDarkLakeLocalStorage.toString());

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
    }

    @Test
    void testDoingStuff() throws IOException {
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegProbeResult in = ffprobe.probe(norwayDarkLakeLocalStorage.toString());

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(in) // Or filename
                .addOutput("/tmp/output.mp4")
                .done();

        FFmpegJob job = executor.createJob(builder, new ProgressListener() {

            // Using the FFmpegProbeResult determine the duration of the input
            final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

            @Override
            public void progress(Progress progress) {
                double percentage = progress.out_time_ns / duration_ns;

                // Print out interesting information about the progress
                System.out.printf(
                        "OMFG [%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx%n",
                        percentage * 100,
                        progress.status,
                        progress.frame,
                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                        progress.fps.doubleValue(),
                        progress.speed
                );
            }
        });

        job.run();
    }

    @Test
    void testBuildingStuffWithImprovedFffmpegFunctions() throws IOException {
        PathRef snippet = snippetSplitter(fetch(norwayRemoteUrl), 56222833, 60477083);
        assertEquals(104, countNumberOfFrames(snippet));
        PathRef snippet2 = snippetSplitter(fetch(norwayRemoteUrl), 90477083, 90477083+5008300);
        assertEquals(141, countNumberOfFrames(snippet2));

        //TODO Implement
        //Path noSoundConcatenation  = FFmpegFunctions.protocolConcatVideoSnippets(ExtensionType.mp4, snippet, snippet2);
        PathRef noSoundConcatenation  = concatVideoSnippets(snippet.path(), snippet2.path());
        assertEquals(245, countNumberOfFrames(noSoundConcatenation));
        //Path combinedWithSound = Paths.get("/tmp/jalla.mp4");
        Path combinedWithSound  = combineAudioAndVideo(noSoundConcatenation, fetch(sobotaMp3RemoteUrl));

        System.out.println("Our result is at " + combinedWithSound);
        assertEquals(245, countNumberOfFrames(new PathRef(combinedWithSound)));
    }

    @Test
    void testSnippingStuffBergen() throws IOException {
        PathRef origFile = fetch(bergenRemoteUrl);
        PathRef snippet = snippetSplitter(origFile, 56222833, 60477083);
        assertEquals(102, ffmpegStreamInfo(snippet).nb_frames);
        assertEquals("144930000/6044789", ffmpegStreamInfo(origFile).avg_frame_rate.toString());
        assertEquals("24000/1001", ffmpegStreamInfo(snippet).avg_frame_rate.toString());
        //Double snippetDuration = ffmpegFormatInfo(snippet).duration;

        System.out.println("Rez: " + FFmpegFunctions.stretchSnippet(snippet, 5));
        //System.out.println("Rez: " + ImprovedFFMpegFunctions.stretchSnippet(snippet, 5));
    }
}
