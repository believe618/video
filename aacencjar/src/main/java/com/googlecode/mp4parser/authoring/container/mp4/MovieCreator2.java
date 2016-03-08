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
package com.googlecode.mp4parser.authoring.container.mp4;

import com.coremedia.iso.IsoFile2;
import com.coremedia.iso.boxes.TrackBox2;
import com.googlecode.mp4parser.authoring.Movie2;
import com.googlecode.mp4parser.authoring.Mp4Track22Impl2;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * Shortcut to build a movie from an MP4 file.
 */
public class MovieCreator2 {
    public static Movie2 build(ReadableByteChannel channel) throws IOException {
        IsoFile2 isoFile = new IsoFile2(channel);
        Movie2 m = new Movie2();
        List<TrackBox2> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox2.class);
        for (TrackBox2 trackBox : trackBoxes) {
            m.addTrack(new Mp4Track22Impl2(trackBox));
        }
        return m;
    }
}
