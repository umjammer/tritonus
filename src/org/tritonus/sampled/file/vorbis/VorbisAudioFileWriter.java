/*
 *	VorbisAudioFileWriter.java
 */

/*
 *  Copyright (c) 2000 by Florian Bomers <florian@bome.com>
 *  Copyright (c) 2000 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
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

package	org.tritonus.sampled.file.vorbis;


import	java.util.Arrays;

import	javax.sound.sampled.AudioFileFormat;
import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.AudioSystem;

import	org.tritonus.share.TDebug;
import	org.tritonus.share.sampled.Encodings;
import	org.tritonus.share.sampled.AudioFileTypes;
import	org.tritonus.share.sampled.file.THeaderlessAudioFileWriter;



/**	Class for writing Vorbis streams
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class VorbisAudioFileWriter
	extends THeaderlessAudioFileWriter
{
	private static final AudioFileFormat.Type[]	FILE_TYPES =
	{
		AudioFileTypes.getType("Vorbis", "ogg")
	};

	private static final AudioFormat[]	AUDIO_FORMATS =
	{
		new AudioFormat(Encodings.getEncoding("OGG_VORBIS"), ALL, ALL, ALL, ALL, ALL, false),
		new AudioFormat(Encodings.getEncoding("OGG_VORBIS"), ALL, ALL, ALL, ALL, ALL, true),
	};



	public VorbisAudioFileWriter()
	{
		super(Arrays.asList(FILE_TYPES),
		      Arrays.asList(AUDIO_FORMATS));
	}
}



/*** VorbisAudioFileWriter.java ***/