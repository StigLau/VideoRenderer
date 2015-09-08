package no.lau.vdvil.download;

import no.lau.AppManagedDownload;
import no.lau.vdvil.cache.SimpleVdvilCache;
import no.lau.vdvil.cache.VdvilCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import static java.time.Instant.now;

public class YoutubeCache implements VdvilCache, SimpleVdvilCache {

    private Logger logger = LoggerFactory.getLogger(YoutubeCache.class);

    enum accepts { HTTPS }

    public boolean accepts(URL url) {
        return (url.getHost().contains("youtube.com") ||
                url.getHost().contains("youtu.be") ||
                url.getHost().contains("vimeo.com")
        );
    }

    @Deprecated
    public String mimeType(URL url) {
        return null;
    }

    /**
     * A shorthand for fetching files if they have been downloaded to disk
     * Used by testing purposes
     */
    public void fetchFromInternet(URL url, File erronousLocation) throws IOException {
        File localStorage = new File(erronousLocation.getAbsolutePath() + "/");
        //assert localStorage.mkdirs();
        Instant start = now();
        try {
            logger.info("Downloading {} into {}", url, localStorage.getAbsolutePath());

            new AppManagedDownload().run(url, new File("/tmp/"));
            //AppManagedDownload.main(new String[] { "http://vimeo.com/107469289", "/tmp/" });
        } finally {
            System.out.println("Downloading took: " + Duration.between(start, now()).toMillis() / 1000 + " seconds");
        }
    }

    public InputStream fetchAsStream(URL url) throws IOException {
        throw new RuntimeException("Can't do this");
    }
}