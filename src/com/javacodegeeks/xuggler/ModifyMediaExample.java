package com.javacodegeeks.xuggler;

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
		
		private List<BufferedImage> logoImages = new ArrayList<>();

		public StaticImageMediaTool(String[] imageFile) {
			
			try {
				for (String image : imageFile) {
					logoImages.add(ImageIO.read(new File(image)));
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not open file");
			}
			
		}

		@Override
		public void onVideoPicture(IVideoPictureEvent event) {

            //10 sekunder framerate25 * hertz44,1 * 1000

            if((event.getTimeStamp() > 10000000) && (event.getTimeStamp() < 20000000)) {
            //if((event.getTimeStamp() > 10 * 25 * 44.1 * 1000) && (event.getTimeStamp() < 20 * 25 * 44.1 * 1000)) {
				writeImage(event, logoImages.get(0));
            }
			if((event.getTimeStamp() > 20000000) && (event.getTimeStamp() < 30000000)) {
				//if((event.getTimeStamp() > 10 * 25 * 44.1 * 1000) && (event.getTimeStamp() < 20 * 25 * 44.1 * 1000)) {
				writeImage(event, logoImages.get(1));
			}
			
			// call parent which will pass the video onto next tool in chain
			super.onVideoPicture(event);
			
		}

		private void writeImage(IVideoPictureEvent event, BufferedImage logoImage) {
			BufferedImage image = event.getImage();

			// get the graphics for the image
			Graphics2D g = image.createGraphics();

			Rectangle2D bounds = new Rectangle2D.Float(20, 20, logoImage.getWidth() / 2, logoImage.getHeight() / 2);

			// compute the amount to inset the time stamp and translate the image to that position
			double inset = bounds.getHeight();
			g.translate(inset, event.getImage().getHeight() - inset);

			g.setColor(Color.WHITE);
			g.fill(bounds);
			g.setColor(Color.BLACK);
			g.drawImage(logoImage, 0, 0, null);
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
