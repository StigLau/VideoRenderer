package no.lau.vdvil.domain;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class MediaFile {
    public String id;
    private PathRef fileName;
    public final Long startingOffset;
    private String checksums;
    public final Float bpm;
    public String extension;


    public MediaFile(PathRef fileRef, Long startingOffsetInMillis, Float bpm, String checksums) {
        this.fileName = fileRef;
        this.startingOffset = startingOffsetInMillis;
        this.bpm = bpm;
        this.checksums = checksums;
    }

    public void setFileName(PathRef fileName) {
        this.fileName = fileName;
    }

    public PathRef getReference() {
        if(fileName == null) {
            String tempFileId = id + "_"+ bpm;
            try {
                Path file = Files.createTempFile(tempFileId, extension);
                this.fileName = new PathRef(file);
            } catch (IOException e) {
                throw new RuntimeException("Error creating temp file ");
            }
        }
        return fileName;
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

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String md5Checksum(PathRef url) {
        try (InputStream is = url.openStream()) {
            return md5Hex(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Shit didn't go all that well");
        }
    }

    public static String md5Hex(byte[] input) {
        try {
            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input);

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
