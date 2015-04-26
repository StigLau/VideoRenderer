package no.lau.vdvil.renderer.video.creator;

import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.domain.utils.KompositionUtils;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * @author Stig@Lau.no 23/03/15
 * Much copy paste from Xuggler project
 */
public class VideoImageStitcher {
    private Logger log = LoggerFactory.getLogger(VideoImageStitcher.class);

    public void createVideo(String inputFile, Komposition komposition, String outputFilePrefix) {
        IMediaReader mediaReader = ToolFactory.makeReader(inputFile);
        String outFile = komposition.storageLocation.fileName.getFile();
        IMediaWriter mediaWriter = ToolFactory.makeWriter(outFile, mediaReader);
        try {
            // configure it to generate BufferImages
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            IMediaTool imageMediaTool = new StaticImageMediaTool(komposition, outputFilePrefix);
            IMediaTool audioVolumeMediaTool = new VolumeAdjustMediaTool(0.1);

            // create a tool chain:
            // reader -> addStaticImage -> reduceVolume -> writer
            mediaReader.addListener(new StartAndStopMediaTool(komposition));
            mediaReader.addListener(imageMediaTool);
            imageMediaTool.addListener(audioVolumeMediaTool);
            audioVolumeMediaTool.addListener(mediaWriter);

            while (mediaReader.readPacket() == null) ;
        }catch(VideoExtractionFinished finished) {
            log.info("Video processing finished {}", finished.getMessage());
        }finally {
            mediaReader.close();
            //mediaWriter.close();
        }
    }

    private static class StartAndStopMediaTool extends MediaToolAdapter {

        private final Komposition komposition;

        private StartAndStopMediaTool(Komposition komposition) {
            this.komposition = komposition;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            if (KompositionUtils.isFinishedProcessing(komposition.segments, event.getTimeStamp(), komposition.bpm)) {
                throw new VideoExtractionFinished("End of compilation");
            }
            super.onVideoPicture(event);
        }


    }

    /**
     * Responsible for writing images to the videostream at specific times.
     */
    private static class StaticImageMediaTool extends MediaToolAdapter {

        final ImageStore<BufferedImage> imageStore;
        private final Komposition komposition;

        private StaticImageMediaTool(Komposition komposition, String outputFilePrefix) {
            this.komposition = komposition;
            imageStore = new ImageFileStore(komposition, outputFilePrefix);
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            for (BufferedImage foundImage : imageStore.getImageAt(event.getTimeStamp(), komposition)) {
                writeImage(event, foundImage);
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
