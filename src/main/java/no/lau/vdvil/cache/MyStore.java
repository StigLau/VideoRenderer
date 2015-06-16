package no.lau.vdvil.cache;

import no.lau.vdvil.download.YoutubeCache;

/**
 * Created by stiglau on 15/06/15.
 */
public class MyStore extends Store {
    public MyStore(){
        super.addTransport(new YoutubeCache());
    }
}
