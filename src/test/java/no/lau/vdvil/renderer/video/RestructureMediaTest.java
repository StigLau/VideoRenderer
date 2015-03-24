package no.lau.vdvil.renderer.video;

import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.VideoImageStitcher;
import no.lau.vdvil.renderer.video.stigs.Instruction;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 08/02/15.
 */
public class RestructureMediaTest {

    List<String> fileset1 = new ArrayList<>();
    List<String> fileset2 = new ArrayList<>();
    
    @Before
    public void setUp() {
        fileset1.add("/tmp/snaps/40000.png");
        fileset1.add("/tmp/snaps/320000.png");
        fileset1.add("/tmp/snaps/560000.png");
        fileset1.add("/tmp/snaps/800000.png");
        fileset1.add("/tmp/snaps/1040000.png");
        fileset1.add("/tmp/snaps/1320000.png");
        fileset1.add("/tmp/snaps/1800000.png");

        fileset2.add("/tmp/snaps/16040000.png");
        fileset2.add("/tmp/snaps/16080000.png");
        fileset2.add("/tmp/snaps/16120000.png");
        fileset2.add("/tmp/snaps/16160000.png");
        fileset2.add("/tmp/snaps/16200000.png");
        fileset2.add("/tmp/snaps/16240000.png");
        fileset2.add("/tmp/snaps/16280000.png");
        fileset2.add("/tmp/snaps/16320000.png");
        fileset2.add("/tmp/snaps/16360000.png");
        fileset2.add("/tmp/snaps/16400000.png");
        fileset2.add("/tmp/snaps/16440000.png");
        fileset2.add("/tmp/snaps/16480000.png");
        fileset2.add("/tmp/snaps/16520000.png");
        fileset2.add("/tmp/snaps/16560000.png");
        fileset2.add("/tmp/snaps/16600000.png");
        fileset2.add("/tmp/snaps/16640000.png");
        fileset2.add("/tmp/snaps/16680000.png");
        fileset2.add("/tmp/snaps/16720000.png");
        fileset2.add("/tmp/snaps/16760000.png");
        fileset2.add("/tmp/snaps/16800000.png");
        fileset2.add("/tmp/snaps/16840000.png");
        fileset2.add("/tmp/snaps/16880000.png");
        fileset2.add("/tmp/snaps/16920000.png");
        fileset2.add("/tmp/snaps/16960000.png");
        fileset2.add("/tmp/snaps/17000000.png");
        fileset2.add("/tmp/snaps/17040000.png");
        fileset2.add("/tmp/snaps/17080000.png");
    }
    
    @Test
    public void testBuildingVideo() {
        final String outputFilename = "/tmp/rez2.flv";
        //String inputFile = "/Users/stiglau/Downloads/CLMD-The_Stockholm_Syndrome.mp4";
        String inputFile = "/tmp/Olive-Youre_Not_Alone.webm";

        Instruction instruction1 = new Instruction("inst1", 8, 7, 128);
        instruction1.relevantFiles.addAll(fileset2);

        Instruction instruction201 = new Instruction("inst2.1", 16, 2, 128);
        instruction201.relevantFiles.addAll(fileset1);

        Instruction instruction202 = new Instruction("inst2.2", 20, 2, 128);
        instruction202.relevantFiles.addAll(fileset2);

        Instruction instruction2 = new Instruction("inst2", 24, 2, 128);
        instruction2.relevantFiles.addAll(fileset2);

        Instruction instruction3 = new Instruction("inst3", 30, 16, 128);
        instruction3.relevantFiles.addAll(fileset2);


        ImageStore imageStore = new ImageStore();
        imageStore.instructions.add(instruction1);
        imageStore.instructions.add(instruction201);
        imageStore.instructions.add(instruction202);
        imageStore.instructions.add(instruction2);
        imageStore.instructions.add(instruction3);
        new VideoImageStitcher().createVideo(inputFile, outputFilename, imageStore);
    }
}

