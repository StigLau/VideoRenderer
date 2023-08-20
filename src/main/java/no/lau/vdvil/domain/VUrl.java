package no.lau.vdvil.domain;

import java.io.InputStream;

/**
 * Vdvil URL to tackle protocol and download issues
 */
public interface VUrl {


    InputStream openStream() throws java.io.IOException;
    String toString();
}
