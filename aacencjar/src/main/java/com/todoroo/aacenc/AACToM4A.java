package com.todoroo.aacenc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import android.content.Context;

import com.coremedia.iso.IsoFile2;
import com.googlecode.mp4parser.authoring.Movie2;
import com.googlecode.mp4parser.authoring.Track2;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder22;
import com.googlecode.mp4parser.authoring.tracks.AACTrack22Impl2;

public class AACToM4A {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public void convert(Context context, String infile, String outfile) throws IOException {
        AACToM4A.context = context;

        InputStream input = new FileInputStream(infile);

        PushbackInputStream pbi = new PushbackInputStream(input, 100);

        System.err.println("well you got " + input.available());
        Movie2 movie2 = new Movie2();

        Track2 audioTrack2 = new AACTrack22Impl2(pbi);
        movie2.addTrack(audioTrack2);

        IsoFile2 out = new DefaultMp4Builder22().build(movie2);
        FileOutputStream output = new FileOutputStream(outfile);
        out.getBox(output.getChannel());
        output.close();
    }

}
