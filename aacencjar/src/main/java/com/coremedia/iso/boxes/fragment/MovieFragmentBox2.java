/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.fragment;

import com.googlecode.mp4parser.AbstractContainerBox2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.SampleDependencyTypeBox2;
import com.googlecode.mp4parser.annotations.DoNotParseDetail2;

import java.util.ArrayList;
import java.util.List;

/**
 * aligned(8) class MovieFragmentBox extends Box(moof){
 * }
 */

public class MovieFragmentBox2 extends AbstractContainerBox2 {
    public static final String TYPE = "moof";

    public MovieFragmentBox2() {
        super(TYPE);
    }


    public List<Long> getSyncSamples(SampleDependencyTypeBox2 sdtp) {
        List<Long> result = new ArrayList<Long>();

        final List<SampleDependencyTypeBox2.Entry> sampleEntries = sdtp.getEntries();
        long i = 1;
        for (SampleDependencyTypeBox2.Entry sampleEntry : sampleEntries) {
            if (sampleEntry.getSampleDependsOn() == 2) {
                result.add(i);
            }
            i++;
        }

        return result;
    }

    @DoNotParseDetail2
    public long getOffset() {
        Box2 b = this;
        long offset = 0;
        while (b.getParent() != null) {
            for (Box2 box2 : b.getParent().getBox2s()) {
                if (b == box2) {
                    break;
                }
                offset += box2.getSize();
            }
            b = b.getParent();
        }
        return offset;
    }


    public int getTrackCount() {
        return getBoxes(TrackFragmentBox2.class, false).size();
    }

    /**
     * Returns the track numbers associated with this <code>MovieBox</code>.
     *
     * @return the tracknumbers (IDs) of the tracks in their order of appearance in the file
     */

    public long[] getTrackNumbers() {

        List<TrackFragmentBox2> trackBoxes = this.getBoxes(TrackFragmentBox2.class, false);
        long[] trackNumbers = new long[trackBoxes.size()];
        for (int trackCounter = 0; trackCounter < trackBoxes.size(); trackCounter++) {
            TrackFragmentBox2 trackBoxe = trackBoxes.get(trackCounter);
            trackNumbers[trackCounter] = trackBoxe.getTrackFragmentHeaderBox().getTrackId();
        }
        return trackNumbers;
    }

    public List<TrackRunBox2> getTrackRunBoxes() {
        return getBoxes(TrackRunBox2.class, true);
    }
}
