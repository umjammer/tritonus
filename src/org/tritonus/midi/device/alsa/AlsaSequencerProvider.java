/*
 *	AlsaSequencerProvider.java
 */

/*
 *  Copyright (c) 1999 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
 *
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
 *
 */


package	org.tritonus.midi.device.alsa;


import	java.util.ArrayList;
import	java.util.List;
import	java.util.Iterator;

import	javax.sound.midi.MidiDevice;
import	javax.sound.midi.spi.MidiDeviceProvider;

import	org.tritonus.lowlevel.alsa.ASequencer;




public class AlsaSequencerProvider
	extends		MidiDeviceProvider
{
	private MidiDevice.Info		m_info;



	public AlsaSequencerProvider()
	{
		m_info = new MidiDevice.Info("Tritonus ALSA sequencer", "Tritonus is free software. See http://www.tritonus.org/", "this sequencer uses the ALSA sequencer", "0.0");
	}



	public MidiDevice.Info[] getDeviceInfo()
	{
		MidiDevice.Info[]	infos = new MidiDevice.Info[1];
		infos[0] = m_info;
		return infos;
	}



	public MidiDevice getDevice(MidiDevice.Info info)
	{
		if (info.equals(m_info))
		{
			return new AlsaSequencer(m_info);
		}
		else
		{
			throw new IllegalArgumentException("no device for " + info);
		}
	}



}



/*** AlsaSequencerProvider.java ***/
