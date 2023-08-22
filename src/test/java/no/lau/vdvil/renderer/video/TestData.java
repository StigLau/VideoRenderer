package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.LocalMediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import static no.lau.vdvil.domain.UrlHandler.urlCreator;
import static no.lau.vdvil.domain.utils.KompositionUtils.fetchRemoteFile;

public class TestData {

    public static URL norwayRemoteUrl = urlCreator("https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4");
    public static URL bergenRemoteUrl = urlCreator("https://s3.amazonaws.com/dvl-test-music/test-data/Bergen_In_Motion-Sigurd_Svidal_Randal/Bergen_In_Motion-Sigurd_Svidal_Randal.mp4");
    public static URL sobotaMp3RemoteUrl = urlCreator("https://s3.amazonaws.com/dvl-test-music/music/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3");
    public static URL norwayDarkLakeRemoteUrl = urlCreator("https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure_Dark_lake_69375000___74000000.mp4");
    public static URL norwayFlowerFjordRemoteUrl = urlCreator("https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure_Flower_fjord_35500000___46250000.mp4");

    public static Path fetch(URL remoteUrl) {
        try {
            return fetchRemoteFile("/tmp/komposttest/", remoteUrl);
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch " + remoteUrl, e);
        }
    }

    public static Komposition fetchNorwayDVL() {
        return new Komposition(125,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 19750000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Norway showing", 30166667, 34541667, 4),
                new TimeStampFixedImageSampleSegment("Flower fjord", 35500000, 46250000, 8),
                new TimeStampFixedImageSampleSegment("Slide Blue mountain top lake", 47000000, 57000000, 8),
                new TimeStampFixedImageSampleSegment("Fjord foss", 58541667, 62875000, 8),
                new TimeStampFixedImageSampleSegment("Fjord like river", 64250000, 68125000, 8),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74000000, 8),
                new TimeStampFixedImageSampleSegment("Mountain range", 74750000, 79500000, 8),
                new TimeStampFixedImageSampleSegment("Omnious fjord Lightbrake", 79750000, 87750000, 8),
                new TimeStampFixedImageSampleSegment("Boat village panorama", 88125000, 96125000, 8),
                new TimeStampFixedImageSampleSegment("Village street", 96500000, 101750000, 8),
                new TimeStampFixedImageSampleSegment("Seaside houses Panorama", 102000000, 107125000, 8),
                new TimeStampFixedImageSampleSegment("Bergen movement", 107500000, 112750000, 8)
        )
                .applyStorageLocation(new LocalMediaFile(fetch(norwayRemoteUrl), 0L, -1f, "abc"));
    }
    /*
        fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34034000, 34993292, 15),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(theSwingVideo, 0l, 120F, "abc123");
*/


    public static Komposition norwayBaseKomposition() {
        return new Komposition(125,
                new VideoStillImageSegment("Dark lake", 0, 4),
                new VideoStillImageSegment("Dark lake", 4, 4).revert(),
                new VideoStillImageSegment("Dark lake", 8, 8).revert(),

                new VideoStillImageSegment("Purple Mountains Clouds", 16, 8),
                new VideoStillImageSegment("Purple Mountains Clouds", 24, 7).revert(), //Forsøplet
                new VideoStillImageSegment("Norway showing", 31, 1),
                new VideoStillImageSegment("Slide Blue mountain top lake", 32, 8), //Forsøplet
                new VideoStillImageSegment("Flower fjord", 40, 4),
                new VideoStillImageSegment("Slide Blue mountain top lake", 44, 8).revert(),
                new VideoStillImageSegment("Fjord like river", 52, 11),
                //new VideoStillImageSegment("Fjord foss", 56, 4),//Forsøplet Has parts of Slide Blue!!

                //new VideoStillImageSegment("Besseggen", 60, 3),
                new VideoStillImageSegment("Norway showing", 63, 1).revert(),
                new VideoStillImageSegment("Swing into bridge", 64, 8),
                new VideoStillImageSegment("Swing through bridge with mountain smile", 72, 8),
                new VideoStillImageSegment("Smile girl, smile", 80, 8),
                new VideoStillImageSegment("Swing out from bridge", 88, 12)
        );
    }
}
