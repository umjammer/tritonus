/*
 *	GSMAudioFileReaderTestCase.java
 */

/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer <Matthias.Pfisterer@web.de>
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


import	java.io.File;
import	java.io.FileInputStream;
import	java.io.InputStream;

import	java.net.URL;

import	java.util.MissingResourceException;
import	java.util.ResourceBundle;

import	javax.sound.sampled.AudioSystem;
import	javax.sound.sampled.AudioFileFormat;
import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.spi.AudioFileReader;

import	junit.framework.TestCase;

import	org.tritonus.share.sampled.AudioFileTypes;
import	org.tritonus.share.sampled.Encodings;



public class GSMAudioFileReaderTestCase
extends BaseAudioFileReaderTestCase
{
	public GSMAudioFileReaderTestCase(String strName)
	{
		super(strName);
		setResourcePrefix("gsm");
	}
}



/*** GSMAudioFileReaderTestCase.java ***/