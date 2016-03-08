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

package com.coremedia.iso.boxes.sampleentry;


import com.coremedia.iso.IsoFile2;
import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.googlecode.mp4parser.AbstractBox2;

import java.nio.ByteBuffer;

/**
 * AMR audio format specific subbox of an audio sample entry.
 *
 * @see AudioSampleEntry22
 */
public class AmrSpecificBox2 extends AbstractBox2 {
    public static final String TYPE = "damr";

    private String vendor;
    private int decoderVersion;
    private int modeSet;
    private int modeChangePeriod;
    private int framesPerSample;

    public AmrSpecificBox2() {
        super(TYPE);
    }

    public String getVendor() {
        return vendor;
    }

    public int getDecoderVersion() {
        return decoderVersion;
    }

    public int getModeSet() {
        return modeSet;
    }

    public int getModeChangePeriod() {
        return modeChangePeriod;
    }

    public int getFramesPerSample() {
        return framesPerSample;
    }

    protected long getContentSize() {
        return 9;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        byte[] v = new byte[4];
        content.get(v);
        vendor = IsoFile2.bytesToFourCC(v);

        decoderVersion = IsoTypeReader2.readUInt8(content);
        modeSet = IsoTypeReader2.readUInt16(content);
        modeChangePeriod = IsoTypeReader2.readUInt8(content);
        framesPerSample = IsoTypeReader2.readUInt8(content);

    }


    public void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(IsoFile2.fourCCtoBytes(vendor));
        IsoTypeWriter2.writeUInt8(byteBuffer, decoderVersion);
        IsoTypeWriter2.writeUInt16(byteBuffer, modeSet);
        IsoTypeWriter2.writeUInt8(byteBuffer, modeChangePeriod);
        IsoTypeWriter2.writeUInt8(byteBuffer, framesPerSample);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("AmrSpecificBox[vendor=").append(getVendor());
        buffer.append(";decoderVersion=").append(getDecoderVersion());
        buffer.append(";modeSet=").append(getModeSet());
        buffer.append(";modeChangePeriod=").append(getModeChangePeriod());
        buffer.append(";framesPerSample=").append(getFramesPerSample());
        buffer.append("]");
        return buffer.toString();
    }
}
