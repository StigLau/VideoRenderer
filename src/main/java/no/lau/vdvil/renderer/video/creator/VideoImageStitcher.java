package no.lau.vdvil.renderer.video.creator;

import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import no.lau.vdvil.domain.out.Komposition;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Stig@Lau.no 23/03/15
 * Much copy paste from Xuggler project
 */
public class VideoImageStitcher {
    public void createVideo(String inputFile, Komposition komposition) {
        IMediaReader mediaReader = ToolFactory.makeReader(inputFile);
        IMediaWriter mediaWriter = ToolFactory.makeWriter(komposition.storageLocation.fileName.getFile(), mediaReader);
        try {
            // configure it to generate BufferImages
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            IMediaTool imageMediaTool = new StaticImageMediaTool(komposition);
            IMediaTool audioVolumeMediaTool = new VolumeAdjustMediaTool(0.1);

            // create a tool chain:
            // reader -> addStaticImage -> reduceVolume -> writer
            mediaReader.addListener(imageMediaTool);
            imageMediaTool.addListener(audioVolumeMediaTool);
            audioVolumeMediaTool.addListener(mediaWriter);

            while (mediaReader.readPacket() == null) ;
        }finally {
            mediaReader.close();
            mediaWriter.close();
        }
    }

    /**
     * Responsible for writing images to the videostream at specific times.
     */
    private static class StaticImageMediaTool extends MediaToolAdapter {

        final ImageStore imageStore = new ImageStore();

        private StaticImageMediaTool(Komposition komposition) {
            System.out.println("NOT USING KOMPOSITION!");
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            try {
                for (BufferedImage foundImage : imageStore.getImageAt(event.getTimeStamp())) {
                    writeImage(event, foundImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // call parent which will pass the video onto next tool in chain
            super.onVideoPicture(event);

        }



        private void writeImage(IVideoPictureEvent event, BufferedImage logoImage) {
            BufferedImage image = event.getImage();

            // get the graphics for the image
            Graphics2D g = image.createGraphics();

            if (logoImage != null){
                Rectangle2D bounds = new Rectangle2D.Float(0, 0, logoImage.getWidth() / 2, logoImage.getHeight() / 2);

                // compute the amount to inset the time stamp and translate the image to that position
                double inset = bounds.getHeight();
                g.translate(inset, event.getImage().getHeight() - inset);

                g.setColor(Color.WHITE);
                g.fill(bounds);
                g.setColor(Color.BLACK);
                g.drawImage(logoImage, -1000, -500, null);
            }
        }
    }
}
