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
 * List of keywords according to 3GPP 26.244.
 */
public class KeywordsBox2 extends AbstractFullBox2 {
    public static final String TYPE = "kywd";

    private String language;
    private String[] keywords;

    public KeywordsBox2() {
        super(TYPE);
    }

    public String getLanguage() {
        return language;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    protected long getContentSize() {
        long contentSize = 7;
        for (String keyword : keywords) {
            contentSize += 1 + Utf82.utf8StringLengthInBytes(keyword) + 1;
        }
        return contentSize;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader2.readIso639(content);
        int keywordCount = IsoTypeReader2.readUInt8(content);
        keywords = new String[keywordCount];
        for (int i = 0; i < keywordCount; i++) {
            IsoTypeReader2.readUInt8(content);
            keywords[i] = IsoTypeReader2.readString(content);
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeIso639(byteBuffer, language);
        IsoTypeWriter2.writeUInt8(byteBuffer, keywords.length);
        for (String keyword : keywords) {
            IsoTypeWriter2.writeUInt8(byteBuffer, Utf82.utf8StringLengthInBytes(keyword) + 1);
            byteBuffer.put(Utf82.convert(keyword));
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("KeywordsBox[language=").append(getLanguage());
        for (int i = 0; i < keywords.length; i++) {
            buffer.append(";keyword").append(i).append("=").append(keywords[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
}