package no.lau.vdvil.renderer.video.download;

import no.lau.vdvil.cache.FileRepresentation;
import no.lau.vdvil.cache.MyStore;
import no.lau.vdvil.download.YoutubeCache;
import org.junit.Test;
import no.lau.vdvil.cache.Store;
import java.io.IOException;
import java.net.URL;

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
        //FileRepresentation gotsMe = store.cache(new URL("https://vimeo.com/35725762"), "A Checksum");
    }
}
