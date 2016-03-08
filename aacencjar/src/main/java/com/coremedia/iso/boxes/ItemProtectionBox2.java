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
import com.googlecode.mp4parser.FullContainerBox2;

import java.nio.ByteBuffer;

/**
 * The Item Protection Box provides an array of item protection information, for use by the Item Information Box.
 *
 * @see ItemProtectionBox2
 */
public class ItemProtectionBox2 extends FullContainerBox2 {

    public static final String TYPE = "ipro";

    public ItemProtectionBox2() {
        super(TYPE);
    }

    public SchemeInformationBox2 getItemProtectionScheme() {
        if (!getBoxes(SchemeInformationBox2.class).isEmpty()) {
            return getBoxes(SchemeInformationBox2.class).get(0);
        } else {
            return null;
        }
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        IsoTypeReader2.readUInt16(content);
        parseChildBoxes(content);
    }


    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeUInt16(byteBuffer, getBox2s().size());
        writeChildBoxes(byteBuffer);
    }

}
