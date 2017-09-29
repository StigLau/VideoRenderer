package no.lau.vdvil.domain;

import java.net.URL;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class MediaFile {
    public String id;
    public final URL fileName;
    public final Long startingOffset;
    public final String checksums;
    public final Float bpm;
    public String extension;

    public MediaFile(URL url, Long startingOffsetInMillis, Float bpm, String checksums) {
        this.fileName = url;
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksums = checksums;
    }
}
