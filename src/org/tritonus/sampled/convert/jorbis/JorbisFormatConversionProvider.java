/*
 *	JorbisFormatConversionProvider.java
 */

/*
 *  Copyright (c) 1999 - 2003 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

package	org.tritonus.sampled.convert.jorbis;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.TDebug;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;
import org.tritonus.share.sampled.convert.TSimpleFormatConversionProvider;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.Encodings;

import com.jcraft.jogg.SyncState;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Block;



/**	Pure-java decoder for ogg vorbis streams.
	The FormatConversionProvider uses the pure-java
	ogg vorbis decoder from www.jcraft.com/jorbis/.

	@author Matthias Pfisterer
*/
public class JorbisFormatConversionProvider
	extends		TEncodingFormatConversionProvider
{
	// only used as abbreviation
	private static final AudioFormat.Encoding	VORBIS = Encodings.getEncoding("VORBIS");
	private static final AudioFormat.Encoding	PCM_SIGNED = Encodings.getEncoding("PCM_SIGNED");


	private static final AudioFormat[]	INPUT_FORMATS =
	{
		// mono
		// TODO: mechanism to make the double specification with
		// different endianess obsolete.
		new AudioFormat(VORBIS, -1.0F, -1, 1, -1, -1.0F, false),
		new AudioFormat(VORBIS, -1.0F, -1, 1, -1, -1.0F, true),
		// stereo
		new AudioFormat(VORBIS, -1.0F, -1, 2, -1, -1.0F, false),
		new AudioFormat(VORBIS, -1.0F, -1, 2, -1, -1.0F, true),
		// TODO: other channel configurations
	};


	private static final AudioFormat[]	OUTPUT_FORMATS =
	{
		// mono, 16 bit signed
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
		// stereo, 16 bit signed
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
		new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),
		// TODO: other channel configurations
	};




	/**	Constructor.
	 */
	// TODO: check interaction with base class
	public JorbisFormatConversionProvider()
	{
		super(Arrays.asList(INPUT_FORMATS),
		      Arrays.asList(OUTPUT_FORMATS)/*,
						     true, // new behaviour
						     false*/); // bidirectional .. constants UNIDIR../BIDIR..?
	}



	public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream)
	{
		/** The AudioInputStream to return.
		 */
		AudioInputStream	convertedAudioInputStream = null;

		if (TDebug.TraceAudioConverter)
		{
			TDebug.out("JorbisFormatConversionProvider.getAudioInputStream():");
			TDebug.out("checking if conversion supported");
			TDebug.out("from: " + audioInputStream.getFormat());
			TDebug.out("to: " + targetFormat);
		}

		// what is this ???
		targetFormat=getDefaultTargetFormat(targetFormat, audioInputStream.getFormat());
		if (isConversionSupported(targetFormat,
					  audioInputStream.getFormat()))
		{
			if (TDebug.TraceAudioConverter)
			{
				TDebug.out("JorbisFormatConversionProvider.getAudioInputStream():");
				TDebug.out("conversion supported; trying to create DecodedJorbisAudioInputStream");
			}
			convertedAudioInputStream = new
				DecodedJorbisAudioInputStream(
					targetFormat,
					audioInputStream);
		}
		else
		{
			if (TDebug.TraceAudioConverter)
			{
				TDebug.out("JorbisFormatConversionProvider.getAudioInputStream():");
				TDebug.out("conversion not supported; throwing IllegalArgumentException");
			}
			throw new IllegalArgumentException("conversion not supported");
		}
		return convertedAudioInputStream;
	}



	// TODO: recheck !!
	protected AudioFormat getDefaultTargetFormat(AudioFormat targetFormat, AudioFormat sourceFormat)
	{
		if (TDebug.TraceAudioConverter) { TDebug.out("JorbisFormatConversionProvider.getDefaultTargetFormat(): target format: " + targetFormat); }
		if (TDebug.TraceAudioConverter) { TDebug.out("JorbisFormatConversionProvider.getDefaultTargetFormat(): source format: " + sourceFormat); }
		AudioFormat	newTargetFormat = null;
		// return first of the matching formats
		// pre-condition: the predefined target formats (FORMATS2) must be well-defined !
		Iterator iterator=getCollectionTargetFormats().iterator();
		while (iterator.hasNext())
		{
			AudioFormat format = (AudioFormat) iterator.next();
			if (AudioFormats.matches(targetFormat, format))
			{
				newTargetFormat = format;
			}
		}
		if (newTargetFormat == null)
		{
			throw new IllegalArgumentException("conversion not supported");
		}
		if (TDebug.TraceAudioConverter) { TDebug.out("JorbisFormatConversionProvider.getDefaultTargetFormat(): new target format: " + newTargetFormat); }
		// hacked together...
		// ... only works for PCM target encoding ...
		newTargetFormat = new AudioFormat(targetFormat.getEncoding(),
						  sourceFormat.getSampleRate(),
						  newTargetFormat.getSampleSizeInBits(),
						  newTargetFormat.getChannels(),
						  newTargetFormat.getFrameSize(),
						  sourceFormat.getSampleRate(),
						  newTargetFormat.isBigEndian());
		if (TDebug.TraceAudioConverter) { TDebug.out("JorbisFormatConversionProvider.getDefaultTargetFormat(): really new target format: " + newTargetFormat); }
		return newTargetFormat;
	}
		


	/**	AudioInputStream returned on decoding of ogg vorbis.
		An instance of this class is returned if you call
		AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream)
		to decode an ogg/vorbis stream. This class contains the logic
		of maintaining buffers and calling the decoder.
	*/
	/* Class should be private, but is public due to a bug (?) in the
	   aspectj compiler. */
	/*private*/public static class DecodedJorbisAudioInputStream
	extends TAsynchronousFilteredAudioInputStream
	{
  		private static final int	BUFFER_MULTIPLE = 4;
  		private static final int	BUFFER_SIZE = BUFFER_MULTIPLE * 256 * 2;
  		private static final int	CONVSIZE = BUFFER_SIZE * 2;

		private InputStream		m_oggBitStream = null;

		// Ogg structures
		private SyncState		m_oggSyncState = null;
  		private StreamState		m_oggStreamState = null;
  		private Page			m_oggPage = null;
  		private Packet			m_oggPacket = null;

		// Vorbis structures
  		private Info			m_vorbisInfo = null;
  		private Comment			m_vorbisComment = null;
  		private DspState		m_vorbisDspState = null;
		// actually is an ogg structure
  		private Block			m_vorbisBlock = null;

  		private List			m_songComments = new ArrayList();
		// is altered lated in a dubious way
  		private int			convsize = -1; // BUFFER_SIZE * 2;
		// TODO: further checking
  		private byte[]			convbuffer = new byte[CONVSIZE];
		private float[][][]		_pcmf = null;
		private int[]			_index = null;

		private int			loopid = 1;
		private int			eos = 0;
		private boolean			streamStillHasData = true;

/*
  if (TDebug.TraceAudioConverter) { TDebug.out("TAsynchronousFilteredAudioInputStream.<init>(): begin"); }
  getCircularBuffer() = new TCircularBuffer(
  nBufferSize,
  false,	// blocking read
  true,	// blocking write
  this);	// trigger
  if (TDebug.TraceAudioConverter) { TDebug.out("TAsynchronousFilteredAudioInputStream.<init>(): end"); }
*/

		/**
		 * Constructor.
		 */
		public DecodedJorbisAudioInputStream(AudioFormat outputFormat, AudioInputStream bitStream)
		{
			super(outputFormat, AudioSystem.NOT_SPECIFIED);
			if (TDebug.TraceAudioConverter) { TDebug.out("DecodedJorbisAudioInputStream.<init>(): begin"); }
			m_oggBitStream = bitStream;
			loopid = 1;
			init_jorbis();
			if (TDebug.TraceAudioConverter) { TDebug.out("DecodedJorbisAudioInputStream.<init>(): end"); }
		}



		/**
		 * Initializes all the jOrbis and jOgg vars that are used for song playback.
		 */
		private void init_jorbis()
		{
			m_oggSyncState = new SyncState();
			m_oggStreamState = new StreamState();
			m_oggPage = new Page();
			m_oggPacket = new Packet();

			m_vorbisInfo = new Info();
			m_vorbisComment = new Comment();
			m_vorbisDspState = new DspState();
			m_vorbisBlock = new Block(m_vorbisDspState);

			m_oggSyncState.init();
		}


		/**
		 * Main loop.
		 */
		public void execute()
		{
			if (TDebug.TraceAudioConverter) TDebug.out(">DecodedJorbisAudioInputStream.execute(): begin");
			int bytes = 0;
			// This code was developed by the jCraft group, slightly modifed by jOggPlayer developer
			// and adapted by E.B from JavaZOOM to suit to JavaSound SPI.
			// loop 1
			if (streamStillHasData == false)
			{
				m_oggSyncState.clear();
				if (TDebug.TraceAudioConverter) System.out.println("Done Song.");
				try
				{
					if (m_oggBitStream != null)
					{
						m_oggBitStream.close();
					}
					getCircularBuffer().close();
				}
				catch (Exception e)
				{
					if (TDebug.TraceAllExceptions) { TDebug.out(e); }
				}
				if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
				return;
			}
			if (loopid == 1)
			{
				if (TDebug.TraceAudioConverter) TDebug.out("loop1 [reading headers]");
				eos = 0;
				// Headers (+ Comments).
				try
				{
					readHeaders();
				}
				catch (IOException e)
				{
					// TDebug.out(e);
					streamStillHasData = false;
					if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
					return;
				}
				loopid = 2;
			}
			// loop 2
			if (eos == 1)
			{
				loopid = 1;
				m_oggStreamState.clear();
				m_vorbisBlock.clear();
				m_vorbisDspState.clear();
				m_vorbisInfo.clear();
				if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
				return;
			}
			if (TDebug.TraceAudioConverter) TDebug.out("loop2 [Decoding]");
			// loop 3
			int result = 0;
			try
			{
				readOggPage();
			}
			catch (IOException e)
			{
				streamStillHasData = false;
				eos = 1;
				if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
				return;
			}
			if (TDebug.TraceAudioConverter) TDebug.out("page header length: " + m_oggPage.header_len);
			if (TDebug.TraceAudioConverter) TDebug.out("page body length: " + m_oggPage.body_len);
			m_oggStreamState.pagein(m_oggPage);
			// Decoding !
			while (true)
			{
				result = m_oggStreamState.packetout(m_oggPacket);
				if (TDebug.TraceAudioConverter) TDebug.out("packetout(): " + result);
				if (TDebug.TraceAudioConverter) TDebug.out("packet no: " + m_oggPacket.packetno);
				if (TDebug.TraceAudioConverter) TDebug.out("packet length: " + m_oggPacket.bytes);
				if (result == 0) // need more data
				{
					if (TDebug.TraceAudioConverter) TDebug.out("leaving packet decoding loop (needs another page)");
					break;
				}
				else if (result == -1)
				{ // missing or corrupt data at this page position
					// DO NOTHING
				}
				else
				{
					decodeDataPacket();
				}
			}
			if (m_oggPage.eos() != 0)
			{
				eos = 1;
			}
			loopid = 3;
			if (loopid == 3)
			{
				if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
				return;
			}

			if (loopid == 2)
			{
				if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
				return;
			}

			m_oggStreamState.clear();
			m_vorbisBlock.clear();
			m_vorbisDspState.clear();
			m_vorbisInfo.clear();
			if (TDebug.TraceAudioConverter) TDebug.out("<DecodedJorbisAudioInputStream.execute(): end");
		}



		/** Read and process all three vorbis headers.
		    @return true if successful, false otherwise.
		*/
		private boolean readHeaders()
			throws IOException
		{
			boolean	bSuccess;
			bSuccess = readIdentificationHeader();
			if (! bSuccess)
			{
				return false;
			}
			bSuccess = readCommentAndCodebookHeaders();
			if (! bSuccess)
			{
				return false;
			}
			processComments();
			setupVorbisStructures();
			return true;
		}



		/**
		   @return true if successful, false otherwise.
		*/
		private boolean readIdentificationHeader()
			throws IOException
		{
			int nIndex = m_oggSyncState.buffer(BUFFER_SIZE);
			int nBytes = readFromStream(m_oggSyncState.data, nIndex, BUFFER_SIZE);
			if (nBytes == -1)
			{
				return false;
			}
			m_oggSyncState.wrote(nBytes);
			if (m_oggSyncState.pageout(m_oggPage) != 1)
			{
				return false;
			}
			m_oggStreamState.init(m_oggPage.serialno());
			m_vorbisInfo.init();
			m_vorbisComment.init();
			if (m_oggStreamState.pagein(m_oggPage) < 0)
			{
				// error; stream version mismatch perhaps
				return false;
			}
			if (m_oggStreamState.packetout(m_oggPacket) != 1)
			{
				// no page? must not be vorbis
				return false;
			}
			if (m_vorbisInfo.synthesis_headerin(m_vorbisComment, m_oggPacket) < 0)
			{
				// error case; not a vorbis header
				return false;
			}
			return true;
		}



		/**
		   @return true if successful, false otherwise.
		*/
		private boolean readCommentAndCodebookHeaders()
			throws IOException
		{
			for (int i = 0; i < 2; i++)
			{
				readOggPacket();
				m_vorbisInfo.synthesis_headerin(m_vorbisComment, m_oggPacket);
			}
			return true;
		}



		/**
		 */
		private void processComments()
		{
			byte[][] ptr = m_vorbisComment.user_comments;
			String currComment = "";
			m_songComments.clear();
			for (int j = 0; j < ptr.length; j++)
			{
				if (ptr[j] == null)
				{
					break;
				}
				currComment = (new String(ptr[j], 0, ptr[j].length - 1)).trim();
				m_songComments.add(currComment);
				if (currComment.toUpperCase().startsWith("ARTIST"))
				{
					String artistLabelValue = currComment.substring(7);
				}
				else if (currComment.toUpperCase().startsWith("TITLE"))
				{
					String titleLabelValue = currComment.substring(6);
					String miniDragLabel = currComment.substring(6);
				}
				if (TDebug.TraceAudioConverter) TDebug.out("Comment: " + currComment);
			}
			currComment = "Bitstream: " + m_vorbisInfo.channels + " channel," + m_vorbisInfo.rate + "Hz";
			m_songComments.add(currComment);
			if (TDebug.TraceAudioConverter) TDebug.out(currComment);
			if (TDebug.TraceAudioConverter) currComment = "Encoded by: " + new String(m_vorbisComment.vendor, 0, m_vorbisComment.vendor.length - 1);
			m_songComments.add(currComment);
			if (TDebug.TraceAudioConverter) TDebug.out(currComment);
		}



		/** Setup structures needed for vorbis decoding.
		    Precondition: m_vorbisInfo has to be initialized completely
		    (i.e. all three headers are read).
		*/
		private void setupVorbisStructures()
		{
			convsize = BUFFER_SIZE / m_vorbisInfo.channels;
			m_vorbisDspState.synthesis_init(m_vorbisInfo);
			m_vorbisBlock.init(m_vorbisDspState);
			_pcmf = new float[1][][];
			_index = new int[m_vorbisInfo.channels];
		}



		/** Decode a packet of vorbis data.
		    This method assumes that a packet is available in
		    {@link #m_oggPacket m_oggPacket}. The content of this
		    packet is run through the decoder. The resulting
		    PCM data are written to the circular buffer.
		*/
		private void decodeDataPacket()
		{
			int samples;
			if (m_vorbisBlock.synthesis(m_oggPacket) == 0)
			{ // test for success!
				m_vorbisDspState.synthesis_blockin(m_vorbisBlock);
			}
			while ((samples = m_vorbisDspState.synthesis_pcmout(_pcmf, _index)) > 0)
			{
				float[][] pcmf = _pcmf[0];
				int bout = (samples < convsize ? samples : convsize);
				// convert floats to signed ints and
				// interleave
				for (int nChannel = 0; nChannel < m_vorbisInfo.channels; nChannel++)
				{
					int pointer = nChannel * getSampleSizeInBytes();
					int mono = _index[nChannel];
					for (int j = 0; j < bout; j++)
					{
						float fVal = pcmf[nChannel][mono + j];
						clipAndWriteSample(fVal, pointer);
						pointer += getFrameSize();
					}
				}
				getCircularBuffer().write(convbuffer, 0, getFrameSize() * bout);
				m_vorbisDspState.synthesis_read(bout);
			}
		}


		/** Scale and clip the sample and write it to convbuffer.
		 */
		private void clipAndWriteSample(float fSample, int nPointer)
		{
			int nSample;
			// TODO: check if clipping is necessary
			if (fSample > 1.0F)
			{
				fSample = 1.0F;
			}
			if (fSample < -1.0F)
			{
				fSample = -1.0F;
			}
			switch (getFormat().getSampleSizeInBits())
			{
			case 16:
				nSample = (int) (fSample * 32767.0F);
				if (isBigEndian())
				{
					convbuffer[nPointer++] = (byte) (nSample >> 8);
					convbuffer[nPointer] = (byte) (nSample & 0xFF);
				}
				else
				{
					convbuffer[nPointer++] = (byte) (nSample & 0xFF);
					convbuffer[nPointer] = (byte) (nSample >> 8);
				}
				break;

			case 24:
				nSample = (int) (fSample * 8388607.0F);
				if (isBigEndian())
				{
					convbuffer[nPointer++] = (byte) (nSample >> 16);
					convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
					convbuffer[nPointer] = (byte) (nSample & 0xFF);
				}
				else
				{
					convbuffer[nPointer++] = (byte) (nSample & 0xFF);
					convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
					convbuffer[nPointer] = (byte) (nSample >> 16);
				}
				break;

			case 32:
				nSample = (int) (fSample * 2147483647.0F);
				if (isBigEndian())
				{
					convbuffer[nPointer++] = (byte) (nSample >> 24);
					convbuffer[nPointer++] = (byte) ((nSample >>> 16) & 0xFF);
					convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
					convbuffer[nPointer] = (byte) (nSample & 0xFF);
				}
				else
				{
					convbuffer[nPointer++] = (byte) (nSample & 0xFF);
					convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
					convbuffer[nPointer++] = (byte) ((nSample >>> 16) & 0xFF);
					convbuffer[nPointer] = (byte) (nSample >> 24);
				}
				break;
			}
		}



		/** Read an ogg packet.
		    This method does everything necessary to read an ogg
		    packet. If needed, it calls
		    {@link #readOggPage readOggPage()}, which, in turn, may
		    read more data from the stream. The resulting packet is
		    placed in {@link #m_oggPacket m_oggPacket} (for which the
		    reference is not altered; is has to be initialized before).
		*/
		private void readOggPacket()
			throws IOException
		{
			while (true)
			{
				int result = m_oggStreamState.packetout(m_oggPacket);
				if (result == 1)
				{
					return;
				}
				if (result == -1)
				{
					throw new IOException("Corrupt secondary header.  Exiting.");
				}
				readOggPage();
				m_oggStreamState.pagein(m_oggPage);
			}
		}



		/** Read an ogg page.
		    This method does everything necessary to read an ogg
		    page. If needed, it reads more data from the stream.
		    The resulting page is
		    placed in {@link #m_oggPage m_oggPage} (for which the
		    reference is not altered; is has to be initialized before).

		    Note: this method doesn't deliver the page read to a
		    StreamState object (which assembles pages to packets).
		    This has to be done by the caller.
		*/
		private void readOggPage()
			throws IOException
		{
			while (true)
			{
				int result = m_oggSyncState.pageout(m_oggPage);
				if (result == 1)
				{
					return;
				}
				// we need more data from the stream
				int nIndex = m_oggSyncState.buffer(BUFFER_SIZE);
				// TODO: call stream.read() directly
				int nBytes = readFromStream(m_oggSyncState.data, nIndex, BUFFER_SIZE);
				// TODO: This clause should become obsolete; readFromStream() should
				// propagate exceptions directly.
				if (nBytes == -1)
				{
					throw new EOFException();
				}
				m_oggSyncState.wrote(nBytes);
			}
		}



		/** Read raw data from to ogg bitstream.
		    Reads from  {@ #m_oggBitStream m_oggBitStream} a
		    specified number of bytes into a buffer, starting
		    at a specified buffer index.

		    @param buffer the where the read data should be put into. Its length has to be at least nStart + nLength.
		    @param nStart
		    @param nLength the number of bytes to read
		    @return the number of bytes read (maybe 0) or
		    -1 if there is no more data in the stream.
		*/
		private int readFromStream(byte[] buffer, int nStart, int nLength)
			throws IOException
		{
			return m_oggBitStream.read(buffer, nStart, nLength);
		}



		/**
		 */
		private int getSampleSizeInBytes()
		{
			return getFormat().getFrameSize() / getFormat().getChannels();
		}



		/** .
		    @return .
		*/
		private int getFrameSize()
		{
			return getFormat().getFrameSize();
		}



		/** Returns if this stream (the decoded one) is big endian.
		    @return true if this stream is big endian.
		*/
		private boolean isBigEndian()
		{
			return getFormat().isBigEndian();
		}



		/**
		 *
		 */
		public void close() throws IOException
		{
			super.close();
			m_oggBitStream.close();
		}

	}
}



/*** JorbisFormatConversionProvider.java ***/