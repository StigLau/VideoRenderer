package no.lau.vdvil.renderer.video;

import java.io.IOException;
import java.nio.file.Path;
import static no.lau.vdvil.domain.utils.KompositionUtils.fetchRemoteFile;

public class TestData {

    public static String norwayRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4";
    public static String sobotaMp3RemoteUrl = "https://s3.amazonaws.com/dvl-test-music/music/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";
    public static String norwayDarkLakeRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure_Dark_lake_69375000___74000000.mp4";
    public static String norwayFlowerFjordRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure_Flower_fjord_35500000___46250000.mp4";

    public static Path fetch(String remoteUrl) {
        try {
            return fetchRemoteFile("/tmp/komposttest/", remoteUrl);
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch " + remoteUrl, e);
        }
    }
}
