package no.lau.vdvil.download;

import com.github.axet.vget.AppManagedDownload;
import no.lau.vdvil.cache.SimpleVdvilCache;
import no.lau.vdvil.cache.VdvilCache;
import java.io.*;
import java.net.URL;

public class YoutubeCache implements VdvilCache, SimpleVdvilCache {

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
    public void fetchFromInternet(URL url, File localStorage) throws IOException {
        AppManagedDownload e = new AppManagedDownload();
        e.run(url, localStorage);
    }

    public InputStream fetchAsStream(URL url) throws IOException {
        throw new RuntimeException("Can't do this");
    }
}