package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoFile2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ChunkOffsetBox2;
import com.coremedia.iso.boxes.SampleSizeBox2;
import com.coremedia.iso.boxes.SampleToChunkBox2;
import com.coremedia.iso.boxes.TrackBox2;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox2;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox2;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox2;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox2;
import com.coremedia.iso.boxes.fragment.TrackRunBox2;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * Creates a list of <code>ByteBuffer</code>s that represent the samples of a given track.
 */
public class SampleList2 extends AbstractList<ByteBuffer> {

    Map<Long, Long> offsets2Sizes;
    List<Long> offsetKeys = null;
    IsoFile2 isoFile2;
    HashMap<MediaDataBox2, Long> mdatStartCache = new HashMap<MediaDataBox2, Long>();
    HashMap<MediaDataBox2, Long> mdatEndCache = new HashMap<MediaDataBox2, Long>();
    ArrayList<MediaDataBox2> mdats = new ArrayList<MediaDataBox2>(1);

    /**
     * Gets a sorted random access optimized list of all sample offsets.
     * Basically it is a map from sample number to sample offset.
     *
     * @return the sorted list of sample offsets
     */
    public List<Long> getOffsetKeys() {
        if (offsetKeys == null) {
            List<Long> offsetKeys = new ArrayList<Long>(offsets2Sizes.size());
            for (Long aLong : offsets2Sizes.keySet()) {
                offsetKeys.add(aLong);
            }
            Collections.sort(offsetKeys);
            this.offsetKeys = offsetKeys;
        }
        return offsetKeys;
    }


    public SampleList2(TrackBox2 trackBox) {
        this.isoFile2 = trackBox.getIsoFile(); // where are we?
        offsets2Sizes = new HashMap<Long, Long>();

        // find all mdats first to be able to use them later with explicitly looking them up
        long currentOffset = 0;
        for (Box2 b : isoFile2.getBox2s()) {
            long currentSize = b.getSize();
            if ("mdat".equals(b.getType())) {
                if (b instanceof MediaDataBox2) {
                    long contentOffset = currentOffset + ((MediaDataBox2) b).getHeader().limit();
                    mdatStartCache.put((MediaDataBox2) b, contentOffset);
                    mdatEndCache.put((MediaDataBox2) b, contentOffset + currentSize);
                    mdats.add((MediaDataBox2) b);
                } else {
                    throw new RuntimeException("Sample need to be in mdats and mdats need to be instanceof MediaDataBox");
                }
            }
            currentOffset += currentSize;
        }


        // first we get all sample from the 'normal' MP4 part.
        // if there are none - no problem.

        SampleSizeBox2 sampleSizeBox = trackBox.getSampleTableBox().getSampleSizeBox();
        ChunkOffsetBox2 chunkOffsetBox = trackBox.getSampleTableBox().getChunkOffsetBox();
        SampleToChunkBox2 sampleToChunkBox = trackBox.getSampleTableBox().getSampleToChunkBox();


        if (sampleToChunkBox != null && sampleToChunkBox.getEntries().size() > 0 && chunkOffsetBox != null &&
                chunkOffsetBox.getChunkOffsets().length > 0 && sampleSizeBox != null && sampleSizeBox.getSampleCount() > 0) {
            long[] numberOfSamplesInChunk = sampleToChunkBox.blowup(chunkOffsetBox.getChunkOffsets().length);
            if (sampleSizeBox.getSampleSize() > 0) {
                // Every sample has the same size!
                // no need to store each size separately
                // this happens when people use raw audio formats in MP4 (are you stupid guys???)
                offsets2Sizes = new DummyMap2<Long, Long>(sampleSizeBox.getSampleSize());
                long sampleSize = sampleSizeBox.getSampleSize();
                for (int i = 0; i < numberOfSamplesInChunk.length; i++) {
                    long thisChunksNumberOfSamples = numberOfSamplesInChunk[i];
                    long sampleOffset = chunkOffsetBox.getChunkOffsets()[i];
                    for (int j = 0; j < thisChunksNumberOfSamples; j++) {
                        offsets2Sizes.put(sampleOffset, sampleSize);
                        sampleOffset += sampleSize;
                    }
                }
            } else {
                // the normal case where all samples have different sizes
                int sampleIndex = 0;
                long sampleSizes[] = sampleSizeBox.getSampleSizes();
                for (int i = 0; i < numberOfSamplesInChunk.length; i++) {
                    long thisChunksNumberOfSamples = numberOfSamplesInChunk[i];
                    long sampleOffset = chunkOffsetBox.getChunkOffsets()[i];
                    for (int j = 0; j < thisChunksNumberOfSamples; j++) {
                        long sampleSize = sampleSizes[sampleIndex];
                        offsets2Sizes.put(sampleOffset, sampleSize);
                        sampleOffset += sampleSize;
                        sampleIndex++;
                    }
                }

            }
        }

        // Next we add all samples from the fragments
        // in most cases - I've never seen it different it's either normal or fragmented.

        List<MovieExtendsBox2> movieExtendsBoxes = trackBox.getParent().getBoxes(MovieExtendsBox2.class);

        if (movieExtendsBoxes.size() > 0) {
            List<TrackExtendsBox2> trackExtendsBoxes = movieExtendsBoxes.get(0).getBoxes(TrackExtendsBox2.class);
            for (TrackExtendsBox2 trackExtendsBox : trackExtendsBoxes) {
                if (trackExtendsBox.getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    for (MovieFragmentBox2 movieFragmentBox : trackBox.getIsoFile().getBoxes(MovieFragmentBox2.class)) {
                        offsets2Sizes.putAll(getOffsets(movieFragmentBox, trackBox.getTrackHeaderBox().getTrackId()));
                    }
                }
            }
        }


        // We have now a map from all sample offsets to their sizes
    }


    @Override
    public int size() {
        return offsets2Sizes.size();
    }


    @Override
    public ByteBuffer get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);
        int sampleSize = l2i(offsets2Sizes.get(offset));

        for (MediaDataBox2 mediaDataBox : mdats) {
            long start = mdatStartCache.get(mediaDataBox);
            long end = mdatEndCache.get(mediaDataBox);
            if ((start <= offset) && (offset + sampleSize <= end)) {
                ByteBuffer bb = mediaDataBox.getContent();
                bb.position(l2i(offset - start));
                ByteBuffer sample = bb.slice();
                sample.limit(sampleSize);
                return sample;
            }
        }

        throw new RuntimeException("The sample with offset " + offset + " and size " + sampleSize + " is NOT located within an mdat");
    }

    Map<Long, Long> getOffsets(MovieFragmentBox2 moof, long trackId) {
        Map<Long, Long> offsets2Sizes = new HashMap<Long, Long>();
        List<TrackFragmentBox2> traf = moof.getBoxes(TrackFragmentBox2.class);
        for (TrackFragmentBox2 trackFragmentBox : traf) {
            if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                long baseDataOffset;
                if (trackFragmentBox.getTrackFragmentHeaderBox().hasBaseDataOffset()) {
                    baseDataOffset = trackFragmentBox.getTrackFragmentHeaderBox().getBaseDataOffset();
                } else {
                    baseDataOffset = moof.getOffset();
                }

                for (TrackRunBox2 trun : trackFragmentBox.getBoxes(TrackRunBox2.class)) {
                    long sampleBaseOffset = baseDataOffset + trun.getDataOffset();
                    long[] sampleOffsets = trun.getSampleOffsets();
                    long[] sampleSizes = trun.getSampleSizes();
                    for (int i = 0; i < sampleSizes.length; i++) {
                        offsets2Sizes.put(sampleOffsets[i] + sampleBaseOffset, sampleSizes[i]);
                    }
                }
            }
        }
        return offsets2Sizes;
    }

}