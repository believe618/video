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


import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * This box containes the sample count and a table giving the size in bytes of each sample.
 * Defined in ISO/IEC 14496-12.
 */
public class SampleSizeBox2 extends AbstractFullBox2 {
    private long sampleSize;
    private long[] sampleSizes = new long[0];
    public static final String TYPE = "stsz";
    int sampleCount;

    public SampleSizeBox2() {
        super(TYPE);
    }

    /**
     * Returns the field sample size.
     * If sampleSize > 0 every sample has the same size.
     * If sampleSize == 0 the samples have different size as stated in the sampleSizes field.
     *
     * @return the sampleSize field
     */
    public long getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(long sampleSize) {
        this.sampleSize = sampleSize;
    }


    public long getSampleSizeAtIndex(int index) {
        if (sampleSize > 0) {
            return sampleSize;
        } else {
            return sampleSizes[index];
        }
    }

    public long getSampleCount() {
        if (sampleSize > 0) {
            return sampleCount;
        } else {
            return sampleSizes.length;
        }

    }

    public long[] getSampleSizes() {
        return sampleSizes;
    }

    public void setSampleSizes(long[] sampleSizes) {
        this.sampleSizes = sampleSizes;
    }

    protected long getContentSize() {
        return 12 + (sampleSize == 0 ? sampleSizes.length * 4 : 0);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        sampleSize = IsoTypeReader2.readUInt32(content);
        sampleCount = l2i(IsoTypeReader2.readUInt32(content));

        if (sampleSize == 0) {
            sampleSizes = new long[(int) sampleCount];

            for (int i = 0; i < sampleCount; i++) {
                sampleSizes[i] = IsoTypeReader2.readUInt32(content);
            }
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeUInt32(byteBuffer, sampleSize);

        if (sampleSize == 0) {
            IsoTypeWriter2.writeUInt32(byteBuffer, sampleSizes.length);
            for (long sampleSize1 : sampleSizes) {
                IsoTypeWriter2.writeUInt32(byteBuffer, sampleSize1);
            }
        } else {
            IsoTypeWriter2.writeUInt32(byteBuffer, sampleCount);
        }

    }

    public String toString() {
        return "SampleSizeBox[sampleSize=" + getSampleSize() + ";sampleCount=" + getSampleCount() + "]";
    }
}
