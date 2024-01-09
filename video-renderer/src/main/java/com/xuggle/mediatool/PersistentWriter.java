package com.xuggle.mediatool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Image writer as substitute for video writer for testing purposes
 */
public class PersistentWriter extends MediaWriter {

    final String outputFilePrefix;

    Logger logger = LoggerFactory.getLogger(getClass());

    String previousImageHex = "";

    PersistentWriter(String outputFilePrefix) {
        super(outputFilePrefix);
        this.outputFilePrefix = outputFilePrefix;
    }

    public static PersistentWriter create(String outputFilePrefix){
        return new PersistentWriter(outputFilePrefix);
    }


    public void encodeVideo(int streamIndex, BufferedImage image, long frameTime, TimeUnit timeUnit) {
        String folderName = outputFilePrefix + "snaps";
        try {
            Path folderPath = Paths.get(folderName);
            if(!Files.exists(folderPath)) {
                Files.createDirectory(folderPath);
            }
            String imgHex = Integer.toHexString(image.hashCode());
            //String outputFilename = folderName + "/" + frameTime + "_" + imgHex+ ".png";
            String outputFilename = folderName + "/" + frameTime + ".png";
            //String outputFilename = outputFilePrefix + "/" + frameRepresentation.getSegmentShortId() + "_" + frameTime + ".png";
            String imageReusal = imgHex.equals(previousImageHex) ? " Image reusal!" : "";
            logger.debug("Image written: {} {}", outputFilename, imageReusal);
            ImageIO.write(image, "png", new File(outputFilename));
            previousImageHex = imgHex;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
