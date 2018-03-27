package no.lau.vdvil.domain;

import java.net.URL;
import java.nio.file.Path;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class MediaFile {
    public String id;
    public URL fileName;
    public final Long startingOffset;
    public final String checksums;
    public final Float bpm;
    public String extension;


    public MediaFile(Path path, Long startingOffsetInMillis, Float bpm, String checksums) {
        try {
            this.fileName = path.toUri().toURL();
        } catch (Exception e) {
            System.out.println("Fsck " +  e.getMessage());
        }
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksums = checksums;
    }

    public MediaFile(URL url, Long startingOffsetInMillis, Float bpm, String checksums) {
        this.fileName = url;
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksums = checksums;
    }
}
