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
 *
 */
public class RecordingYearBox2 extends AbstractFullBox2 {
    public static final String TYPE = "yrrc";

    int recordingYear;

    public RecordingYearBox2() {
        super(TYPE);
    }


    protected long getContentSize() {
        return 6;
    }

    public int getRecordingYear() {
        return recordingYear;
    }

    public void setRecordingYear(int recordingYear) {
        this.recordingYear = recordingYear;
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        recordingYear = IsoTypeReader2.readUInt16(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeUInt16(byteBuffer, recordingYear);
    }

}
