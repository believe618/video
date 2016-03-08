/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.sampleentry.TextSampleEntry2;
import com.googlecode.mp4parser.authoring.AbstractTrack22;
import com.googlecode.mp4parser.authoring.TrackMetaData2;
import com.googlecode.mp4parser.boxes.threegpp26245.FontTableBox2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class TextTrack22Impl2 extends AbstractTrack22 {
    TrackMetaData2 trackMetaData2 = new TrackMetaData2();
    SampleDescriptionBox2 sampleDescriptionBox;
    List<Line> subs = new LinkedList<Line>();

    public List<Line> getSubs() {
        return subs;
    }

    public TextTrack22Impl2() {
        sampleDescriptionBox = new SampleDescriptionBox2();
        TextSampleEntry2 tx3g = new TextSampleEntry2("tx3g");
        tx3g.setStyleRecord(new TextSampleEntry2.StyleRecord());
        tx3g.setBoxRecord(new TextSampleEntry2.BoxRecord());
        sampleDescriptionBox.addBox(tx3g);

        FontTableBox2 ftab = new FontTableBox2();
        ftab.setEntries(Collections.singletonList(new FontTableBox2.FontRecord(1, "Serif")));

        tx3g.addBox(ftab);


        trackMetaData2.setCreationTime(new Date());
        trackMetaData2.setModificationTime(new Date());
        trackMetaData2.setTimescale(1000); // Text tracks use millieseconds


    }


    public List<ByteBuffer> getSamples() {
        List<ByteBuffer> samples = new LinkedList<ByteBuffer>();
        long lastEnd = 0;
        for (Line sub : subs) {
            long silentTime = sub.from - lastEnd;
            if (silentTime > 0) {
                samples.add(ByteBuffer.wrap(new byte[]{0, 0}));
            } else if (silentTime < 0) {
                throw new Error("Subtitle display times may not intersect");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.writeShort(sub.text.getBytes("UTF-8").length);
                dos.write(sub.text.getBytes("UTF-8"));
                dos.close();
            } catch (IOException e) {
                throw new Error("VM is broken. Does not support UTF-8");
            }
            samples.add(ByteBuffer.wrap(baos.toByteArray()));
            lastEnd = sub.to;
        }
        return samples;
    }

    public SampleDescriptionBox2 getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public List<TimeToSampleBox2.Entry> getDecodingTimeEntries() {
        List<TimeToSampleBox2.Entry> stts = new LinkedList<TimeToSampleBox2.Entry>();
        long lastEnd = 0;
        for (Line sub : subs) {
            long silentTime = sub.from - lastEnd;
            if (silentTime > 0) {
                stts.add(new TimeToSampleBox2.Entry(1, silentTime));
            } else if (silentTime < 0) {
                throw new Error("Subtitle display times may not intersect");
            }
            stts.add(new TimeToSampleBox2.Entry(1, sub.to - sub.from));
            lastEnd = sub.to;
        }
        return stts;
    }

    public List<CompositionTimeToSample2.Entry> getCompositionTimeEntries() {
        return null;
    }

    public long[] getSyncSamples() {
        return null;
    }

    public List<SampleDependencyTypeBox2.Entry> getSampleDependencies() {
        return null;
    }

    public TrackMetaData2 getTrackMetaData2() {
        return trackMetaData2;
    }

    public String getHandler() {
        return "text";
    }


    public static class Line {
        long from;
        long to;
        String text;


        public Line(long from, long to, String text) {
            this.from = from;
            this.to = to;
            this.text = text;
        }

        public long getFrom() {
            return from;
        }

        public String getText() {
            return text;
        }

        public long getTo() {
            return to;
        }
    }

    public AbstractMediaHeaderBox2 getMediaHeaderBox() {
        return new NullMediaHeaderBox2();
    }

    public SubSampleInformationBox2 getSubsampleInformationBox() {
        return null;
    }
}
