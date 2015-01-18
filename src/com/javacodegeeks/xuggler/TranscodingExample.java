package com.javacodegeeks.xuggler;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

public class TranscodingExample {

	private static final String inputFilename = "c:/myvideo.mp4";
	private static final String outputFilename = "c:/myvideo.flv";

	public static void main(String[] args) {

		// create a media reader
		IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
		
		// create a media writer
		IMediaWriter mediaWriter = ToolFactory.makeWriter(outputFilename, mediaReader);

		// add a writer to the reader, to create the output file
		mediaReader.addListener(mediaWriter);
		
		// create a media viewer with stats enabled
		IMediaViewer mediaViewer = ToolFactory.makeViewer(true);
		
		// add a viewer to the reader, to see the decoded media
		mediaReader.addListener(mediaViewer);

		// read and decode packets from the source file and
		// and dispatch decoded audio and video to the writer
		while (mediaReader.readPacket() == null) ;

	}

}
