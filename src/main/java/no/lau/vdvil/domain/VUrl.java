package no.lau.vdvil.domain;

import java.io.InputStream;
import java.net.URL;

/**
 * Vdvil URL to tackle protocol and download issues
 */
public interface VUrl {


    InputStream openStream() throws java.io.IOException;
    String toString();

    URL asURL();
}
