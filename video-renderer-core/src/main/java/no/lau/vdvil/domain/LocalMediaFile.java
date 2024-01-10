package no.lau.vdvil.domain;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import static no.lau.CommonFunctions.md5Checksum;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class LocalMediaFile implements MediaFile {
    public String id;
    private final URL originalUrl;
    private Path fileName;
    public final Long startingOffset;
    private String checksums;
    public final Float bpm;
    public String extension;


    public LocalMediaFile(URL originalUrl, Path fileRef, Long startingOffsetInMillis, Float bpm, String checksums) {
        this.originalUrl = originalUrl;
        this.fileName = fileRef;
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksums = checksums;
    }

    public void setFileName(Path fileName) {
        this.fileName = fileName;
    }

    @Override
    public Path getReference() {
        if(fileName == null) {
            String tempFileId = id + "_"+ bpm;
            try {
                this.fileName = Files.createTempFile(tempFileId, extension);
            } catch (IOException e) {
                throw new RuntimeException("Error creating temp file ");
            }
        }
        return fileName;
    }

    @Override
    public URL getFileName() {
        try {
            return fileName.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getChecksums() {
        if(checksums == null || checksums.isEmpty()) {
            String fileHash = md5Checksum(fileName).toLowerCase();
            if (checksums != null && !checksums.isEmpty()) {
                if (checksums.contains(fileHash)) {
                    // already contains fileHash
                } else {
                    checksums += ", " + fileHash;
                }
            } else {
                checksums = fileHash;
            }
        }
        return checksums;
    }

    public URL getOriginalUrl() {
        return originalUrl;
    }
}
