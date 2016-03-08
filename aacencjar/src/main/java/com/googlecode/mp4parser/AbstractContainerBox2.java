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

package com.googlecode.mp4parser;

import com.coremedia.iso.BoxParser2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;
import com.googlecode.mp4parser.util.ByteBufferByteChannel2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Abstract base class suitable for most boxes acting purely as container for other boxes.
 */
public abstract class AbstractContainerBox2 extends AbstractBox2 implements ContainerBox2 {
    private static Logger LOG = Logger.getLogger(AbstractContainerBox2.class.getName());

    protected List<Box2> box2s = new LinkedList<Box2>();
    protected BoxParser2 boxParser;

    @Override
    protected long getContentSize() {
        long contentSize = 0;
        for (Box2 boxe : box2s) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public AbstractContainerBox2(String type) {
        super(type);
    }

    public List<Box2> getBox2s() {
        return Collections.unmodifiableList(box2s);
    }

    public void setBox2s(List<Box2> box2s) {
        this.box2s = new LinkedList<Box2>(box2s);
    }

    @SuppressWarnings("unchecked")
    public <T extends Box2> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Box2> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        List<T> boxesToBeReturned = new ArrayList<T>(2);
        for (Box2 boxe : box2s) {
            //clazz.isInstance(boxe) / clazz == boxe.getClass()?
            // I hereby finally decide to use isInstance

            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add((T) boxe);
            }

            if (recursive && boxe instanceof ContainerBox2) {
                boxesToBeReturned.addAll(((ContainerBox2) boxe).getBoxes(clazz, recursive));
            }
        }
        return boxesToBeReturned;
    }

    /**
     * Add <code>b</code> to the container and sets the parent correctly.
     *
     * @param b will be added to the container
     */
    public void addBox(Box2 b) {
        b.setParent(this);
        box2s.add(b);
    }

    public void removeBox(Box2 b) {
        box2s.remove(b);
    }

    @Override
    public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser2 boxParser2) throws IOException {
        super.parse(readableByteChannel, header, contentSize, boxParser2);
        this.boxParser = boxParser2;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseChildBoxes(content);
    }


    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(this.getClass().getSimpleName()).append("[");
        for (int i = 0; i < box2s.size(); i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(box2s.get(i).toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * The number of bytes from box start (first length byte) to the
     * first length byte of the first child box
     *
     * @return offset to first child box
     */
    public long getNumOfBytesToFirstChild() {
        return 8;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeChildBoxes(byteBuffer);
    }

    protected final void parseChildBoxes(ByteBuffer content) {
        try {
            while (content.remaining() >= 8) { //  8 is the minimal size for a sane box
                box2s.add(boxParser.parseBox(new ByteBufferByteChannel2(content), this));
            }

            if (content.remaining() != 0) {
                setDeadBytes(content.slice());
                LOG.warning("Something's wrong with the sizes. There are dead bytes in a container box.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected final void writeChildBoxes(ByteBuffer bb) {
        WritableByteChannel wbc = new ByteBufferByteChannel2(bb);
        for (Box2 box2 : box2s) {
            try {
                box2.getBox(wbc);
            } catch (IOException e) {
                // My WritableByteChannel won't throw any excpetion
                throw new RuntimeException("Cannot happen to me", e);
            }
        }
    }

}
