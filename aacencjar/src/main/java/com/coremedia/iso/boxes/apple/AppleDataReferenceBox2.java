/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoFile2;
import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

public class AppleDataReferenceBox2 extends AbstractFullBox2 {
    public static final String TYPE = "rdrf";
    private int dataReferenceSize;
    private String dataReferenceType;
    private String dataReference;

    public AppleDataReferenceBox2() {
        super(TYPE);
    }


    protected long getContentSize() {
        return 12 + dataReferenceSize;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        dataReferenceType = IsoTypeReader2.read4cc(content);
        dataReferenceSize = l2i(IsoTypeReader2.readUInt32(content));
        dataReference = IsoTypeReader2.readString(content, dataReferenceSize);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(IsoFile2.fourCCtoBytes(dataReferenceType));
        IsoTypeWriter2.writeUInt32(byteBuffer, dataReferenceSize);
        byteBuffer.put(Utf82.convert(dataReference));
    }

    public long getDataReferenceSize() {
        return dataReferenceSize;
    }

    public String getDataReferenceType() {
        return dataReferenceType;
    }

    public String getDataReference() {
        return dataReference;
    }
}
