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
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * A vodafone specific box.
 */
public class CoverUriBox2 extends AbstractFullBox2 {
    public static final String TYPE = "cvru";

    private String coverUri;

    public CoverUriBox2() {
        super(TYPE);
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(String coverUri) {
        this.coverUri = coverUri;
    }

    protected long getContentSize() {
        return Utf82.utf8StringLengthInBytes(coverUri) + 5;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        coverUri = IsoTypeReader2.readString(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf82.convert(coverUri));
        byteBuffer.put((byte) 0);
    }


    public String toString() {
        return "CoverUriBox[coverUri=" + getCoverUri() + "]";
    }
}
