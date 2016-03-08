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

import com.coremedia.iso.BoxParser2;
import com.coremedia.iso.IsoFile2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.CompositionTimeToSample2;
import com.coremedia.iso.boxes.ContainerBox2;
import com.coremedia.iso.boxes.DataEntryUrlBox2;
import com.coremedia.iso.boxes.DataInformationBox2;
import com.coremedia.iso.boxes.DataReferenceBox2;
import com.coremedia.iso.boxes.FileTypeBox2;
import com.coremedia.iso.boxes.HandlerBox2;
import com.coremedia.iso.boxes.MediaBox2;
import com.coremedia.iso.boxes.MediaHeaderBox2;
import com.coremedia.iso.boxes.MediaInformationBox2;
import com.coremedia.iso.boxes.MovieBox2;
import com.coremedia.iso.boxes.MovieHeaderBox2;
import com.coremedia.iso.boxes.SampleDependencyTypeBox2;
import com.coremedia.iso.boxes.SampleTableBox2;
import com.coremedia.iso.boxes.StaticChunkOffsetBox2;
import com.coremedia.iso.boxes.TimeToSampleBox2;
import com.coremedia.iso.boxes.TrackBox2;
import com.coremedia.iso.boxes.TrackHeaderBox2;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox2;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox2;
import com.coremedia.iso.boxes.fragment.MovieFragmentHeaderBox2;
import com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessBox2;
import com.coremedia.iso.boxes.fragment.MovieFragmentRandomAccessOffsetBox2;
import com.coremedia.iso.boxes.fragment.SampleFlags2;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox2;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox2;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox2;
import com.coremedia.iso.boxes.fragment.TrackFragmentRandomAccessBox2;
import com.coremedia.iso.boxes.fragment.TrackRunBox2;
import com.googlecode.mp4parser.authoring.DateHelper2;
import com.googlecode.mp4parser.authoring.Movie2;
import com.googlecode.mp4parser.authoring.Track2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * Creates a fragmented MP4 file.
 */
public class FragmentedMp4Builder22 implements Mp4Builder2 {
    FragmentIntersectionFinder2 intersectionFinder = new SyncSampleIntersectFinder2Impl2();

    private static final Logger LOG = Logger.getLogger(FragmentedMp4Builder22.class.getName());

    public List<String> getAllowedHandlers() {
        return Arrays.asList("soun", "vide");
    }

    public Box2 createFtyp(Movie2 movie2) {
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");
        return new FileTypeBox2("isom", 0, minorBrands);
    }

    protected List<Box2> createMoofMdat(final Movie2 movie2) {
        List<Box2> box2s = new LinkedList<Box2>();
        int maxNumberOfFragments = 0;
        for (Track2 track2 : movie2.getTrack2s()) {
            int currentLength = intersectionFinder.sampleNumbers(track2, movie2).length;
            maxNumberOfFragments = currentLength > maxNumberOfFragments ? currentLength : maxNumberOfFragments;
        }
        int sequence = 1;
        for (int i = 0; i < maxNumberOfFragments; i++) {

            final List<Track2> sizeSortedTrack2s = new LinkedList<Track2>(movie2.getTrack2s());
            final int j = i;
            Collections.sort(sizeSortedTrack2s, new Comparator<Track2>() {
                public int compare(Track2 o1, Track2 o2) {
                    long[] startSamples1 = intersectionFinder.sampleNumbers(o1, movie2);
                    long startSample1 = startSamples1[j];
                    // one based sample numbers - the first sample is 1
                    long endSample1 = j + 1 < startSamples1.length ? startSamples1[j + 1] : o1.getSamples().size() + 1;
                    long[] startSamples2 = intersectionFinder.sampleNumbers(o2, movie2);
                    long startSample2 = startSamples2[j];
                    // one based sample numbers - the first sample is 1
                    long endSample2 = j + 1 < startSamples2.length ? startSamples2[j + 1] : o2.getSamples().size() + 1;
                    List<ByteBuffer> samples1 = o1.getSamples().subList(l2i(startSample1) - 1, l2i(endSample1) - 1);
                    List<ByteBuffer> samples2 = o2.getSamples().subList(l2i(startSample2) - 1, l2i(endSample2) - 1);
                    int size1 = 0;
                    for (ByteBuffer byteBuffer : samples1) {
                        size1 += byteBuffer.limit();
                    }
                    int size2 = 0;
                    for (ByteBuffer byteBuffer : samples2) {
                        size2 += byteBuffer.limit();
                    }
                    return size1 - size2;
                }
            });

            for (Track2 track2 : sizeSortedTrack2s) {
                if (getAllowedHandlers().isEmpty() || getAllowedHandlers().contains(track2.getHandler())) {
                    long[] startSamples = intersectionFinder.sampleNumbers(track2, movie2);

                    if (i < startSamples.length) {
                        long startSample = startSamples[i];
                        // one based sample numbers - the first sample is 1
                        long endSample = i + 1 < startSamples.length ? startSamples[i + 1] : track2.getSamples().size() + 1;

                        if (startSample == endSample) {
                            // empty fragment
                            // just don't add any boxes.
                        } else {
                            box2s.add(createMoof(startSample, endSample, track2, sequence));
                            box2s.add(createMdat(startSample, endSample, track2, sequence++));
                        }

                    } else {
                        //obvious this track has not that many fragments
                    }
                }
            }


        }
        return box2s;
    }

    /**
     * {@inheritDoc}
     */
    public IsoFile2 build(Movie2 movie2) {
        LOG.fine("Creating movie " + movie2);
        IsoFile2 isoFile2 = new IsoFile2();


        isoFile2.addBox(createFtyp(movie2));
        isoFile2.addBox(createMoov(movie2));

        for (Box2 box2 : createMoofMdat(movie2)) {
            isoFile2.addBox(box2);
        }
        isoFile2.addBox(createMfra(movie2, isoFile2));

        return isoFile2;
    }

    protected Box2 createMdat(final long startSample, final long endSample, final Track2 track2, final int i) {

        class Mdat implements Box2 {
            ContainerBox2 parent;

            public ContainerBox2 getParent() {
                return parent;
            }

            public void setParent(ContainerBox2 parent) {
                this.parent = parent;
            }

            public long getSize() {
                long size = 8; // I don't expect 2gig fragments
                for (ByteBuffer sample : getSamples(startSample, endSample, track2, i)) {
                    size += sample.limit();
                }
                return size;
            }

            public String getType() {
                return "mdat";
            }

            public void getBox(WritableByteChannel writableByteChannel) throws IOException {
                List<ByteBuffer> bbs = getSamples(startSample, endSample, track2, i);
                final List<ByteBuffer> samples = ByteBufferHelper2.mergeAdjacentBuffers(bbs);
                ByteBuffer header = ByteBuffer.allocate(8);
                IsoTypeWriter2.writeUInt32(header, l2i(getSize()));
                header.put(IsoFile2.fourCCtoBytes(getType()));
                header.rewind();
                writableByteChannel.write(header);
                if (writableByteChannel instanceof GatheringByteChannel) {

                    int STEPSIZE = 1024;
                    for (int i = 0; i < Math.ceil((double) samples.size() / STEPSIZE); i++) {
                        List<ByteBuffer> sublist = samples.subList(
                                i * STEPSIZE, // start
                                (i + 1) * STEPSIZE < samples.size() ? (i + 1) * STEPSIZE : samples.size()); // end
                        ByteBuffer sampleArray[] = sublist.toArray(new ByteBuffer[sublist.size()]);
                        do {
                            ((GatheringByteChannel) writableByteChannel).write(sampleArray);
                        } while (sampleArray[sampleArray.length - 1].remaining() > 0);
                    }
                    //System.err.println(bytesWritten);
                } else {
                    for (ByteBuffer sample : samples) {
                        sample.rewind();
                        writableByteChannel.write(sample);
                    }
                }

            }

            public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser2 boxParser) throws IOException {

            }
        }

        return new Mdat();
    }

    protected Box2 createTfhd(long startSample, long endSample, Track2 track2, int sequenceNumber) {
        TrackFragmentHeaderBox2 tfhd = new TrackFragmentHeaderBox2();
        SampleFlags2 sf = new SampleFlags2();

        tfhd.setDefaultSampleFlags2(sf);
        tfhd.setBaseDataOffset(-1);
        tfhd.setTrackId(track2.getTrackMetaData2().getTrackId());
        return tfhd;
    }

    protected Box2 createMfhd(long startSample, long endSample, Track2 track2, int sequenceNumber) {
        MovieFragmentHeaderBox2 mfhd = new MovieFragmentHeaderBox2();
        mfhd.setSequenceNumber(sequenceNumber);
        return mfhd;
    }

    protected Box2 createTraf(long startSample, long endSample, Track2 track2, int sequenceNumber) {
        TrackFragmentBox2 traf = new TrackFragmentBox2();
        traf.addBox(createTfhd(startSample, endSample, track2, sequenceNumber));
        for (Box2 trun : createTruns(startSample, endSample, track2, sequenceNumber)) {
            traf.addBox(trun);
        }

        return traf;
    }


    /**
     * @param startSample    first sample in list starting with 1. 1 is the first sample.
     * @param endSample
     * @param track2
     * @param sequenceNumber
     * @return
     */
    protected List<ByteBuffer> getSamples(long startSample, long endSample, Track2 track2, int sequenceNumber) {
        // since startSample and endSample are one-based substract 1 before addressing list elements
        return track2.getSamples().subList(l2i(startSample) - 1, l2i(endSample) - 1);
    }


    protected List<? extends Box2> createTruns(long startSample, long endSample, Track2 track2, int sequenceNumber) {
        List<ByteBuffer> samples = getSamples(startSample, endSample, track2, sequenceNumber);

        long[] sampleSizes = new long[samples.size()];
        for (int i = 0; i < sampleSizes.length; i++) {
            sampleSizes[i] = samples.get(i).limit();
        }
        TrackRunBox2 trun = new TrackRunBox2();


        trun.setSampleDurationPresent(true);
        trun.setSampleSizePresent(true);
        List<TrackRunBox2.Entry> entries = new ArrayList<TrackRunBox2.Entry>(l2i(endSample - startSample));


        Queue<TimeToSampleBox2.Entry> timeQueue = new LinkedList<TimeToSampleBox2.Entry>(track2.getDecodingTimeEntries());
        long left = startSample;
        long curEntryLeft = timeQueue.peek().getCount();
        while (left >= curEntryLeft) {
            left -= curEntryLeft;
            timeQueue.remove();
            curEntryLeft = timeQueue.peek().getCount();
        }
        curEntryLeft -= left;


        Queue<CompositionTimeToSample2.Entry> compositionTimeQueue =
                track2.getCompositionTimeEntries() != null && track2.getCompositionTimeEntries().size() > 0 ?
                        new LinkedList<CompositionTimeToSample2.Entry>(track2.getCompositionTimeEntries()) : null;
        long compositionTimeEntriesLeft = compositionTimeQueue != null ? compositionTimeQueue.peek().getCount() : -1;


        trun.setSampleCompositionTimeOffsetPresent(compositionTimeEntriesLeft > 0);

        // fast forward composition stuff
        for (long i = 1; i < startSample; i++) {
            if (compositionTimeQueue != null) {
                trun.setSampleCompositionTimeOffsetPresent(true);
                if (--compositionTimeEntriesLeft == 0 && compositionTimeQueue.size() > 1) {
                    compositionTimeQueue.remove();
                    compositionTimeEntriesLeft = compositionTimeQueue.element().getCount();
                }
            }
        }

        boolean sampleFlagsRequired = (track2.getSampleDependencies() != null && !track2.getSampleDependencies().isEmpty() ||
                track2.getSyncSamples() != null && track2.getSyncSamples().length != 0);

        trun.setSampleFlagsPresent(sampleFlagsRequired);

        for (int i = 0; i < sampleSizes.length; i++) {
            TrackRunBox2.Entry entry = new TrackRunBox2.Entry();
            entry.setSampleSize(sampleSizes[i]);
            if (sampleFlagsRequired) {
                //if (false) {
                SampleFlags2 sflags = new SampleFlags2();

                if (track2.getSampleDependencies() != null && !track2.getSampleDependencies().isEmpty()) {
                    SampleDependencyTypeBox2.Entry e = track2.getSampleDependencies().get(i);
                    sflags.setSampleDependsOn(e.getSampleDependsOn());
                    sflags.setSampleIsDependedOn(e.getSampleIsDependentOn());
                    sflags.setSampleHasRedundancy(e.getSampleHasRedundancy());
                }
                if (track2.getSyncSamples() != null && track2.getSyncSamples().length > 0) {
                    // we have to mark non-sync samples!
                    if (Arrays.binarySearch(track2.getSyncSamples(), startSample + i) >= 0) {
                        sflags.setSampleIsDifferenceSample(false);
                        sflags.setSampleDependsOn(2);
                    } else {
                        sflags.setSampleIsDifferenceSample(true);
                        sflags.setSampleDependsOn(1);
                    }
                }
                // i don't have sample degradation
                entry.setSampleFlags2(sflags);

            }

            entry.setSampleDuration(timeQueue.peek().getDelta());
            if (--curEntryLeft == 0 && timeQueue.size() > 1) {
                timeQueue.remove();
                curEntryLeft = timeQueue.peek().getCount();
            }

            if (compositionTimeQueue != null) {
                trun.setSampleCompositionTimeOffsetPresent(true);
                entry.setSampleCompositionTimeOffset(compositionTimeQueue.peek().getOffset());
                if (--compositionTimeEntriesLeft == 0 && compositionTimeQueue.size() > 1) {
                    compositionTimeQueue.remove();
                    compositionTimeEntriesLeft = compositionTimeQueue.element().getCount();
                }
            }
            entries.add(entry);
        }

        trun.setEntries(entries);

        return Collections.singletonList(trun);
    }

    protected Box2 createMoof(long startSample, long endSample, Track2 track2, int sequenceNumber) {


        MovieFragmentBox2 moof = new MovieFragmentBox2();
        moof.addBox(createMfhd(startSample, endSample, track2, sequenceNumber));
        moof.addBox(createTraf(startSample, endSample, track2, sequenceNumber));

        TrackRunBox2 firstTrun = moof.getTrackRunBoxes().get(0);
        firstTrun.setDataOffset(1); // dummy to make size correct
        firstTrun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size

        return moof;
    }

    protected Box2 createMvhd(Movie2 movie2) {
        MovieHeaderBox2 mvhd = new MovieHeaderBox2();
        mvhd.setVersion(1);
        mvhd.setCreationTime(DateHelper2.convert(new Date()));
        mvhd.setModificationTime(DateHelper2.convert(new Date()));
        long movieTimeScale = movie2.getTimescale();
        long duration = 0;

        for (Track2 track2 : movie2.getTrack2s()) {
            long tracksDuration = getDuration(track2) * movieTimeScale / track2.getTrackMetaData2().getTimescale();
            if (tracksDuration > duration) {
                duration = tracksDuration;
            }


        }

        mvhd.setDuration(duration);
        mvhd.setTimescale(movieTimeScale);
        // find the next available trackId
        long nextTrackId = 0;
        for (Track2 track2 : movie2.getTrack2s()) {
            nextTrackId = nextTrackId < track2.getTrackMetaData2().getTrackId() ? track2.getTrackMetaData2().getTrackId() : nextTrackId;
        }
        mvhd.setNextTrackId(++nextTrackId);
        return mvhd;
    }

    protected Box2 createMoov(Movie2 movie2) {
        MovieBox2 movieBox = new MovieBox2();

        movieBox.addBox(createMvhd(movie2));
        movieBox.addBox(createMvex(movie2));

        for (Track2 track2 : movie2.getTrack2s()) {
            movieBox.addBox(createTrak(track2, movie2));
        }
        // metadata here
        return movieBox;

    }

    protected Box2 createTfra(Track2 track2, IsoFile2 isoFile2) {
        TrackFragmentRandomAccessBox2 tfra = new TrackFragmentRandomAccessBox2();
        tfra.setVersion(1); // use long offsets and times
        List<TrackFragmentRandomAccessBox2.Entry> offset2timeEntries = new LinkedList<TrackFragmentRandomAccessBox2.Entry>();
        List<Box2> box2s = isoFile2.getBox2s();
        long offset = 0;
        long duration = 0;
        for (Box2 box2 : box2s) {
            if (box2 instanceof MovieFragmentBox2) {
                List<TrackFragmentBox2> trafs = ((MovieFragmentBox2) box2).getBoxes(TrackFragmentBox2.class);
                for (int i = 0; i < trafs.size(); i++) {
                    TrackFragmentBox2 traf = trafs.get(i);
                    if (traf.getTrackFragmentHeaderBox().getTrackId() == track2.getTrackMetaData2().getTrackId()) {
                        // here we are at the offset required for the current entry.
                        List<TrackRunBox2> truns = traf.getBoxes(TrackRunBox2.class);
                        for (int j = 0; j < truns.size(); j++) {
                            List<TrackFragmentRandomAccessBox2.Entry> offset2timeEntriesThisTrun = new LinkedList<TrackFragmentRandomAccessBox2.Entry>();
                            TrackRunBox2 trun = truns.get(j);
                            for (int k = 0; k < trun.getEntries().size(); k++) {
                                TrackRunBox2.Entry trunEntry = trun.getEntries().get(k);
                                SampleFlags2 sf = null;
                                if (k == 0 && trun.isFirstSampleFlagsPresent()) {
                                    sf = trun.getFirstSampleFlags2();
                                } else if (trun.isSampleFlagsPresent()) {
                                    sf = trunEntry.getSampleFlags2();
                                } else {
                                    List<MovieExtendsBox2> mvexs = isoFile2.getMovieBox().getBoxes(MovieExtendsBox2.class);
                                    for (MovieExtendsBox2 mvex : mvexs) {
                                        List<TrackExtendsBox2> trexs = mvex.getBoxes(TrackExtendsBox2.class);
                                        for (TrackExtendsBox2 trex : trexs) {
                                            if (trex.getTrackId() == track2.getTrackMetaData2().getTrackId()) {
                                                sf = trex.getDefaultSampleFlags2();
                                            }
                                        }
                                    }

                                }
                                if (sf == null) {
                                    throw new RuntimeException("Could not find any SampleFlags to indicate random access or not");
                                }
                                if (sf.getSampleDependsOn() == 2) {
                                    offset2timeEntriesThisTrun.add(new TrackFragmentRandomAccessBox2.Entry(
                                            duration,
                                            offset,
                                            i + 1, j + 1, k + 1));
                                }
                                duration += trunEntry.getSampleDuration();
                            }
                            if (offset2timeEntriesThisTrun.size() == trun.getEntries().size() && trun.getEntries().size() > 0) {
                                // Oooops every sample seems to be random access sample
                                // is this an audio track? I don't care.
                                // I just use the first for trun sample for tfra random access
                                offset2timeEntries.add(offset2timeEntriesThisTrun.get(0));
                            } else {
                                offset2timeEntries.addAll(offset2timeEntriesThisTrun);
                            }
                        }
                    }
                }
            }


            offset += box2.getSize();
        }
        tfra.setEntries(offset2timeEntries);
        tfra.setTrackId(track2.getTrackMetaData2().getTrackId());
        return tfra;
    }

    protected Box2 createMfra(Movie2 movie2, IsoFile2 isoFile2) {
        MovieFragmentRandomAccessBox2 mfra = new MovieFragmentRandomAccessBox2();
        for (Track2 track2 : movie2.getTrack2s()) {
            mfra.addBox(createTfra(track2, isoFile2));
        }

        MovieFragmentRandomAccessOffsetBox2 mfro = new MovieFragmentRandomAccessOffsetBox2();
        mfra.addBox(mfro);
        mfro.setMfraSize(mfra.getSize());
        return mfra;
    }

    protected Box2 createTrex(Movie2 movie2, Track2 track2) {
        TrackExtendsBox2 trex = new TrackExtendsBox2();
        trex.setTrackId(track2.getTrackMetaData2().getTrackId());
        trex.setDefaultSampleDescriptionIndex(1);
        trex.setDefaultSampleDuration(0);
        trex.setDefaultSampleSize(0);
        SampleFlags2 sf = new SampleFlags2();
        if ("soun".equals(track2.getHandler())) {
            // as far as I know there is no audio encoding
            // where the sample are not self contained.
            sf.setSampleDependsOn(2);
            sf.setSampleIsDependedOn(2);
        }
        trex.setDefaultSampleFlags2(sf);
        return trex;
    }


    protected Box2 createMvex(Movie2 movie2) {
        MovieExtendsBox2 mvex = new MovieExtendsBox2();

        for (Track2 track2 : movie2.getTrack2s()) {
            mvex.addBox(createTrex(movie2, track2));
        }
        return mvex;
    }

    protected Box2 createTkhd(Movie2 movie2, Track2 track2) {
        TrackHeaderBox2 tkhd = new TrackHeaderBox2();
        tkhd.setVersion(1);
        int flags = 0;
        if (track2.isEnabled()) {
            flags += 1;
        }

        if (track2.isInMovie()) {
            flags += 2;
        }

        if (track2.isInPreview()) {
            flags += 4;
        }

        if (track2.isInPoster()) {
            flags += 8;
        }
        tkhd.setFlags(flags);

        tkhd.setAlternateGroup(track2.getTrackMetaData2().getGroup());
        tkhd.setCreationTime(DateHelper2.convert(track2.getTrackMetaData2().getCreationTime()));
        // We need to take edit list box into account in trackheader duration
        // but as long as I don't support edit list boxes it is sufficient to
        // just translate media duration to movie timescale
        tkhd.setDuration(getDuration(track2) * movie2.getTimescale() / track2.getTrackMetaData2().getTimescale());
        tkhd.setHeight(track2.getTrackMetaData2().getHeight());
        tkhd.setWidth(track2.getTrackMetaData2().getWidth());
        tkhd.setLayer(track2.getTrackMetaData2().getLayer());
        tkhd.setModificationTime(DateHelper2.convert(new Date()));
        tkhd.setTrackId(track2.getTrackMetaData2().getTrackId());
        tkhd.setVolume(track2.getTrackMetaData2().getVolume());
        return tkhd;
    }

    protected Box2 createMdhd(Movie2 movie2, Track2 track2) {
        MediaHeaderBox2 mdhd = new MediaHeaderBox2();
        mdhd.setCreationTime(DateHelper2.convert(track2.getTrackMetaData2().getCreationTime()));
        mdhd.setDuration(getDuration(track2));
        mdhd.setTimescale(track2.getTrackMetaData2().getTimescale());
        mdhd.setLanguage(track2.getTrackMetaData2().getLanguage());
        return mdhd;
    }

    protected Box2 createStbl(Movie2 movie2, Track2 track2) {
        SampleTableBox2 stbl = new SampleTableBox2();

        stbl.addBox(track2.getSampleDescriptionBox());
        stbl.addBox(new TimeToSampleBox2());
        //stbl.addBox(new SampleToChunkBox());
        stbl.addBox(new StaticChunkOffsetBox2());
        return stbl;
    }

    protected Box2 createMinf(Track2 track2, Movie2 movie2) {
        MediaInformationBox2 minf = new MediaInformationBox2();
        minf.addBox(track2.getMediaHeaderBox());
        minf.addBox(createDinf(movie2, track2));
        minf.addBox(createStbl(movie2, track2));
        return minf;
    }

    protected Box2 createMdiaHdlr(Track2 track2, Movie2 movie2) {
        HandlerBox2 hdlr = new HandlerBox2();
        hdlr.setHandlerType(track2.getHandler());
        return hdlr;
    }

    protected Box2 createMdia(Track2 track2, Movie2 movie2) {
        MediaBox2 mdia = new MediaBox2();
        mdia.addBox(createMdhd(movie2, track2));


        mdia.addBox(createMdiaHdlr(track2, movie2));


        mdia.addBox(createMinf(track2, movie2));
        return mdia;
    }

    protected Box2 createTrak(Track2 track2, Movie2 movie2) {
        LOG.fine("Creating Track " + track2);
        TrackBox2 trackBox = new TrackBox2();
        trackBox.addBox(createTkhd(movie2, track2));
        trackBox.addBox(createMdia(track2, movie2));
        return trackBox;
    }

    protected DataInformationBox2 createDinf(Movie2 movie2, Track2 track2) {
        DataInformationBox2 dinf = new DataInformationBox2();
        DataReferenceBox2 dref = new DataReferenceBox2();
        dinf.addBox(dref);
        DataEntryUrlBox2 url = new DataEntryUrlBox2();
        url.setFlags(1);
        dref.addBox(url);
        return dinf;
    }

    public void setIntersectionFinder(FragmentIntersectionFinder2 intersectionFinder) {
        this.intersectionFinder = intersectionFinder;
    }

    protected long getDuration(Track2 track2) {
        long duration = 0;
        for (TimeToSampleBox2.Entry entry : track2.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }


}
