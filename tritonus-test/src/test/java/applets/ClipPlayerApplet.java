/*
 *  Copyright (c) 1999 by Matthias Pfisterer
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package applets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * ClipPlayerApplet.
 */
public class ClipPlayerApplet extends JApplet implements LineListener {

    private AudioInputStream audioInputStream;
    private AudioFormat format;
    private Clip clip;

    private JPanel panel;
    private JButton loopButton;
    private JButton stopButton;

    public ClipPlayerApplet() {
    }

    public void init() {
        System.out.println("context class loader: " + Thread.currentThread().getContextClassLoader());
        System.out.println("system class loader: " + ClassLoader.getSystemClassLoader());
        String clipURL = getParameter("clipurl");
        System.out.println("URL str: " + clipURL);
        URL clipURL = null;
        try {
            clipURL = new URL(getDocumentBase(), clipURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("URL: " + clipURL);
        loadClip(clipURL);
        JPanel panel = new JPanel();
        this.getContentPane().add(panel);
        // TODO label showing the url
        loopButton = new JButton("Loop");
        loopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        });
        panel.add(loopButton);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                clip.loop(0);
            }
        });
        stopButton.setEnabled(false);
        panel.add(stopButton);
    }

    public void destroy() {
        if (clip != null) {
            clip.close();
        }
    }

    private void loadClip(URL clipURL) {
        System.out.println("setting another class loader");
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        try {
            audioInputStream = AudioSystem.getAudioInputStream(clipURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (audioInputStream != null) {
            format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format, AudioSystem.NOT_SPECIFIED);
            try {
                clip = (Clip) AudioSystem.getLine(info);
                clip.addLineListener(this);
                clip.open(audioInputStream);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // clip.loop(nLoopCount);
        } else {
            // TODO popup (also for other error conditions)
            System.out.println("can't get data from URL " + clipURL);
        }
        Thread.currentThread().setContextClassLoader(originalClassLoader);
        System.out.println("restored the original class loader");
    }

    public void update(LineEvent event) {
        System.out.println("received event: " + event);
        if (event.getType().equals(LineEvent.Type.START)) {
            loopButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
        if (event.getType().equals(LineEvent.Type.STOP)) {
            loopButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
}
