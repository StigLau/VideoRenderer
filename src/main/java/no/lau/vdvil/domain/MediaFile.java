package no.lau.vdvil.domain;

import java.net.URL;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class MediaFile {
    public String id;
    public final URL fileName;
    public final Float startingOffset;
    public final String checksum;
    public final Float bpm;

    public MediaFile(URL url, Float startingOffsetInMillis, Float bpm, String checksum) {
        this.fileName = url;
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksum = checksum;
    }
}
