package no.lau.vdvil.renderer.video.download;

import no.lau.AppManagedDownload;
import no.lau.vdvil.cache.FileRepresentation;
import no.lau.vdvil.cache.MyStore;
import no.lau.vdvil.download.YoutubeCache;
import org.junit.Test;
import no.lau.vdvil.cache.Store;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import static java.time.Instant.now;

/**
 * Created by stiglau on 15/06/15.
 */
public class DownloadTest {

    Store store = new MyStore();
    @Test
    public void testDownloading() throws IOException {
        //FileRepresentation gotsMe = store.cache(ClassLoader.getSystemResource("test.mp3"));
        //FileRepresentation gotsMe = store.cache(new URL("http://kpro09.googlecode.com/svn/test-files/holden-nothing-93_returning_mix.mp3"));
        store.addTransport(new YoutubeCache());
        FileRepresentation gotsMe = store.cache(new URL("https://www.youtube.com/watch?v=3vozw18HiwM"), "A Checksum");
        System.out.println("gotsMe.localStorage().getAbsolutePath() = " + gotsMe.localStorage().getAbsolutePath());
        //FileRepresentation gotsMe = store.cache(new URL("https://vimeo.com/35725762"), "A Checksum");
    }

    @Test
    public void workingDownload() {
        Instant start = now();
        try {
            new AppManagedDownload().run(new URL("https://www.youtube.com/watch?v=ZSs0PBuLeSc"), new File("/tmp/"));
            //AppManagedDownload.main(new String[] { "http://vimeo.com/107469289", "/tmp/" });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Downloading took: " + Duration.between(start, now()).toMillis() / 1000 + " seconds");
        }
    }
}
