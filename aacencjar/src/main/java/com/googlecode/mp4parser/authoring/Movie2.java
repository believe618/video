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
package com.googlecode.mp4parser.authoring;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Movie2 {
    List<Track2> track2s = new LinkedList<Track2>();

    public List<Track2> getTrack2s() {
        return track2s;
    }

    public void setTrack2s(List<Track2> track2s) {
        this.track2s = track2s;
    }

    public void addTrack(Track2 nuTrack2) {
        // do some checking
        // perhaps the movie needs to get longer!
        if (getTrackByTrackId(nuTrack2.getTrackMetaData2().getTrackId()) != null) {
            // We already have a track with that trackId. Create a new one
            nuTrack2.getTrackMetaData2().setTrackId(getNextTrackId());
        }
        track2s.add(nuTrack2);
    }


    @Override
    public String toString() {
        String s = "Movie{ ";
        for (Track2 track2 : track2s) {
            s += "track_" + track2.getTrackMetaData2().getTrackId() + " (" + track2.getHandler() + ") ";
        }

        s += '}';
        return s;
    }

    public long getNextTrackId() {
        long nextTrackId = 0;
        for (Track2 track2 : track2s) {
            nextTrackId = nextTrackId < track2.getTrackMetaData2().getTrackId() ? track2.getTrackMetaData2().getTrackId() : nextTrackId;
        }
        return ++nextTrackId;
    }


    public Track2 getTrackByTrackId(long trackId) {
        for (Track2 track2 : track2s) {
            if (track2.getTrackMetaData2().getTrackId() == trackId) {
                return track2;
            }
        }
        return null;
    }


    public long getTimescale() {
        long timescale = this.getTrack2s().iterator().next().getTrackMetaData2().getTimescale();
        for (Track2 track2 : this.getTrack2s()) {
            timescale = gcd(track2.getTrackMetaData2().getTimescale(), timescale);
        }
        return timescale;
    }

    public static long gcd(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

}
