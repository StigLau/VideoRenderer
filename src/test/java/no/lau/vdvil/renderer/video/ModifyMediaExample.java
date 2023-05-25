package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import static no.lau.vdvil.domain.utils.KompositionUtils.fromMillis;

/**
 * @author Stig Lau 08/02/15.
 */
public class ModifyMediaExample {

	private static final String outputFilename = "/tmp/rez2.mp4";

	//Main functionality
	public static void main(String[] args) throws IOException {

		//String inputFile = ModifyMediaExample.class.getClassLoader().getResource("video/5sec-test.flv").getFile();
		String inputFile = "/tmp/CLMD-The_Stockholm_Syndrome.mp4";

        Komposition komposition = new Komposition(128,
                new ImageSampleInstruction("0", 0, 1, 1),
                new ImageSampleInstruction("2", 1, 3, 1),
                new ImageSampleInstruction("5", 4, 4, 1),
                new ImageSampleInstruction("10", 8, 4, 1),
                new ImageSampleInstruction("15", 12, 4, 1),
                new ImageSampleInstruction("18", 13, 1, 1),
                new ImageSampleInstruction("20", 24, 1, 1)
        );


		// create a media reader
		IMediaReader mediaReader = ToolFactory.makeReader(inputFile);

		// configure it to generate BufferImages
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

		IMediaWriter mediaWriter = ToolFactory.makeWriter(outputFilename, mediaReader);

		IMediaTool imageMediaTool = new StaticImageMediaTool(komposition);
		IMediaTool audioVolumeMediaTool = new VolumeAdjustMediaTool(0.1);

		// create a tool chain:
		// reader -> addStaticImage -> reduceVolume -> writer
		mediaReader.addListener(imageMediaTool);
		imageMediaTool.addListener(audioVolumeMediaTool);
		audioVolumeMediaTool.addListener(mediaWriter);

		while (mediaReader.readPacket() == null) ;

	}

	/**
	 * Responsible for writing images to the videostream at specific times.
	 */
	private static class StaticImageMediaTool extends MediaToolAdapter {

		//private List<BufferedImage> logoImages = new ArrayList<>();
		Map<String, Path> files = new HashMap<>();
        Komposition komposition;

		public StaticImageMediaTool(Komposition komposition) throws IOException {
			this.komposition = komposition;
			Path folder = Path.of(ClassLoader.getSystemResource("images").getPath());
			Files.list(folder)
					.filter(Files::exists)
					.filter(path -> path.getFileName().toString().contains(".png"))
					.forEach(path -> files.put(path.toString(), path)
					);
		}


		class Cache {
			String lastPic = "-1";
			BufferedImage cached;

			BufferedImage getImage(String id) {
				if(id.equals(lastPic)) {
					return cached;
				}else {
					try {
						lastPic = id;
						cached = ImageIO.read(Files.newInputStream(files.get(id)));
						return cached;
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException();
					}
				}
			}
		}

		Cache picCache = new Cache();

		Segment last;

		public void onVideoPicture(IVideoPictureEvent event) {

			Segment found = lastAfter(event.getTimeStamp());
			if(found == null)
				found = last;
			else
				last = found;

			System.out.println(found.id() + " at time: " + event.getTimeStamp());
			writeImage(event, picCache.getImage(found.id()));

			// call parent which will pass the video onto next tool in chain
			super.onVideoPicture(event);

		}

		Segment lastAfter(long time) {
            for (Segment segment : komposition.segments) {
                if(segment != null) {
                    System.out.println("instruction.fromMillis(120) = " + fromMillis(segment, komposition));
                }
				if(segment != null && time >= fromMillis(segment, komposition)) {
					return segment;
				}
			}
			return null;
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

	private static class VolumeAdjustMediaTool extends MediaToolAdapter {

		// the amount to adjust the volume by
		private double mVolume;

		public VolumeAdjustMediaTool(double volume) {
			mVolume = volume;
		}

		@Override
		public void onAudioSamples(IAudioSamplesEvent event) {

			// get the raw audio bytes and adjust it's value
			ShortBuffer buffer = event.getAudioSamples().getByteBuffer().asShortBuffer();

			for (int i = 0; i < buffer.limit(); ++i) {
				buffer.put(i, (short) (buffer.get(i) * mVolume));
			}

			// call parent which will pass the audio onto next tool in chain
			super.onAudioSamples(event);

		}

	}

}
