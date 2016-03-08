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

import com.coremedia.iso.boxes.TimeToSampleBox2;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry22;
import com.googlecode.mp4parser.authoring.Movie2;
import com.googlecode.mp4parser.authoring.Track2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.Math2.lcm;

/**
 * This <code>FragmentIntersectionFinder</code> cuts the input movie video tracks in
 * fragments of the same length exactly before the sync samples. Audio tracks are cut
 * into pieces of similar length.
 */
public class SyncSampleIntersectFinder2Impl2 implements FragmentIntersectionFinder2 {

    private static Logger LOG = Logger.getLogger(SyncSampleIntersectFinder2Impl2.class.getName());

    /**
     * Gets an array of sample numbers that are meant to be the first sample of each
     * chunk or fragment.
     *
     * @param track2 concerned track
     * @param movie2 the context of the track
     * @return an array containing the ordinal of each fragment's first sample
     */
    public long[] sampleNumbers(Track2 track2, Movie2 movie2) {
        if ("vide".equals(track2.getHandler())) {
            if (track2.getSyncSamples() != null && track2.getSyncSamples().length > 0) {
                List<long[]> times = getSyncSamplesTimestamps(movie2, track2);
                return getCommonIndices(track2.getSyncSamples(), getTimes(movie2, track2), times.toArray(new long[times.size()][]));
            } else {
                throw new RuntimeException("Video Tracks need sync samples. Only tracks other than video may have no sync samples.");
            }
        } else if ("soun".equals(track2.getHandler())) {
            Track2 referenceTrack2 = null;
            for (Track2 candidate : movie2.getTrack2s()) {
                if (candidate.getSyncSamples() != null && candidate.getSyncSamples().length > 0) {
                    referenceTrack2 = candidate;
                }
            }
            if (referenceTrack2 != null) {

                // Gets the reference track's fra
                long[] refSyncSamples = sampleNumbers(referenceTrack2, movie2);

                int refSampleCount = referenceTrack2.getSamples().size();

                long[] syncSamples = new long[refSyncSamples.length];
                long minSampleRate = 192000;
                for (Track2 testTrack2 : movie2.getTrack2s()) {
                    if ("soun".equals(testTrack2.getHandler())) {
                        AudioSampleEntry22 ase = (AudioSampleEntry22) testTrack2.getSampleDescriptionBox().getSampleEntry();
                        if (ase.getSampleRate() < minSampleRate) {
                            minSampleRate = ase.getSampleRate();
                            long sc = testTrack2.getSamples().size();
                            double stretch = (double) sc / refSampleCount;

                            for (int i = 0; i < syncSamples.length; i++) {
                                int start = (int) Math.ceil(stretch * (refSyncSamples[i] - 1)) + 1;
                                syncSamples[i] = start;
                                // The Stretch makes sure that there are as much audio and video chunks!
                            }
                        }
                    }
                }
                AudioSampleEntry22 ase = (AudioSampleEntry22) track2.getSampleDescriptionBox().getSampleEntry();
                double factor = (double) ase.getSampleRate() / (double) minSampleRate;
                if (factor != Math.rint(factor)) { // Not an integer
                    throw new RuntimeException("Sample rates must be a multiple of the lowest sample rate to create a correct file!");
                }
                for (int i = 1; i < syncSamples.length; i++) {
                    syncSamples[i] = (int) (1 + (syncSamples[i] - 1) * factor);
                }
                return syncSamples;
            }
            throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
        } else {
            // Ok, my track has no sync samples - let's find one with sync samples.
            for (Track2 candidate : movie2.getTrack2s()) {
                if (candidate.getSyncSamples() != null && candidate.getSyncSamples().length > 0) {
                    long[] refSyncSamples = sampleNumbers(candidate, movie2);
                    int refSampleCount = candidate.getSamples().size();

                    long[] syncSamples = new long[refSyncSamples.length];
                    long sc = track2.getSamples().size();
                    double stretch = (double) sc / refSampleCount;

                    for (int i = 0; i < syncSamples.length; i++) {
                        int start = (int) Math.ceil(stretch * (refSyncSamples[i] - 1)) + 1;
                        syncSamples[i] = start;
                        // The Stretch makes sure that there are as much audio and video chunks!
                    }
                    return syncSamples;
                }
            }
            throw new RuntimeException("There was absolutely no Track with sync samples. I can't work with that!");
        }


    }

    /**
     * Calculates the timestamp of all tracks' sync samples.
     *
     * @param movie2
     * @param track2
     * @return
     */
    public static List<long[]> getSyncSamplesTimestamps(Movie2 movie2, Track2 track2) {
        List<long[]> times = new LinkedList<long[]>();
        for (Track2 currentTrack2 : movie2.getTrack2s()) {
            if (currentTrack2.getHandler().equals(track2.getHandler())) {
                long[] currentTrackSyncSamples = currentTrack2.getSyncSamples();
                if (currentTrackSyncSamples != null && currentTrackSyncSamples.length > 0) {
                    final long[] currentTrackTimes = getTimes(movie2, currentTrack2);
                    times.add(currentTrackTimes);
                }
            }
        }
        return times;
    }

    public static long[] getCommonIndices(long[] syncSamples, long[] syncSampleTimes, long[]... otherTracksTimes) {
        List<Long> nuSyncSamples = new LinkedList<Long>();
        for (int i = 0; i < syncSampleTimes.length; i++) {
            boolean foundInEveryRef = true;
            for (long[] times : otherTracksTimes) {
                foundInEveryRef &= (Arrays.binarySearch(times, syncSampleTimes[i]) >= 0);
            }
            if (foundInEveryRef) {
                nuSyncSamples.add(syncSamples[i]);
            }
        }
        long[] nuSyncSampleArray = new long[nuSyncSamples.size()];
        for (int i = 0; i < nuSyncSampleArray.length; i++) {
            nuSyncSampleArray[i] = nuSyncSamples.get(i);
        }
        if (nuSyncSampleArray.length < (syncSamples.length * 0.3)) {
            LOG.warning("There are less than 25% of common sync samples in the given track.");
            throw new RuntimeException("There are less than 25% of common sync samples in the given track.");
        } else if (nuSyncSampleArray.length < (syncSamples.length * 0.5)) {
            LOG.fine("There are less than 50% of common sync samples in the given track. This is implausible but I'm ok to continue.");
        } else if (nuSyncSampleArray.length < syncSamples.length) {
            LOG.finest("Common SyncSample positions vs. this tracks SyncSample positions: " + nuSyncSampleArray.length + " vs. " + syncSamples.length);
        }
        return nuSyncSampleArray;
    }


    private static long[] getTimes(Movie2 m, Track2 track2) {
        long[] syncSamples = track2.getSyncSamples();
        long[] syncSampleTimes = new long[syncSamples.length];
        Queue<TimeToSampleBox2.Entry> timeQueue = new LinkedList<TimeToSampleBox2.Entry>(track2.getDecodingTimeEntries());

        int currentSample = 1;  // first syncsample is 1
        long currentDuration = 0;
        long currentDelta = 0;
        int currentSyncSampleIndex = 0;
        long left = 0;

        long timeScale = 1;
        for (Track2 track21 : m.getTrack2s()) {
            if (track21.getTrackMetaData2().getTimescale() != track2.getTrackMetaData2().getTimescale()) {
                timeScale = lcm(timeScale, track21.getTrackMetaData2().getTimescale());
            }
        }


        while (currentSample <= syncSamples[syncSamples.length - 1]) {
            if (currentSample++ == syncSamples[currentSyncSampleIndex]) {
                syncSampleTimes[currentSyncSampleIndex++] = currentDuration * timeScale;
            }
            if (left-- == 0) {
                TimeToSampleBox2.Entry entry = timeQueue.poll();
                left = entry.getCount();
                currentDelta = entry.getDelta();
            }
            currentDuration += currentDelta;
        }
        return syncSampleTimes;

    }
}
