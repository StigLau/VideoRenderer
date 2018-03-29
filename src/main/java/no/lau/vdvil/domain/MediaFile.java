package no.lau.vdvil.domain;

import no.lau.vdvil.renderer.video.ExtensionType;
import org.slf4j.LoggerFactory;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class MediaFile {
    public String id;
    private URL fileName;
    public final Long startingOffset;
    private String checksums;
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

    public void setFileName(URL fileName) {
        this.fileName = fileName;
    }

    public URL getFileName() {
        if(fileName == null) {
            String tempFileId = id + "_"+ bpm;
            try {
                Path file = Files.createTempFile(tempFileId, extension);
                this.fileName = file.toUri().toURL();
            } catch (IOException e) {
                throw new RuntimeException("Error creating temp file ");
            }
        }
        return fileName;
    }

    public String getChecksums() {
        if(checksums == null || checksums.isEmpty()) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(Files.readAllBytes(Paths.get(fileName.toURI())));
                String fileHash = DatatypeConverter
                        .printHexBinary(digest).toLowerCase();
                if (checksums != null && !checksums.isEmpty()) {
                    if (checksums.contains(fileHash)) {
                        // already contains fileHash
                    } else {
                        checksums += ", " + fileHash;
                    }
                } else {
                    checksums = fileHash;
                }

            } catch (NoSuchAlgorithmException | IOException | URISyntaxException e) {
                LoggerFactory.getLogger(getClass()).error("Error when creating checksum for {}", id, e);
            }
        }
        return checksums;
    }

    public static MediaFile createEmptyMediaFile(Segment segment, Float bpm, ExtensionType extension) throws IOException {
        String segmentId = segment.shortId().replace(" ", "_") + "-" + segment.start() + ":" + segment.duration() + "-" + bpm + "_bpm_";
        Path file = Files.createTempFile(segmentId, "." +extension.name());
        return new MediaFile(file, -0l, bpm, "");
    }
}
