package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.VideoImageStitcher;
import org.junit.Before;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;

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
        kompost.storageLocation = new MediaFile(new URL("file:///tmp/myKompost.mp4"), 123f, 128f, "abap");

        String inputFile = "/tmp/output320.mp4";

        new VideoImageStitcher().createVideo(inputFile,  kompost);
    }
}

