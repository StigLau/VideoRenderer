package no.lau.vdvil.domain;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class Track {
    public final String reference;
    public final MediaFile mediaFile;
    public final Segment[] segments;
    public transient FileRepresentation fileRepresentation;

    public Track(String reference, MediaFile mediaFile, Segment... segments) {
        this.reference = reference;
        this.mediaFile = mediaFile;
        this.segments = segments;
        //this.fileRepresentation = new CacheMetaData(mediaFile.fileName, mediaFile.checksum);
    }
}