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

package com.coremedia.iso;

import com.googlecode.mp4parser.AbstractContainerBox2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.MovieBox2;
import com.googlecode.mp4parser.annotations.DoNotParseDetail2;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * The most upper container for ISO Boxes. It is a container box that is a file.
 * Uses IsoBufferWrapper  to access the underlying file.
 */
@DoNotParseDetail2
public class IsoFile2 extends AbstractContainerBox2 {
    protected BoxParser2 boxParser = new PropertyBoxParser2Impl2();
    ReadableByteChannel byteChannel;

    public IsoFile2() {
        super("");
    }

    public IsoFile2(ReadableByteChannel byteChannel) throws IOException {
        super("");
        this.byteChannel = byteChannel;
        boxParser = createBoxParser();
        parse();
    }

    public IsoFile2(ReadableByteChannel byteChannel, BoxParser2 boxParser) throws IOException {
        super("");
        this.byteChannel = byteChannel;
        this.boxParser = boxParser;
        parse();


    }

    protected BoxParser2 createBoxParser() {
        return new PropertyBoxParser2Impl2();
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        // there are no details to parse we should be just file
    }

    public void parse(ReadableByteChannel inFC, ByteBuffer header, long contentSize, AbstractBoxParser2 abstractBoxParser) throws IOException {
        throw new IOException("This method is not meant to be called. Use #parse() directly.");
    }

    private void parse() throws IOException {

        boolean done = false;
        while (!done) {
            try {
                Box2 box2 = boxParser.parseBox(byteChannel, this);
                if (box2 != null) {
                    //  System.err.println(box.getType());
                    box2s.add(box2);
                } else {
                    done = true;
                }
            } catch (EOFException e) {
                done = true;
            }
        }
    }

    @DoNotParseDetail2
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("IsoFile[");
        if (box2s == null) {
            buffer.append("unparsed");
        } else {
            for (int i = 0; i < box2s.size(); i++) {
                if (i > 0) {
                    buffer.append(";");
                }
                buffer.append(box2s.get(i).toString());
            }
        }
        buffer.append("]");
        return buffer.toString();
    }

    @DoNotParseDetail2
    public static byte[] fourCCtoBytes(String fourCC) {
        byte[] result = new byte[4];
        if (fourCC != null) {
            for (int i = 0; i < Math.min(4, fourCC.length()); i++) {
                result[i] = (byte) fourCC.charAt(i);
            }
        }
        return result;
    }

    @DoNotParseDetail2
    public static String bytesToFourCC(byte[] type) {
        byte[] result = new byte[]{0, 0, 0, 0};
        if (type != null) {
            System.arraycopy(type, 0, result, 0, Math.min(type.length, 4));
        }
        try {
            return new String(result, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new Error("Required character encoding is missing", e);
        }
    }


    @Override
    public long getNumOfBytesToFirstChild() {
        return 0;
    }

    @Override
    public long getSize() {
        long size = 0;
        for (Box2 box2 : box2s) {
            size += box2.getSize();
        }
        return size;
    }

    @Override
    public IsoFile2 getIsoFile() {
        return this;
    }


    /**
     * Shortcut to get the MovieBox since it is often needed and present in
     * nearly all ISO 14496 files (at least if they are derived from MP4 ).
     *
     * @return the MovieBox or <code>null</code>
     */
    @DoNotParseDetail2
    public MovieBox2 getMovieBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof MovieBox2) {
                return (MovieBox2) box2;
            }
        }
        return null;
    }

    public void getBox(WritableByteChannel os) throws IOException {
        for (Box2 box2 : box2s) {

            if (os instanceof FileChannel) {
                long startPos = ((FileChannel) os).position();
                box2.getBox(os);
                long size = ((FileChannel) os).position() - startPos;
                assert size == box2.getSize();
            } else {
                box2.getBox(os);
            }

        }
    }
}
