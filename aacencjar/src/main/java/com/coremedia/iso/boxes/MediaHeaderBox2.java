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
 * This box defines overall information which is media-independent, and relevant to the entire presentation
 * considered as a whole.
 */
public class MediaHeaderBox2 extends AbstractFullBox2 {
    public static final String TYPE = "mdhd";


    private long creationTime;
    private long modificationTime;
    private long timescale;
    private long duration;
    private String language;

    public MediaHeaderBox2() {
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

    public String getLanguage() {
        return language;
    }

    protected long getContentSize() {
        long contentSize = 4;
        if (getVersion() == 1) {
            contentSize += 8 + 8 + 4 + 8;
        } else {
            contentSize += 4 + 4 + 4 + 4;
        }
        contentSize += 2;
        contentSize += 2;
        return contentSize;

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

    public void setLanguage(String language) {
        this.language = language;
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
        language = IsoTypeReader2.readIso639(content);
        IsoTypeReader2.readUInt16(content);
    }


    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("MeditHeaderBox[");
        result.append("creationTime=").append(getCreationTime());
        result.append(";");
        result.append("modificationTime=").append(getModificationTime());
        result.append(";");
        result.append("timescale=").append(getTimescale());
        result.append(";");
        result.append("duration=").append(getDuration());
        result.append(";");
        result.append("language=").append(getLanguage());
        result.append("]");
        return result.toString();
    }

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
        IsoTypeWriter2.writeIso639(byteBuffer, language);
        IsoTypeWriter2.writeUInt16(byteBuffer, 0);
    }
}
