package no.lau;

import no.lau.vdvil.renderer.video.ExtensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommonFunctions {

    private static Logger logger = LoggerFactory.getLogger(CommonFunctions.class);

    public static String fetchEnvsOrEx(String... keys) {
        for (String key : keys) {
            String returnedResult =  System.getenv(key);
            if(returnedResult != null && !returnedResult.isEmpty()) {
                return returnedResult;
            }
        }
        String kz = String.join(",", keys);
        logger.error("Could not find System env for: {}", kz);
        throw new RuntimeException("No environment keys for " + String.join(",", kz));
    }

    public static String envOrDefault(String envName, String defaultPath) {
        String envPath = System.getenv(envName);
        if (envPath == null || envPath.isEmpty())
            return defaultPath;
        else
            return envPath;
    }

    public static Path createTempPath(String prefix, ExtensionType extensionType) {
        return createTempPath(prefix, "." + extensionType.name());
    }
    public static Path createTempPath(String prefix, String suffix) {
        try {
            Path tempfile = Files.createTempFile(prefix, suffix);
            if(Files.exists(tempfile)) {
                Files.deleteIfExists(tempfile);
            }
            return tempfile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String md5Checksum(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
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

    public static long calc(double time, double bpm) {
        return (long) (time * 60 * 1000 * 1000 / bpm);
    }
}
