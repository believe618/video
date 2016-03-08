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
import com.googlecode.mp4parser.authoring.AbstractTrack22;
import com.googlecode.mp4parser.authoring.Track2;
import com.googlecode.mp4parser.authoring.TrackMetaData2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Appends two or more <code>Tracks</code> of the same type. No only that the type must be equal
 * also the decoder settings must be the same.
 */
public class AppendTrack222 extends AbstractTrack22 {
    Track2[] track2s;

    public AppendTrack222(Track2... track2s) throws IOException {
        this.track2s = track2s;
        byte[] referenceSampleDescriptionBox = null;
        for (Track2 track2 : track2s) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            track2.getSampleDescriptionBox().getBox(Channels.newChannel(baos));
            if (referenceSampleDescriptionBox == null) {
                referenceSampleDescriptionBox = baos.toByteArray();
            } else if (!Arrays.equals(referenceSampleDescriptionBox, baos.toByteArray())) {
                throw new IOException("Cannot append " + track2 + " to " + track2s[0] + " since their Sample Description Boxes differ");
            }
        }
    }

    public List<ByteBuffer> getSamples() {
        ArrayList<ByteBuffer> lists = new ArrayList<ByteBuffer>();

        for (Track2 track2 : track2s) {
            lists.addAll(track2.getSamples());
        }

        return lists;
    }

    public SampleDescriptionBox2 getSampleDescriptionBox() {
        return track2s[0].getSampleDescriptionBox();
    }

    public List<TimeToSampleBox2.Entry> getDecodingTimeEntries() {
        if (track2s[0].getDecodingTimeEntries() != null && !track2s[0].getDecodingTimeEntries().isEmpty()) {
            List<long[]> lists = new LinkedList<long[]>();
            for (Track2 track2 : track2s) {
                lists.add(TimeToSampleBox2.blowupTimeToSamples(track2.getDecodingTimeEntries()));
            }

            LinkedList<TimeToSampleBox2.Entry> returnDecodingEntries = new LinkedList<TimeToSampleBox2.Entry>();
            for (long[] list : lists) {
                for (long nuDecodingTime : list) {
                    if (returnDecodingEntries.isEmpty() || returnDecodingEntries.getLast().getDelta() != nuDecodingTime) {
                        TimeToSampleBox2.Entry e = new TimeToSampleBox2.Entry(1, nuDecodingTime);
                        returnDecodingEntries.add(e);
                    } else {
                        TimeToSampleBox2.Entry e = returnDecodingEntries.getLast();
                        e.setCount(e.getCount() + 1);
                    }
                }
            }
            return returnDecodingEntries;
        } else {
            return null;
        }
    }

    public List<CompositionTimeToSample2.Entry> getCompositionTimeEntries() {
        if (track2s[0].getCompositionTimeEntries() != null && !track2s[0].getCompositionTimeEntries().isEmpty()) {
            List<int[]> lists = new LinkedList<int[]>();
            for (Track2 track2 : track2s) {
                lists.add(CompositionTimeToSample2.blowupCompositionTimes(track2.getCompositionTimeEntries()));
            }
            LinkedList<CompositionTimeToSample2.Entry> compositionTimeEntries = new LinkedList<CompositionTimeToSample2.Entry>();
            for (int[] list : lists) {
                for (int compositionTime : list) {
                    if (compositionTimeEntries.isEmpty() || compositionTimeEntries.getLast().getOffset() != compositionTime) {
                        CompositionTimeToSample2.Entry e = new CompositionTimeToSample2.Entry(1, compositionTime);
                        compositionTimeEntries.add(e);
                    } else {
                        CompositionTimeToSample2.Entry e = compositionTimeEntries.getLast();
                        e.setCount(e.getCount() + 1);
                    }
                }
            }
            return compositionTimeEntries;
        } else {
            return null;
        }
    }

    public long[] getSyncSamples() {
        if (track2s[0].getSyncSamples() != null && track2s[0].getSyncSamples().length > 0) {
            int numSyncSamples = 0;
            for (Track2 track2 : track2s) {
                numSyncSamples += track2.getSyncSamples().length;
            }
            long[] returnSyncSamples = new long[numSyncSamples];

            int pos = 0;
            long samplesBefore = 0;
            for (Track2 track2 : track2s) {
                for (long l : track2.getSyncSamples()) {
                    returnSyncSamples[pos++] = samplesBefore + l;
                }
                samplesBefore += track2.getSamples().size();
            }
            return returnSyncSamples;
        } else {
            return null;
        }
    }

    public List<SampleDependencyTypeBox2.Entry> getSampleDependencies() {
        if (track2s[0].getSampleDependencies() != null && !track2s[0].getSampleDependencies().isEmpty()) {
            List<SampleDependencyTypeBox2.Entry> list = new LinkedList<SampleDependencyTypeBox2.Entry>();
            for (Track2 track2 : track2s) {
                list.addAll(track2.getSampleDependencies());
            }
            return list;
        } else {
            return null;
        }
    }

    public TrackMetaData2 getTrackMetaData2() {
        return track2s[0].getTrackMetaData2();
    }

    public String getHandler() {
        return track2s[0].getHandler();
    }

    public AbstractMediaHeaderBox2 getMediaHeaderBox() {
        return track2s[0].getMediaHeaderBox();
    }

    public SubSampleInformationBox2 getSubsampleInformationBox() {
        return track2s[0].getSubsampleInformationBox();
    }

}
