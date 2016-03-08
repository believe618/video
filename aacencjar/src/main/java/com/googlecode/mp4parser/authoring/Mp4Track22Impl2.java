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

import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox2;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox2;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox2;
import com.coremedia.iso.boxes.fragment.TrackRunBox2;
import com.coremedia.iso.boxes.mdat.SampleList2;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * Represents a single track of an MP4 file.
 */
public class Mp4Track22Impl2 extends AbstractTrack22 {
    private List<ByteBuffer> samples;
    private SampleDescriptionBox2 sampleDescriptionBox;
    private List<TimeToSampleBox2.Entry> decodingTimeEntries;
    private List<CompositionTimeToSample2.Entry> compositionTimeEntries;
    private long[] syncSamples;
    private List<SampleDependencyTypeBox2.Entry> sampleDependencies;
    private TrackMetaData2 trackMetaData2 = new TrackMetaData2();
    private String handler;
    private AbstractMediaHeaderBox2 mihd;

    public Mp4Track22Impl2(TrackBox2 trackBox) {
        samples = new SampleList2(trackBox);
        SampleTableBox2 stbl = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox();
        handler = trackBox.getMediaBox().getHandlerBox().getHandlerType();


        mihd = trackBox.getMediaBox().getMediaInformationBox().getMediaHeaderBox();


        sampleDescriptionBox = stbl.getSampleDescriptionBox();
        if (trackBox.getParent().getBoxes(MovieExtendsBox2.class).size() > 0) {

            decodingTimeEntries = new LinkedList<TimeToSampleBox2.Entry>();
            compositionTimeEntries = new LinkedList<CompositionTimeToSample2.Entry>();
            sampleDependencies = new LinkedList<SampleDependencyTypeBox2.Entry>();

            for (MovieFragmentBox2 movieFragmentBox : trackBox.getIsoFile().getBoxes(MovieFragmentBox2.class)) {
                List<TrackFragmentBox2> trafs = movieFragmentBox.getBoxes(TrackFragmentBox2.class);
                for (TrackFragmentBox2 traf : trafs) {
                    if (traf.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                        List<TrackRunBox2> truns = traf.getBoxes(TrackRunBox2.class);
                        for (TrackRunBox2 trun : truns) {
                            for (TrackRunBox2.Entry entry : trun.getEntries()) {
                                if (trun.isSampleDurationPresent()) {
                                    if (decodingTimeEntries.size() == 0 ||
                                            decodingTimeEntries.get(decodingTimeEntries.size() - 1).getDelta() != entry.getSampleDuration()) {
                                        decodingTimeEntries.add(new TimeToSampleBox2.Entry(1, entry.getSampleDuration()));
                                    } else {
                                        TimeToSampleBox2.Entry e = decodingTimeEntries.get(decodingTimeEntries.size() - 1);
                                        e.setCount(e.getCount() + 1);
                                    }
                                }
                                if (trun.isSampleCompositionTimeOffsetPresent()) {
                                    if (compositionTimeEntries.size() == 0 ||
                                            compositionTimeEntries.get(compositionTimeEntries.size() - 1).getOffset() != entry.getSampleCompositionTimeOffset()) {
                                        compositionTimeEntries.add(new CompositionTimeToSample2.Entry(1, l2i(entry.getSampleCompositionTimeOffset())));
                                    } else {
                                        CompositionTimeToSample2.Entry e = compositionTimeEntries.get(compositionTimeEntries.size() - 1);
                                        e.setCount(e.getCount() + 1);
                                    }
                                }

                            }


                        }


                    }
                }
            }
        } else {
            decodingTimeEntries = stbl.getTimeToSampleBox().getEntries();
            if (stbl.getCompositionTimeToSample() != null) {
                compositionTimeEntries = stbl.getCompositionTimeToSample().getEntries();
            }
            if (stbl.getSyncSampleBox() != null) {
                syncSamples = stbl.getSyncSampleBox().getSampleNumber();
            }
            if (stbl.getSampleDependencyTypeBox() != null) {
                sampleDependencies = stbl.getSampleDependencyTypeBox().getEntries();
            }
        }
        MediaHeaderBox2 mdhd = trackBox.getMediaBox().getMediaHeaderBox();
        TrackHeaderBox2 tkhd = trackBox.getTrackHeaderBox();

        setEnabled(tkhd.isEnabled());
        setInMovie(tkhd.isInMovie());
        setInPoster(tkhd.isInPoster());
        setInPreview(tkhd.isInPreview());

        trackMetaData2.setTrackId(tkhd.getTrackId());
        trackMetaData2.setCreationTime(DateHelper2.convert(mdhd.getCreationTime()));
        trackMetaData2.setLanguage(mdhd.getLanguage());
/*        System.err.println(mdhd.getModificationTime());
        System.err.println(DateHelper.convert(mdhd.getModificationTime()));
        System.err.println(DateHelper.convert(DateHelper.convert(mdhd.getModificationTime())));
        System.err.println(DateHelper.convert(DateHelper.convert(DateHelper.convert(mdhd.getModificationTime()))));*/

        trackMetaData2.setModificationTime(DateHelper2.convert(mdhd.getModificationTime()));
        trackMetaData2.setTimescale(mdhd.getTimescale());
        trackMetaData2.setHeight(tkhd.getHeight());
        trackMetaData2.setWidth(tkhd.getWidth());
        trackMetaData2.setLayer(tkhd.getLayer());
    }

    public List<ByteBuffer> getSamples() {
        return samples;
    }


    public SampleDescriptionBox2 getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public List<TimeToSampleBox2.Entry> getDecodingTimeEntries() {
        return decodingTimeEntries;
    }

    public List<CompositionTimeToSample2.Entry> getCompositionTimeEntries() {
        return compositionTimeEntries;
    }

    public long[] getSyncSamples() {
        return syncSamples;
    }

    public List<SampleDependencyTypeBox2.Entry> getSampleDependencies() {
        return sampleDependencies;
    }

    public TrackMetaData2 getTrackMetaData2() {
        return trackMetaData2;
    }

    public String getHandler() {
        return handler;
    }

    public AbstractMediaHeaderBox2 getMediaHeaderBox() {
        return mihd;
    }

    public SubSampleInformationBox2 getSubsampleInformationBox() {
        return null;
    }

    @Override
    public String toString() {
        return "Mp4TrackImpl{" +
                "handler='" + handler + '\'' +
                '}';
    }
}
