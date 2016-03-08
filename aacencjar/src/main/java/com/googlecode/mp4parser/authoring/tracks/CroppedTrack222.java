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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates a Track that starts at fromSample and ends at toSample (exclusive). The user of this class
 * has to make sure that the fromSample is a random access sample.
 * <ul>
 * <li>In AAC this is every single sample</li>
 * <li>In H264 this is every sample that is marked in the SyncSampleBox</li>
 * </ul>
 */
public class CroppedTrack222 extends AbstractTrack22 {
    Track2 origTrack2;
    private int fromSample;
    private int toSample;
    private long[] syncSampleArray;

    public CroppedTrack222(Track2 origTrack2, long fromSample, long toSample) {
        this.origTrack2 = origTrack2;
        assert fromSample <= Integer.MAX_VALUE;
        assert toSample <= Integer.MAX_VALUE;
        this.fromSample = (int) fromSample;
        this.toSample = (int) toSample;
    }

    public List<ByteBuffer> getSamples() {
        return origTrack2.getSamples().subList(fromSample, toSample);
    }

    public SampleDescriptionBox2 getSampleDescriptionBox() {
        return origTrack2.getSampleDescriptionBox();
    }

    public List<TimeToSampleBox2.Entry> getDecodingTimeEntries() {
        if (origTrack2.getDecodingTimeEntries() != null && !origTrack2.getDecodingTimeEntries().isEmpty()) {
            long[] decodingTimes = TimeToSampleBox2.blowupTimeToSamples(origTrack2.getDecodingTimeEntries());
            long[] nuDecodingTimes = new long[toSample - fromSample];
            System.arraycopy(decodingTimes, fromSample, nuDecodingTimes, 0, toSample - fromSample);

            LinkedList<TimeToSampleBox2.Entry> returnDecodingEntries = new LinkedList<TimeToSampleBox2.Entry>();

            for (long nuDecodingTime : nuDecodingTimes) {
                if (returnDecodingEntries.isEmpty() || returnDecodingEntries.getLast().getDelta() != nuDecodingTime) {
                    TimeToSampleBox2.Entry e = new TimeToSampleBox2.Entry(1, nuDecodingTime);
                    returnDecodingEntries.add(e);
                } else {
                    TimeToSampleBox2.Entry e = returnDecodingEntries.getLast();
                    e.setCount(e.getCount() + 1);
                }
            }
            return returnDecodingEntries;
        } else {
            return null;
        }
    }

    public List<CompositionTimeToSample2.Entry> getCompositionTimeEntries() {
        if (origTrack2.getCompositionTimeEntries() != null && !origTrack2.getCompositionTimeEntries().isEmpty()) {
            int[] compositionTime = CompositionTimeToSample2.blowupCompositionTimes(origTrack2.getCompositionTimeEntries());
            int[] nuCompositionTimes = new int[toSample - fromSample];
            System.arraycopy(compositionTime, fromSample, nuCompositionTimes, 0, toSample - fromSample);

            LinkedList<CompositionTimeToSample2.Entry> returnDecodingEntries = new LinkedList<CompositionTimeToSample2.Entry>();

            for (int nuDecodingTime : nuCompositionTimes) {
                if (returnDecodingEntries.isEmpty() || returnDecodingEntries.getLast().getOffset() != nuDecodingTime) {
                    CompositionTimeToSample2.Entry e = new CompositionTimeToSample2.Entry(1, nuDecodingTime);
                    returnDecodingEntries.add(e);
                } else {
                    CompositionTimeToSample2.Entry e = returnDecodingEntries.getLast();
                    e.setCount(e.getCount() + 1);
                }
            }
            return returnDecodingEntries;
        } else {
            return null;
        }
    }

    synchronized public long[] getSyncSamples() {
        if (this.syncSampleArray == null) {
            if (origTrack2.getSyncSamples() != null && origTrack2.getSyncSamples().length > 0) {
                List<Long> syncSamples = new LinkedList<Long>();
                for (long l : origTrack2.getSyncSamples()) {
                    if (l >= fromSample && l < toSample) {
                        syncSamples.add(l - fromSample);
                    }
                }
                syncSampleArray = new long[syncSamples.size()];
                for (int i = 0; i < syncSampleArray.length; i++) {
                    syncSampleArray[i] = syncSamples.get(i);

                }
                return syncSampleArray;
            } else {
                return null;
            }
        } else {
            return this.syncSampleArray;
        }
    }

    public List<SampleDependencyTypeBox2.Entry> getSampleDependencies() {
        if (origTrack2.getSampleDependencies() != null && !origTrack2.getSampleDependencies().isEmpty()) {
            return origTrack2.getSampleDependencies().subList(fromSample, toSample);
        } else {
            return null;
        }
    }

    public TrackMetaData2 getTrackMetaData2() {
        return origTrack2.getTrackMetaData2();
    }

    public String getHandler() {
        return origTrack2.getHandler();
    }

    public AbstractMediaHeaderBox2 getMediaHeaderBox() {
        return origTrack2.getMediaHeaderBox();
    }

    public SubSampleInformationBox2 getSubsampleInformationBox() {
        return origTrack2.getSubsampleInformationBox();
    }

}