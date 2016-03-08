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
import com.coremedia.iso.boxes.SampleSizeBox2;
import com.coremedia.iso.boxes.SampleTableBox2;
import com.coremedia.iso.boxes.SampleToChunkBox2;
import com.coremedia.iso.boxes.StaticChunkOffsetBox2;
import com.coremedia.iso.boxes.SyncSampleBox2;
import com.coremedia.iso.boxes.TimeToSampleBox2;
import com.coremedia.iso.boxes.TrackBox2;
import com.coremedia.iso.boxes.TrackHeaderBox2;
import com.googlecode.mp4parser.authoring.DateHelper2;
import com.googlecode.mp4parser.authoring.Movie2;
import com.googlecode.mp4parser.authoring.Track2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * Creates a plain MP4 file from a video. Plain as plain can be.
 */
public class DefaultMp4Builder22 implements Mp4Builder2 {
    Set<StaticChunkOffsetBox2> chunkOffsetBoxes = new HashSet<StaticChunkOffsetBox2>();
    private static Logger LOG = Logger.getLogger(DefaultMp4Builder22.class.getName());

    HashMap<Track2, List<ByteBuffer>> track2Sample = new HashMap<Track2, List<ByteBuffer>>();
    HashMap<Track2, long[]> track2SampleSizes = new HashMap<Track2, long[]>();
    private FragmentIntersectionFinder2 intersectionFinder = new TwoSecondIntersectionFinder2();

    List<String> hdlrs = new LinkedList<String>();

    public void setAllowedHandlers(List<String> hdlrs) {
        this.hdlrs = hdlrs;
    }

    public void setIntersectionFinder(FragmentIntersectionFinder2 intersectionFinder) {
        this.intersectionFinder = intersectionFinder;
    }

    /**
     * {@inheritDoc}
     */
    public IsoFile2 build(Movie2 movie2)  {
        LOG.fine("Creating movie " + movie2);
        for (Track2 track2 : movie2.getTrack2s()) {
            // getting the samples may be a time consuming activity
            List<ByteBuffer> samples = track2.getSamples();
            track2Sample.put(track2, samples);
            long[] sizes = new long[samples.size()];
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = samples.get(i).limit();
            }
            track2SampleSizes.put(track2, sizes);
        }

        IsoFile2 isoFile = new IsoFile2();
        // ouch that is ugly but I don't know how to do it else
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");

        isoFile.addBox(new FileTypeBox2("isom", 0, minorBrands));
        isoFile.addBox(createMovieBox(movie2));
        InterleaveChunkMdat mdat = new InterleaveChunkMdat(movie2);
        isoFile.addBox(mdat);

        /*
        dataOffset is where the first sample starts. In this special mdat the samples always start
        at offset 16 so that we can use the same offset for large boxes and small boxes
         */
        long dataOffset = mdat.getDataOffset();
        for (StaticChunkOffsetBox2 chunkOffsetBox : chunkOffsetBoxes) {
            long[] offsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += dataOffset;
            }
        }


        return isoFile;
    }

    private MovieBox2 createMovieBox(Movie2 movie2) {
        MovieBox2 movieBox = new MovieBox2();
        List<Box2> movieBox2Children = new LinkedList<Box2>();
        MovieHeaderBox2 mvhd = new MovieHeaderBox2();
        mvhd.setVersion(1);
        mvhd.setCreationTime(DateHelper2.convert(new Date()));
        mvhd.setModificationTime(DateHelper2.convert(new Date()));

        long movieTimeScale = getTimescale(movie2);
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
        movieBox2Children.add(mvhd);
        for (Track2 track2 : movie2.getTrack2s()) {
            movieBox2Children.add(createTrackBox(track2, movie2));
        }
        // metadata here
        movieBox.setBox2s(movieBox2Children);
        Box2 udta = createUdta(movie2);
        if (udta != null) {
            movieBox.addBox(udta);
        }
        return movieBox;

    }

    /**
     * Override to create a user data box that may contain metadata.
     * @return a 'udta' box or <code>null</code> if none provided
     */
    protected Box2 createUdta(Movie2 movie2) {
        return null;
    }

    private TrackBox2 createTrackBox(Track2 track2, Movie2 movie2) {

        LOG.info("Creating Mp4TrackImpl " + track2);
        TrackBox2 trackBox = new TrackBox2();
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
        tkhd.setDuration(getDuration(track2) * getTimescale(movie2) / track2.getTrackMetaData2().getTimescale());
        tkhd.setHeight(track2.getTrackMetaData2().getHeight());
        tkhd.setWidth(track2.getTrackMetaData2().getWidth());
        tkhd.setLayer(track2.getTrackMetaData2().getLayer());
        tkhd.setModificationTime(DateHelper2.convert(new Date()));
        tkhd.setTrackId(track2.getTrackMetaData2().getTrackId());
        tkhd.setVolume(track2.getTrackMetaData2().getVolume());
        trackBox.addBox(tkhd);

/*
        EditBox edit = new EditBox();
        EditListBox editListBox = new EditListBox();
        editListBox.setEntries(Collections.singletonList(
                new EditListBox.Entry(editListBox, (long) (track.getTrackMetaData().getStartTime() * getTimescale(movie)), -1, 1)));
        edit.addBox(editListBox);
        trackBox.addBox(edit);
*/

        MediaBox2 mdia = new MediaBox2();
        trackBox.addBox(mdia);
        MediaHeaderBox2 mdhd = new MediaHeaderBox2();
        mdhd.setCreationTime(DateHelper2.convert(track2.getTrackMetaData2().getCreationTime()));
        mdhd.setDuration(getDuration(track2));
        mdhd.setTimescale(track2.getTrackMetaData2().getTimescale());
        mdhd.setLanguage(track2.getTrackMetaData2().getLanguage());
        mdia.addBox(mdhd);
        HandlerBox2 hdlr = new HandlerBox2();
        mdia.addBox(hdlr);

        hdlr.setHandlerType(track2.getHandler());

        MediaInformationBox2 minf = new MediaInformationBox2();
        minf.addBox(track2.getMediaHeaderBox());

        // dinf: all these three boxes tell us is that the actual
        // data is in the current file and not somewhere external
        DataInformationBox2 dinf = new DataInformationBox2();
        DataReferenceBox2 dref = new DataReferenceBox2();
        dinf.addBox(dref);
        DataEntryUrlBox2 url = new DataEntryUrlBox2();
        url.setFlags(1);
        dref.addBox(url);
        minf.addBox(dinf);
        //

        SampleTableBox2 stbl = new SampleTableBox2();

        stbl.addBox(track2.getSampleDescriptionBox());

        List<TimeToSampleBox2.Entry> decodingTimeToSampleEntries = track2.getDecodingTimeEntries();
        if (decodingTimeToSampleEntries != null && !track2.getDecodingTimeEntries().isEmpty()) {
            TimeToSampleBox2 stts = new TimeToSampleBox2();
            stts.setEntries(track2.getDecodingTimeEntries());
            stbl.addBox(stts);
        }

        List<CompositionTimeToSample2.Entry> compositionTimeToSampleEntries = track2.getCompositionTimeEntries();
        if (compositionTimeToSampleEntries != null && !compositionTimeToSampleEntries.isEmpty()) {
            CompositionTimeToSample2 ctts = new CompositionTimeToSample2();
            ctts.setEntries(compositionTimeToSampleEntries);
            stbl.addBox(ctts);
        }

        long[] syncSamples = track2.getSyncSamples();
        if (syncSamples != null && syncSamples.length > 0) {
            SyncSampleBox2 stss = new SyncSampleBox2();
            stss.setSampleNumber(syncSamples);
            stbl.addBox(stss);
        }

        if (track2.getSampleDependencies() != null && !track2.getSampleDependencies().isEmpty()) {
            SampleDependencyTypeBox2 sdtp = new SampleDependencyTypeBox2();
            sdtp.setEntries(track2.getSampleDependencies());
            stbl.addBox(sdtp);
        }
        int chunkSize[] = getChunkSizes(track2, movie2);
        SampleToChunkBox2 stsc = new SampleToChunkBox2();
        stsc.setEntries(new LinkedList<SampleToChunkBox2.Entry>());
        long lastChunkSize = Integer.MIN_VALUE; // to be sure the first chunks hasn't got the same size
        for (int i = 0; i < chunkSize.length; i++) {
            // The sample description index references the sample description box
            // that describes the samples of this chunk. My Tracks cannot have more
            // than one sample description box. Therefore 1 is always right
            // the first chunk has the number '1'
            if (lastChunkSize != chunkSize[i]) {
                stsc.getEntries().add(new SampleToChunkBox2.Entry(i + 1, chunkSize[i], 1));
                lastChunkSize = chunkSize[i];
            }
        }
        stbl.addBox(stsc);

        SampleSizeBox2 stsz = new SampleSizeBox2();
        stsz.setSampleSizes(track2SampleSizes.get(track2));

        stbl.addBox(stsz);
        // The ChunkOffsetBox we create here is just a stub
        // since we haven't created the whole structure we can't tell where the
        // first chunk starts (mdat box). So I just let the chunk offset
        // start at zero and I will add the mdat offset later.
        StaticChunkOffsetBox2 stco = new StaticChunkOffsetBox2();
        this.chunkOffsetBoxes.add(stco);
        long offset = 0;
        long[] chunkOffset = new long[chunkSize.length];
        // all tracks have the same number of chunks
        LOG.fine("Calculating chunk offsets for track_" + track2.getTrackMetaData2().getTrackId());
        for (int i = 0; i < chunkSize.length; i++) {
            // The filelayout will be:
            // chunk_1_track_1,... ,chunk_1_track_n, chunk_2_track_1,... ,chunk_2_track_n, ... , chunk_m_track_1,... ,chunk_m_track_n
            // calculating the offsets
            LOG.finer("Calculating chunk offsets for track_" + track2.getTrackMetaData2().getTrackId() + " chunk " + i);
            for (Track2 current : movie2.getTrack2s()) {
                LOG.finest("Adding offsets of track_" + current.getTrackMetaData2().getTrackId());
                int[] chunkSizes = getChunkSizes(current, movie2);
                long firstSampleOfChunk = 0;
                for (int j = 0; j < i; j++) {
                    firstSampleOfChunk += chunkSizes[j];
                }
                if (current == track2) {
                    chunkOffset[i] = offset;
                }
                for (int j = l2i(firstSampleOfChunk); j < firstSampleOfChunk + chunkSizes[i]; j++) {
                    offset += track2SampleSizes.get(current)[j];
                }
            }
        }
        stco.setChunkOffsets(chunkOffset);
        stbl.addBox(stco);
        minf.addBox(stbl);
        mdia.addBox(minf);

        return trackBox;
    }

    private class InterleaveChunkMdat implements Box2 {
        List<Track2> track2s;
        List<ByteBuffer> samples = new LinkedList<ByteBuffer>();
        ContainerBox2 parent;

        long contentSize = 0;

        public ContainerBox2 getParent() {
            return parent;
        }

        public void setParent(ContainerBox2 parent) {
            this.parent = parent;
        }

        public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser2 boxParser) throws IOException {
        }

        private InterleaveChunkMdat(Movie2 movie2) {

            track2s = movie2.getTrack2s();
            Map<Track2, int[]> chunks = new HashMap<Track2, int[]>();
            for (Track2 track2 : movie2.getTrack2s()) {
                chunks.put(track2, getChunkSizes(track2, movie2));
            }

            for (int i = 0; i < chunks.values().iterator().next().length; i++) {
                for (Track2 track2 : track2s) {

                    int[] chunkSizes = chunks.get(track2);
                    long firstSampleOfChunk = 0;
                    for (int j = 0; j < i; j++) {
                        firstSampleOfChunk += chunkSizes[j];
                    }

                    for (int j = l2i(firstSampleOfChunk); j < firstSampleOfChunk + chunkSizes[i]; j++) {

                        ByteBuffer s = DefaultMp4Builder22.this.track2Sample.get(track2).get(j);
                        contentSize += s.limit();
                        samples.add((ByteBuffer) s.rewind());
                    }

                }

            }

        }

        public long getDataOffset() {
            Box2 b = this;
            long offset = 16;
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


        public String getType() {
            return "mdat";
        }

        public long getSize() {
            return 16 + contentSize;
        }

        private boolean isSmallBox(long contentSize) {
            return (contentSize + 8) < 4294967296L;
        }


        public void getBox(WritableByteChannel writableByteChannel) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(16);
            long size = getSize();
            if (isSmallBox(size)) {
                IsoTypeWriter2.writeUInt32(bb, size);
            } else {
                IsoTypeWriter2.writeUInt32(bb, 1);
            }
            bb.put(IsoFile2.fourCCtoBytes("mdat"));
            if (isSmallBox(size)) {
                bb.put(new byte[8]);
            } else {
                IsoTypeWriter2.writeUInt64(bb, size);
            }
            bb.rewind();
            writableByteChannel.write(bb);
          /*  if (writableByteChannel instanceof GatheringByteChannel) {
                List<ByteBuffer> nuSamples = unifyAdjacentBuffers(samples);

                int STEPSIZE = 1024;
                for (int i = 0; i < Math.ceil((double) nuSamples.size() / STEPSIZE); i++) {
                    List<ByteBuffer> sublist = nuSamples.subList(
                            i * STEPSIZE, // start
                            (i + 1) * STEPSIZE < nuSamples.size() ? (i + 1) * STEPSIZE : nuSamples.size()); // end
                    ByteBuffer sampleArray[] = sublist.toArray(new ByteBuffer[sublist.size()]);
                    do {
                        ((GatheringByteChannel) writableByteChannel).write(sampleArray);
                    } while (sampleArray[sampleArray.length - 1].remaining() > 0);
                }
                //System.err.println(bytesWritten);
            } else {*/
                for (ByteBuffer sample : samples) {
                    sample.rewind();
                    writableByteChannel.write(sample);
                }
           // }
        }

    }

    /**
     * Gets the chunk sizes for the given track.
     *
     * @param track2
     * @param movie2
     * @return
     */
    int[] getChunkSizes(Track2 track2, Movie2 movie2) {

        long[] referenceChunkStarts = intersectionFinder.sampleNumbers(track2, movie2);
        int[] chunkSizes = new int[referenceChunkStarts.length];


        for (int i = 0; i < referenceChunkStarts.length; i++) {
            long start = referenceChunkStarts[i] - 1;
            long end;
            if (referenceChunkStarts.length == i + 1) {
                end = track2.getSamples().size() - 1;
            } else {
                end = referenceChunkStarts[i + 1] - 1;
            }

            chunkSizes[i] = l2i(end - start);
            // The Stretch makes sure that there are as much audio and video chunks!
        }
        assert DefaultMp4Builder22.this.track2Sample.get(track2).size() == sum(chunkSizes) : "The number of samples and the sum of all chunk lengths must be equal";
        return chunkSizes;


    }


    private static long sum(int[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }

    protected static long getDuration(Track2 track2) {
        long duration = 0;
        for (TimeToSampleBox2.Entry entry : track2.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    public long getTimescale(Movie2 movie2) {
        long timescale = movie2.getTrack2s().iterator().next().getTrackMetaData2().getTimescale();
        for (Track2 track2 : movie2.getTrack2s()) {
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

    public List<ByteBuffer> unifyAdjacentBuffers(List<ByteBuffer> samples) {
        ArrayList<ByteBuffer> nuSamples = new ArrayList<ByteBuffer>(samples.size());
        for (ByteBuffer buffer : samples) {
            int lastIndex = nuSamples.size() - 1;
            if (lastIndex >= 0 && buffer.hasArray() && nuSamples.get(lastIndex).hasArray() && buffer.array() == nuSamples.get(lastIndex).array() &&
                    nuSamples.get(lastIndex).arrayOffset() + nuSamples.get(lastIndex).limit() == buffer.arrayOffset()) {
                ByteBuffer oldBuffer = nuSamples.remove(lastIndex);
                ByteBuffer nu = ByteBuffer.wrap(buffer.array(), oldBuffer.arrayOffset(), oldBuffer.limit() + buffer.limit()).slice();
                // We need to slice here since wrap([], offset, length) just sets position and not the arrayOffset.
                nuSamples.add(nu);
            } else if (lastIndex >= 0 &&
                    buffer instanceof MappedByteBuffer && nuSamples.get(lastIndex) instanceof MappedByteBuffer &&
                    nuSamples.get(lastIndex).limit() == nuSamples.get(lastIndex).capacity() - buffer.capacity()) {
                // This can go wrong - but will it?
                ByteBuffer oldBuffer = nuSamples.get(lastIndex);
                oldBuffer.limit(buffer.limit() + oldBuffer.limit());
            } else {
                nuSamples.add(buffer);
            }
        }
        return nuSamples;
    }
}
