/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved.  This file is offered as-is,
 * without any warranty.
 *
 */
package cc.creativecomputing.video;

import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class CameraTest {

    /**
     * @param args the command line arguments
     */
    
    private static Pipeline pipe;
    
    public static void main(String[] args) {

    	GStreamerLibrary.getInstance().init();
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
//                SimpleVideoComponent vc = new SimpleVideoComponent();
//                Bin bin = Bin.launch("autovideosrc ! videoconvert ! capsfilter caps=video/x-raw,width=1280,height=960", true);
//                pipe = new Pipeline();
//                pipe.addMany(bin, vc.getElement());
//                Pipeline.linkMany(bin, vc.getElement());           
//
//                JFrame f = new JFrame("Camera Test");
//                f.add(vc);
//                vc.setPreferredSize(new Dimension(640, 480));
//                f.pack();
//                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                
//                pipe.play();
//                f.setVisible(true);
            }
        });
    }

}