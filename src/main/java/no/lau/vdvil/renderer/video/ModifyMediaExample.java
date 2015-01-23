package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ModifyMediaExample {

	private static final String inputFilename = "/private/tmp/SPACESHIP!!!!!!!!!!!.mp4";
	private static final String outputFilename = "/tmp/rez.flv";
	private static final String[] imageFilename = new String[] {"/tmp/Uni-Kitty.jpg", "/tmp/business-kitty.jpg"};


	public static void main(String[] args) {

		// create a media reader
		IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
		
		// configure it to generate BufferImages
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

		IMediaWriter mediaWriter = ToolFactory.makeWriter(outputFilename, mediaReader);
		
		IMediaTool imageMediaTool = new StaticImageMediaTool(imageFilename);
		IMediaTool audioVolumeMediaTool = new VolumeAdjustMediaTool(0.1);
		
		// create a tool chain:
		// reader -> addStaticImage -> reduceVolume -> writer
		mediaReader.addListener(imageMediaTool);
		imageMediaTool.addListener(audioVolumeMediaTool);
		audioVolumeMediaTool.addListener(mediaWriter);
		
		while (mediaReader.readPacket() == null) ;

	}
	
	private static class StaticImageMediaTool extends MediaToolAdapter {
		
		//private List<BufferedImage> logoImages = new ArrayList<>();
		List<File> files = new ArrayList<>();
		/*
		Not used yet!
		Instruction[] ins = new Instruction[] {
				new Instruction(0, 0, 4),
				new Instruction(5, 4, 4),
				new Instruction(10, 8, 4),
				new Instruction(15, 12, 4)
		};
		*/

		public StaticImageMediaTool(String[] imageFile) {

			File folder = new File("/tmp/snaps/");
			for (File file : folder.listFiles()) {
				if (file.isFile() && file.getName().contains(".png")) {
						files.add(file);
				}
			}
		}

		int i = 0;

		class Cache {
			int lastPic = -1;
			BufferedImage cached;

			BufferedImage getImage(int id) {
				if(id == lastPic) {
					return cached;
				}else {
					try {
						lastPic = id;
						cached = ImageIO.read(files.get(id));
						return cached;
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException();
					}
				}
			}
		}

		Cache picCache = new Cache();

		@Override
		public void onVideoPicture(IVideoPictureEvent event) {
			writeImage(event, picCache.getImage(i));

			//Every second - change the pic
            if((event.getTimeStamp() > 10000 * i) ) {
				i+=1;
            }

			// call parent which will pass the video onto next tool in chain
			super.onVideoPicture(event);
			
		}

		private void writeImage(IVideoPictureEvent event, BufferedImage logoImage) {
			BufferedImage image = event.getImage();

			// get the graphics for the image
			Graphics2D g = image.createGraphics();

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

class Instruction {
	final int id;
	final int from;
	final int duration;

	public Instruction(int id, int from, int duration) {
		this.id = id;
		this.from = from;
		this.duration = duration;
	}

	public int fromMillis(int bpm) {
		return calc(from, bpm);
	}

	public int durationMillis(int bpm) {
		return calc(duration, bpm);
	}

	public int calc(int time, int bpm) {
		return time / bpm * 60 * 1000;
	}
}
