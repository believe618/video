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

import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;
import com.coremedia.iso.boxes.UserBox2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * This BoxParser handles the basic stuff like reading size and extracting box type.
 */
public abstract class AbstractBoxParser2 implements BoxParser2 {

    private static Logger LOG = Logger.getLogger(AbstractBoxParser2.class.getName());

    public abstract Box2 createBox(String type, byte[] userType, String parent);

    /**
     * Parses the next size and type, creates a box instance and parses the box's content.
     *
     * @param byteChannel the FileChannel pointing to the ISO file
     * @param parent      the current box's parent (null if no parent)
     * @return the box just parsed
     * @throws java.io.IOException if reading from <code>in</code> fails
     */
    public Box2 parseBox(ReadableByteChannel byteChannel, ContainerBox2 parent) throws IOException {


        ByteBuffer header = ChannelHelper2.readFully(byteChannel, 8);

        long size = IsoTypeReader2.readUInt32(header);
        // do plausibility check
        if (size < 8 && size > 1) {
            LOG.severe("Plausibility check failed: size < 8 (size = " + size + "). Stop parsing!");
            return null;
        }


        String type = IsoTypeReader2.read4cc(header);
        byte[] usertype = null;
        long contentSize;

        if (size == 1) {
            ByteBuffer bb = ByteBuffer.allocate(8);
            byteChannel.read(bb);
            bb.rewind();
            size = IsoTypeReader2.readUInt64(bb);
            contentSize = size - 16;
        } else if (size == 0) {
            if (byteChannel instanceof FileChannel) {
                size = ((FileChannel) byteChannel).size() - ((FileChannel) byteChannel).position() - 8;
            } else {
                throw new RuntimeException("Only FileChannel inputs may use size == 0 (box reaches to the end of file)");
            }
            contentSize = size - 8;
        } else {
            contentSize = size - 8;
        }
        if (UserBox2.TYPE.equals(type)) {
            ByteBuffer bb = ByteBuffer.allocate(16);
            byteChannel.read(bb);
            bb.rewind();
            usertype = bb.array();
            contentSize -= 16;
        }
        Box2 box2 = createBox(type, usertype, parent.getType());
        box2.setParent(parent);
        LOG.finest("Parsing " + box2.getType());
        // System.out.println("parsing " + Arrays.toString(box.getType()) + " " + box.getClass().getName() + " size=" + size);


        if (l2i(size - contentSize) == 8) {
            // default - no large box - no uuid
            // do nothing header's already correct
            header.rewind();
        } else if (l2i(size - contentSize) == 16) {
            header = ByteBuffer.allocate(16);
            IsoTypeWriter2.writeUInt32(header, 1);
            header.put(IsoFile2.fourCCtoBytes(type));
            IsoTypeWriter2.writeUInt64(header, size);
        } else if (l2i(size - contentSize) == 24) {
            header = ByteBuffer.allocate(24);
            IsoTypeWriter2.writeUInt32(header, size);
            header.put(IsoFile2.fourCCtoBytes(type));
            header.put(usertype);
        } else if (l2i(size - contentSize) == 32) {
            header = ByteBuffer.allocate(32);
            IsoTypeWriter2.writeUInt32(header, size);
            header.put(IsoFile2.fourCCtoBytes(type));
            IsoTypeWriter2.writeUInt64(header, size);
            header.put(usertype);
        } else {
            throw new RuntimeException("I didn't expect that");
        }


        box2.parse(byteChannel, header, contentSize, this);
        // System.out.println("box = " + box);


        assert size == box2.getSize() :
                "Reconstructed Size is not x to the number of parsed bytes! (" +
                        box2.getType() + ")"
                        + " Actual Box size: " + size + " Calculated size: " + box2.getSize();
        return box2;
    }


}
