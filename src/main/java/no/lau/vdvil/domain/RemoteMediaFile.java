package no.lau.vdvil.domain;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import static no.lau.vdvil.domain.UrlHandler.urlCreator;

public class RemoteMediaFile implements MediaFile {
    public String id;
    private URL fileName;
    public final Long startingOffset;
    private String checksums;
    public final Float bpm;
    public String extension;


    public RemoteMediaFile(URL fileRef, Long startingOffsetInMillis, Float bpm, String checksums) {
        this.fileName = fileRef;
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksums = checksums;
    }

    public void setFileName(URL fileName) {
        this.fileName = fileName;
    }

    public Path getReference() {
        if(fileName == null) {
            String tempFileId = id + "_"+ bpm;
            try {
                this.fileName = urlCreator(Files.createTempFile(tempFileId, extension));
            } catch (IOException e) {
                throw new RuntimeException("Error creating temp file ");
            }
        }
        return Path.of(fileName.getFile());
    }

    @Override
    public URL getFileName() {
        return fileName;
    }

    public String getChecksums() {
        return checksums;
    }
}