/*
 *	MpegPlayer2.java
 */

/*
 *  Copyright (c) 1999 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
 *
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */


import	java.io.File;
import	java.io.FileInputStream;
import	java.io.IOException;
import	java.io.FileNotFoundException;

import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.AudioInputStream;
import	javax.sound.sampled.AudioSystem;
import	javax.sound.sampled.Mixer;
import	javax.sound.sampled.LineUnavailableException;
import	javax.sound.sampled.SourceDataLine;
import	javax.sound.sampled.DataLine;
import	javax.sound.sampled.Line;
import	javax.sound.sampled.LineListener;
import	javax.sound.sampled.LineEvent;
import	javax.sound.sampled.spi.FormatConversionProvider;



public class MpegPlayer2
	extends		Thread
{
	private static final boolean	DEBUG = false;

	private static final int	EXTERNAL_BUFFER_SIZE = 128000;

	private AudioInputStream	m_audioStream;
	private byte[]			m_data;
	private AudioFormat		m_format;



	public MpegPlayer2(String sFilename)
	{
		super("player thread");
		m_data = new byte[EXTERNAL_BUFFER_SIZE];
		javax.sound.sampled.spi.AudioFileReader	reader = new org.tritonus.sampled.file.MpegAudioFileReader();
		try
		{
			// m_audioStream = AudioSystem.getAudioInputStream(new File (sFilename));
			m_audioStream = reader.getAudioInputStream(new File (sFilename));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (m_audioStream == null)
		{
			System.out.println("###  m_audioStream == null");
		}
		AudioFormat	sourceFormat = m_audioStream.getFormat();
		if (DEBUG)
		{
			System.out.println("MpegPlayer2.<init>(): source format: " + sourceFormat);
		}
		AudioFormat	targetFormat = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED,
			sourceFormat.getSampleRate(),
			16,
			sourceFormat.getChannels(),
			sourceFormat.getChannels() * 2,
			sourceFormat.getSampleRate(),
			false);
		FormatConversionProvider	mpegCodec = new org.tritonus.sampled.convert.MpegFormatConversionProvider();
		if (DEBUG)
		{
			System.out.println("MpegPlayer2.<init>(): target format: " + targetFormat);
			AudioFormat.Encoding[]	encodings = mpegCodec.getTargetEncodings();
			for (int i = 0; i < encodings.length; i++)
			{
				System.out.println("enc: " + encodings[i]);
			}
			AudioFormat[]	formats = mpegCodec.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, sourceFormat);
			for (int i = 0; i < formats.length; i++)
			{
				System.out.println("format: " + formats[i]);
			}
		}

		m_audioStream = mpegCodec.getAudioInputStream(targetFormat, m_audioStream);

		// m_audioStream = AudioSystem.getAudioInputStream(targetFormat, m_audioStream);
		m_format = m_audioStream.getFormat();
		if (DEBUG)
		{
			System.out.println("MpegPlayer2.<init>(): received target format: " + m_format);
			System.out.println("MpegPlayer2.<init>(): end of contructor");
		}
	}



	public void run()
	{
		if (DEBUG)
		{
			System.out.println("MpegPlayer2.run(): called");
		}
		SourceDataLine	line = null;
		DataLine.Info	info = new DataLine.Info(
			SourceDataLine.class,
			m_format,
			AudioSystem.NOT_SPECIFIED);
		try
		{
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(m_format, line.getBufferSize());
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		line.start();
		int	nBytesRead = 0;
		while (nBytesRead != -1)
		{
			try
			{
				nBytesRead = m_audioStream.read(m_data, 0, m_data.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (DEBUG)
			{
				System.out.println("MpegPlayer2.run(): read from AudioInputStream (bytes): " + nBytesRead);
			}
			if (nBytesRead >= 0)
			{
				int	nBytesWritten = line.write(m_data, 0, nBytesRead);
				if (DEBUG)
				{
					System.out.println("MpegPlayer2.run(): written to SourceDataLine (bytes): " + nBytesWritten);
				}
			}
		}
		if (DEBUG)
		{
			System.out.println("MpegPlayer2.run(): after write loop");
		}
		//line.close();
	}



	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("MpegPlayer2: usage:");
			System.out.println("\tjava MpegPlayer2 <soundfile>");
		}
		else
		{
			Thread	thread = new MpegPlayer2(args[0]);
			thread.start();
		}
	}
}



/*** MpegPlayer2.java ***/
