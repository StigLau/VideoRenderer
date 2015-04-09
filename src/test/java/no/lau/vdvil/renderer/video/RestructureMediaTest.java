package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.domain.utils.KompositionUtils;
import no.lau.vdvil.renderer.video.creator.VideoImageStitcher;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.testout.deprecated.Mp4FromPicsCreator;
import org.junit.Before;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 08/02/15.
 */
public class RestructureMediaTest {
    Track track1;

    @Before
    public void setUp() throws MalformedURLException {

        track1 = new Track("track 1", new MediaFile(new URL("http://something"), 123f, 128f, "abd"),
                new VideoStillImageSegment("segment1", 0, 4,
                        new VideoStillImageRepresentation("/tmp/snaps/40000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/320000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/560000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/800000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/1040000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/1320000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/1800000.png")
                )
                ,
                new VideoStillImageSegment("segment2", 32, 24,
                        new VideoStillImageRepresentation("/tmp/snaps/16040000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16080000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16120000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16160000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16200000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16240000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16280000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16320000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16360000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16400000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16440000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16480000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16520000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16560000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16600000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16640000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16680000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16720000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16760000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16840000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16880000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16920000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/16960000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/17000000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/17040000.png"),
                        new VideoStillImageRepresentation("/tmp/snaps/17080000.png")
                )
        );

    }

    @Test
    public void testBuildingVideo() throws MalformedURLException {
        //Se hvordan dvl'er er bygd opp - offset, bpm
        //Inneholder et sett med segments( videofilnavn, from, duration, framerate)
        //Trenger å bygge en ny Kompost
        //Instruction(id, startBpm, durationBpm
        //En annen komposition (musikkk?)
        //Text instruction
        Komposition kompost = new Komposition(128,
                new Instruction("inst1", 8, 7, track1.segments[0]),
                new Instruction("inst2.1", 16, 2, track1.segments[1]),
                new Instruction("inst2.2", 20, 2, track1.segments[1]),
                new Instruction("inst2", 24, 2, track1.segments[1]),
                new Instruction("inst3", 30, 16, track1.segments[1])
        );
        kompost.storageLocation = new MediaFile(new URL("file:///tmp/mykompost.flv"), 123f, 128f, "abap");

        String inputFile = "/tmp/CLMD-The_Stockholm_Syndrome_320";

        new VideoImageStitcher().createVideo(inputFile,  kompost);
    }

    @Test
    public void testTimestampCalculation() throws Exception {
        Komposition kompost = new Komposition(120, new Instruction("inst1", 8, 7, track1.segments[0]));

        int frame = 1;
        int frameRate = 25;
        long timestamp = KompositionUtils.findTimeStamp(frame, frameRate, kompost);
        assertEquals(20000, timestamp);
    }

    @Test
    public void buildWithXuggle() throws MalformedURLException {
        String inputFilename = "/tmp/CLMD-The_Stockholm_Syndrome_320.mp4";
        String outputFilePrefix = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";
        Komposition fetchKomposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 2)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        new VideoThumbnailsCollector().capture(inputFilename, outputFilePrefix, fetchKomposition);
        assertEquals(719, ((ImageSampleInstruction) fetchKomposition.instructions.get(0).segment).collectedImages().size());
        assertEquals(359, ((ImageSampleInstruction) fetchKomposition.instructions.get(1).segment).collectedImages().size());

        Komposition buildKomposition =  new Komposition(128,
                new Instruction("inst1", 8, 7, fetchKomposition.instructions.get(0).segment));
        buildKomposition.storageLocation = new MediaFile(new URL("file:///tmp/snaps/mixed-clmd.mp4"), 0f, 128f, "dunno yet");


        new VideoImageStitcher().createVideo(inputFilename,  buildKomposition);
    }


    @Test
    public void buildWithJCodec() throws Exception {
        String inputFilename = "/tmp/CLMD-The_Stockholm_Syndrome_320.mp4";
        String outputFilePrefix = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";
        Komposition fetchKomposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 2)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        new VideoThumbnailsCollector().capture(inputFilename, outputFilePrefix, fetchKomposition);
        assertEquals(719, ((ImageSampleInstruction) fetchKomposition.instructions.get(0).segment).collectedImages().size());
        assertEquals(359, ((ImageSampleInstruction) fetchKomposition.instructions.get(1).segment).collectedImages().size());

        Komposition buildKomposition =  new Komposition(128,
                new Instruction("inst1", 0, 32, fetchKomposition.instructions.get(0).segment));
        buildKomposition.storageLocation = new MediaFile(new URL("file:///tmp/mixed-clmd.mp4"), 0f, 128f, "dunno yet");


        Mp4FromPicsCreator.SequenceEncoder.createVideo(buildKomposition, 25);
    }
}

