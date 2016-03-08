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

import com.googlecode.mp4parser.AbstractContainerBox2;

/**
 * The sample table contains all the time and data indexing of the media samples in a track. Using the tables
 * here, it is possible to locate samples in time, determine their type (e.g. I-frame or not), and determine their
 * size, container, and offset into that container.  <br>
 * If the track that contains the Sample Table Box references no data, then the Sample Table Box does not need
 * to contain any sub-boxes (this is not a very useful media track).                                          <br>
 * If the track that the Sample Table Box is contained in does reference data, then the following sub-boxes are
 * required: Sample Description, Sample Size, Sample To Chunk, and Chunk Offset. Further, the Sample
 * Description Box shall contain at least one entry. A Sample Description Box is required because it contains the
 * data reference index field which indicates which Data Reference Box to use to retrieve the media samples.
 * Without the Sample Description, it is not possible to determine where the media samples are stored. The Sync
 * Sample Box is optional. If the Sync Sample Box is not present, all samples are sync samples.<br>
 * Annex A provides a narrative description of random access using the structures defined in the Sample Table
 * Box.
 */
public class SampleTableBox2 extends AbstractContainerBox2 {
    public static final String TYPE = "stbl";

    public SampleTableBox2() {
        super(TYPE);
    }

    public SampleDescriptionBox2 getSampleDescriptionBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof SampleDescriptionBox2) {
                return (SampleDescriptionBox2) box2;
            }
        }
        return null;
    }

    public SampleSizeBox2 getSampleSizeBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof SampleSizeBox2) {
                return (SampleSizeBox2) box2;
            }
        }
        return null;
    }

    public SampleToChunkBox2 getSampleToChunkBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof SampleToChunkBox2) {
                return (SampleToChunkBox2) box2;
            }
        }
        return null;
    }

    public ChunkOffsetBox2 getChunkOffsetBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof ChunkOffsetBox2) {
                return (ChunkOffsetBox2) box2;
            }
        }
        return null;
    }

    public void setChunkOffsetBox(ChunkOffsetBox2 b) {
        for (int i = 0; i < box2s.size(); i++) {
            Box2 box2 = box2s.get(i);
            if (box2 instanceof ChunkOffsetBox2) {
                box2s.set(i, b);
            }
        }
    }

    public TimeToSampleBox2 getTimeToSampleBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof TimeToSampleBox2) {
                return (TimeToSampleBox2) box2;
            }
        }
        return null;
    }

    public SyncSampleBox2 getSyncSampleBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof SyncSampleBox2) {
                return (SyncSampleBox2) box2;
            }
        }
        return null;
    }

    public CompositionTimeToSample2 getCompositionTimeToSample() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof CompositionTimeToSample2) {
                return (CompositionTimeToSample2) box2;
            }
        }
        return null;
    }

    public SampleDependencyTypeBox2 getSampleDependencyTypeBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof SampleDependencyTypeBox2) {
                return (SampleDependencyTypeBox2) box2;
            }
        }
        return null;
    }

}
