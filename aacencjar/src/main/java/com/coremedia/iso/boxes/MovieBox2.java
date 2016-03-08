/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package com.coremedia.iso.boxes;


import com.googlecode.mp4parser.AbstractBox2;
import com.googlecode.mp4parser.AbstractContainerBox2;

import java.util.List;

/**
 * The metadata for a presentation is stored in the single Movie Box which occurs at the top-level of a file.
 * Normally this box is close to the beginning or end of the file, though this is not required.
 */
public class MovieBox2 extends AbstractContainerBox2 {
    public static final String TYPE = "moov";

    public MovieBox2() {
        super(TYPE);
    }

    public int getTrackCount() {
        return getBoxes(TrackBox2.class).size();
    }


    /**
     * Returns the track numbers associated with this <code>MovieBox</code>.
     *
     * @return the tracknumbers (IDs) of the tracks in their order of appearance in the file
     */
    public long[] getTrackNumbers() {

        List<TrackBox2> trackBoxes = this.getBoxes(TrackBox2.class);
        long[] trackNumbers = new long[trackBoxes.size()];
        for (int trackCounter = 0; trackCounter < trackBoxes.size(); trackCounter++) {
            AbstractBox2 trackBoxe = trackBoxes.get(trackCounter);
            TrackBox2 trackBox = (TrackBox2) trackBoxe;
            trackNumbers[trackCounter] = trackBox.getTrackHeaderBox().getTrackId();
        }
        return trackNumbers;
    }

    public MovieHeaderBox2 getMovieHeaderBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof MovieHeaderBox2) {
                return (MovieHeaderBox2) box2;
            }
        }
        return null;
    }

}
