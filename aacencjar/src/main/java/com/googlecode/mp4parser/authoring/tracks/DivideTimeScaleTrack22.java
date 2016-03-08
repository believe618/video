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

import com.coremedia.iso.boxes.AbstractMediaHeaderBox2;
import com.coremedia.iso.boxes.CompositionTimeToSample2;
import com.coremedia.iso.boxes.SampleDependencyTypeBox2;
import com.coremedia.iso.boxes.SampleDescriptionBox2;
import com.coremedia.iso.boxes.SubSampleInformationBox2;
import com.coremedia.iso.boxes.TimeToSampleBox2;
import com.googlecode.mp4parser.authoring.Track2;
import com.googlecode.mp4parser.authoring.TrackMetaData2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Changes the timescale of a track by wrapping the track.
 */
public class DivideTimeScaleTrack22 implements Track2 {
    Track2 source;
    private int timeScaleDivisor;

    public DivideTimeScaleTrack22(Track2 source, int timeScaleDivisor) {
        this.source = source;
        this.timeScaleDivisor = timeScaleDivisor;
    }

    public SampleDescriptionBox2 getSampleDescriptionBox() {
        return source.getSampleDescriptionBox();
    }

    public List<TimeToSampleBox2.Entry> getDecodingTimeEntries() {
        return adjustTts();
    }

    public List<CompositionTimeToSample2.Entry> getCompositionTimeEntries() {
        return adjustCtts();
    }

    public long[] getSyncSamples() {
        return source.getSyncSamples();
    }

    public List<SampleDependencyTypeBox2.Entry> getSampleDependencies() {
        return source.getSampleDependencies();
    }

    public TrackMetaData2 getTrackMetaData2() {
        TrackMetaData2 trackMetaData2 = (TrackMetaData2) source.getTrackMetaData2().clone();
        trackMetaData2.setTimescale(source.getTrackMetaData2().getTimescale() / this.timeScaleDivisor);
        return trackMetaData2;
    }

    public String getHandler() {
        return source.getHandler();
    }

    public boolean isEnabled() {
        return source.isEnabled();
    }

    public boolean isInMovie() {
        return source.isInMovie();
    }

    public boolean isInPreview() {
        return source.isInPreview();
    }

    public boolean isInPoster() {
        return source.isInPoster();
    }

    public List<ByteBuffer> getSamples() {
        return source.getSamples();
    }


    List<CompositionTimeToSample2.Entry> adjustCtts() {
        List<CompositionTimeToSample2.Entry> origCtts = this.source.getCompositionTimeEntries();
        if (origCtts != null) {
            List<CompositionTimeToSample2.Entry> entries2 = new ArrayList<CompositionTimeToSample2.Entry>(origCtts.size());
            for (CompositionTimeToSample2.Entry entry : origCtts) {
                entries2.add(new CompositionTimeToSample2.Entry(entry.getCount(), entry.getOffset() / timeScaleDivisor));
            }
            return entries2;
        } else {
            return null;
        }
    }

    List<TimeToSampleBox2.Entry> adjustTts() {
        List<TimeToSampleBox2.Entry> origTts = source.getDecodingTimeEntries();
        LinkedList<TimeToSampleBox2.Entry> entries2 = new LinkedList<TimeToSampleBox2.Entry>();
        for (TimeToSampleBox2.Entry e : origTts) {
            entries2.add(new TimeToSampleBox2.Entry(e.getCount(), e.getDelta() / timeScaleDivisor));
        }
        return entries2;
    }

    public AbstractMediaHeaderBox2 getMediaHeaderBox() {
        return source.getMediaHeaderBox();
    }

    public SubSampleInformationBox2 getSubsampleInformationBox() {
        return source.getSubsampleInformationBox();
    }

    @Override
    public String toString() {
        return "MultiplyTimeScaleTrack{" +
                "source=" + source +
                '}';
    }
}
