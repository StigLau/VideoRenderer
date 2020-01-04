package no.lau.vdvil.domain;

import org.slf4j.LoggerFactory;
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
                String fileHash = byteArrayToHex(digest).toLowerCase();
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

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
