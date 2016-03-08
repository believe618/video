/*
 * Copyright 2012 Sebastian Annies, Hamburg
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
package com.coremedia.iso;

import java.nio.ByteBuffer;

public final class IsoTypeReaderVariable2 {

    public static long read(ByteBuffer bb, int bytes) {
        switch (bytes) {
            case 1:
                return IsoTypeReader2.readUInt8(bb);
            case 2:
                return IsoTypeReader2.readUInt16(bb);
            case 3:
                return IsoTypeReader2.readUInt24(bb);
            case 4:
                return IsoTypeReader2.readUInt32(bb);
            case 8:
                return IsoTypeReader2.readUInt64(bb);
            default:
                throw new RuntimeException("I don't know how to read " + bytes + " bytes");
        }

    }
}
