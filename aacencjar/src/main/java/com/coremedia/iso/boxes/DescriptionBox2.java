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
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Gives a language dependent description of the media contained in the ISO file.
 */
public class DescriptionBox2 extends AbstractFullBox2 {
    public static final String TYPE = "dscp";

    private String language;
    private String description;

    public DescriptionBox2() {
        super(TYPE);
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }

    protected long getContentSize() {
        return 7 + Utf82.utf8StringLengthInBytes(description);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader2.readIso639(content);
        description = IsoTypeReader2.readString(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeIso639(byteBuffer, language);
        byteBuffer.put(Utf82.convert(description));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "DescriptionBox[language=" + getLanguage() + ";description=" + getDescription() + "]";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}