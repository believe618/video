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
package com.googlecode.mp4parser.authoring.builder;

import java.util.Arrays;
import java.util.List;

import com.coremedia.iso.boxes.TimeToSampleBox2;
import com.googlecode.mp4parser.authoring.Movie2;
import com.googlecode.mp4parser.authoring.Track2;

/**
 * This <code>FragmentIntersectionFinder</code> cuts the input movie in 2 second
 * snippets.
 */
public class TwoSecondIntersectionFinder2 implements FragmentIntersectionFinder2 {

    protected long getDuration(Track2 track2) {
        long duration = 0;
        for (TimeToSampleBox2.Entry entry : track2.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    /**
     * {@inheritDoc}
     */
    public long[] sampleNumbers(Track2 track2, Movie2 movie2) {
        List<TimeToSampleBox2.Entry> entries = track2.getDecodingTimeEntries();

        double trackLength = 0;
        for (Track2 thisTrack2 : movie2.getTrack2s()) {
            double thisTracksLength = getDuration(thisTrack2) / thisTrack2.getTrackMetaData2().getTimescale();
            if (trackLength < thisTracksLength) {
                trackLength = thisTracksLength;
            }
        }

        long fragments[] = new long[(int) Math.max(2, Math.ceil(trackLength / 2)) - 1];
        Arrays.fill(fragments, -1);
        fragments[0] = 0;

        long time = 0;
        int samples = 0;
        for (TimeToSampleBox2.Entry entry : entries) {
            for (int i = 0; i < entry.getCount(); i++) {
                int currentFragment = (int) (time / track2.getTrackMetaData2().getTimescale() / 2) + 1;
                if (currentFragment >= fragments.length) {
                    break;
                }
                fragments[currentFragment] = samples++;
                time += entry.getDelta();
            }
        }
        long last = samples;
        // fill all -1 ones.
        for (int i = fragments.length - 1; i >= 0; i--) {
            if (fragments[i] == -1) {
                fragments[i] = last;
            }
            last = fragments[i];
        }
        return fragments;

    }

}
