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

/**
 * <code>
 * Box Type: 'mvhd'<br>
 * Container: {@link MovieBox2} ('moov')<br>
 * Mandatory: Yes<br>
 * Quantity: Exactly one<br><br>
 * </code>
 * This box defines overall information which is media-independent, and relevant to the entire presentation
 * considered as a whole.
 */
public class MovieHeaderBox2 extends AbstractFullBox2 {
    private long creationTime;
    private long modificationTime;
    private long timescale;
    private long duration;
    private double rate = 1.0;
    private float volume = 1.0f;
    private long[] matrix = new long[]{0x00010000, 0, 0, 0, 0x00010000, 0, 0, 0, 0x40000000};
    private long nextTrackId;
    public static final String TYPE = "mvhd";

    public MovieHeaderBox2() {
        super(TYPE);
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public long getTimescale() {
        return timescale;
    }

    public long getDuration() {
        return duration;
    }

    public double getRate() {
        return rate;
    }

    public float getVolume() {
        return volume;
    }

    public long[] getMatrix() {
        return matrix;
    }

    public long getNextTrackId() {
        return nextTrackId;
    }

    protected long getContentSize() {
        long contentSize = 4;
        if (getVersion() == 1) {
            contentSize += 28;
        } else {
            contentSize += 16;
        }
        contentSize += 80;
        return contentSize;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (getVersion() == 1) {
            creationTime = IsoTypeReader2.readUInt64(content);
            modificationTime = IsoTypeReader2.readUInt64(content);
            timescale = IsoTypeReader2.readUInt32(content);
            duration = IsoTypeReader2.readUInt64(content);
        } else {
            creationTime = IsoTypeReader2.readUInt32(content);
            modificationTime = IsoTypeReader2.readUInt32(content);
            timescale = IsoTypeReader2.readUInt32(content);
            duration = IsoTypeReader2.readUInt32(content);
        }
        rate = IsoTypeReader2.readFixedPoint1616(content);
        volume = IsoTypeReader2.readFixedPoint88(content);
        IsoTypeReader2.readUInt16(content);
        IsoTypeReader2.readUInt32(content);
        IsoTypeReader2.readUInt32(content);
        matrix = new long[9];
        for (int i = 0; i < 9; i++) {
            matrix[i] = IsoTypeReader2.readUInt32(content);
        }
        for (int i = 0; i < 6; i++) {
            IsoTypeReader2.readUInt32(content);
        }
        nextTrackId = IsoTypeReader2.readUInt32(content);

    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("MovieHeaderBox[");
        result.append("creationTime=").append(getCreationTime());
        result.append(";");
        result.append("modificationTime=").append(getModificationTime());
        result.append(";");
        result.append("timescale=").append(getTimescale());
        result.append(";");
        result.append("duration=").append(getDuration());
        result.append(";");
        result.append("rate=").append(getRate());
        result.append(";");
        result.append("volume=").append(getVolume());
        for (int i = 0; i < matrix.length; i++) {
            result.append(";");
            result.append("matrix").append(i).append("=").append(matrix[i]);
        }
        result.append(";");
        result.append("nextTrackId=").append(getNextTrackId());
        result.append("]");
        return result.toString();
    }


    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (getVersion() == 1) {
            IsoTypeWriter2.writeUInt64(byteBuffer, creationTime);
            IsoTypeWriter2.writeUInt64(byteBuffer, modificationTime);
            IsoTypeWriter2.writeUInt32(byteBuffer, timescale);
            IsoTypeWriter2.writeUInt64(byteBuffer, duration);
        } else {
            IsoTypeWriter2.writeUInt32(byteBuffer, creationTime);
            IsoTypeWriter2.writeUInt32(byteBuffer, modificationTime);
            IsoTypeWriter2.writeUInt32(byteBuffer, timescale);
            IsoTypeWriter2.writeUInt32(byteBuffer, duration);
        }
        IsoTypeWriter2.writeFixedPont1616(byteBuffer, rate);
        IsoTypeWriter2.writeFixedPont88(byteBuffer, volume);
        IsoTypeWriter2.writeUInt16(byteBuffer, 0);
        IsoTypeWriter2.writeUInt32(byteBuffer, 0);
        IsoTypeWriter2.writeUInt32(byteBuffer, 0);


        for (int i = 0; i < 9; i++) {
            IsoTypeWriter2.writeUInt32(byteBuffer, matrix[i]);
        }
        for (int i = 0; i < 6; i++) {
            IsoTypeWriter2.writeUInt32(byteBuffer, 0);
        }
        IsoTypeWriter2.writeUInt32(byteBuffer, nextTrackId);
    }


    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setTimescale(long timescale) {
        this.timescale = timescale;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setMatrix(long[] matrix) {
        this.matrix = matrix;
    }

    public void setNextTrackId(long nextTrackId) {
        this.nextTrackId = nextTrackId;
    }
}
