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

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser2;
import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.googlecode.mp4parser.AbstractBox2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;
import com.googlecode.mp4parser.util.ByteBufferByteChannel2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for all sample entries.
 *
 * @see AudioSampleEntry22
 * @see VisualSampleEntry2
 * @see TextSampleEntry2
 */
public abstract class SampleEntry2 extends AbstractBox2 implements ContainerBox2 {


    private int dataReferenceIndex;
    protected List<Box2> box2s = new LinkedList<Box2>();
    private BoxParser2 boxParser2;


    protected SampleEntry2(String type) {
        super(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDataReferenceIndex() {
        return dataReferenceIndex;
    }

    public void setDataReferenceIndex(int dataReferenceIndex) {
        this.dataReferenceIndex = dataReferenceIndex;
    }

    public void setBox2s(List<Box2> box2s) {
        this.box2s = new LinkedList<Box2>(box2s);
    }

    public void addBox(AbstractBox2 b) {
        box2s.add(b);
    }

    public boolean removeBox(Box2 b) {
        return box2s.remove(b);
    }

    public List<Box2> getBox2s() {
        return box2s;
    }

    @SuppressWarnings("unchecked")
    public <T extends Box2> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box2 boxe : box2s) { //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            if (clazz == boxe.getClass()) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof ContainerBox2) {
                boxesToBeReturned.addAll(((ContainerBox2) boxe).getBoxes(clazz, recursive));
            }
        }
        // Optimize here! Spare object creation work on arrays directly! System.arrayCopy
        return boxesToBeReturned;
        //return (T[]) boxesToBeReturned.toArray();
    }

    @SuppressWarnings("unchecked")
    public <T extends Box2> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @Override
    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser2 boxParser2) throws IOException {
        super.parse(readableByteChannel, header, contentSize, boxParser2);
        this.boxParser2 = boxParser2;
    }


    public void _parseReservedAndDataReferenceIndex(ByteBuffer content) {
        content.get(new byte[6]); // ignore 6 reserved bytes;
        dataReferenceIndex = IsoTypeReader2.readUInt16(content);
    }

    public void _parseChildBoxes(ByteBuffer content) {
        while (content.remaining() > 8) {
            try {
                box2s.add(boxParser2.parseBox(new ByteBufferByteChannel2(content), this));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        setDeadBytes(content.slice());
    }

    public void _writeReservedAndDataReferenceIndex(ByteBuffer bb) {
        bb.put(new byte[6]);
        IsoTypeWriter2.writeUInt16(bb, dataReferenceIndex);
    }

    public void _writeChildBoxes(ByteBuffer bb) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        try {
            for (Box2 box2 : box2s) {
                box2.getBox(wbc);
            }
            wbc.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot happen. Everything should be in memory and therefore no exceptions.");
        }
        bb.put(baos.toByteArray());
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (Box2 box2 : box2s) {
            sizeOfChildren += box2.getSize();
        }
        return getSize() - sizeOfChildren;
    }

}
