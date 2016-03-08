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

package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Vodafone specific box. Usage unclear.
 */
public class ContentDistributorIdBox2 extends AbstractFullBox2 {
    public static final String TYPE = "cdis";

    private String language;
    private String contentDistributorId;

    public ContentDistributorIdBox2() {
        super(TYPE);
    }

    public String getLanguage() {
        return language;
    }

    public String getContentDistributorId() {
        return contentDistributorId;
    }

    protected long getContentSize() {
        return 2 + Utf82.utf8StringLengthInBytes(contentDistributorId) + 5;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader2.readIso639(content);
        contentDistributorId = IsoTypeReader2.readString(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeIso639(byteBuffer, language);
        byteBuffer.put(Utf82.convert(contentDistributorId));
        byteBuffer.put((byte) 0);

    }

    public String toString() {
        return "ContentDistributorIdBox[language=" + getLanguage() + ";contentDistributorId=" + getContentDistributorId() + "]";
    }
}
