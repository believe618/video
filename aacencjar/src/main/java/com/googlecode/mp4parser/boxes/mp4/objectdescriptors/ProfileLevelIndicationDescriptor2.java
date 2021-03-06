/*
 * Copyright 2011 castLabs, Berlin
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

package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import com.coremedia.iso.IsoTypeReader2;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * class ProfileLevelIndicationIndexDescriptor () extends BaseDescriptor
 * : bit(8) ProfileLevelIndicationIndexDescrTag {
 * bit(8) profileLevelIndicationIndex;
 * }
 */
@Descriptor2(tags = 0x14)
public class ProfileLevelIndicationDescriptor2 extends BaseDescriptor2 {
    int profileLevelIndicationIndex;

    @Override
    public void parseDetail( ByteBuffer bb) throws IOException {
        profileLevelIndicationIndex = IsoTypeReader2.readUInt8(bb);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ProfileLevelIndicationDescriptor");
        sb.append("{profileLevelIndicationIndex=").append(Integer.toHexString(profileLevelIndicationIndex));
        sb.append('}');
        return sb.toString();
    }
}
