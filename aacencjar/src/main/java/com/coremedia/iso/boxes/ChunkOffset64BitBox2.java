package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;

import java.nio.ByteBuffer;

import static com.googlecode.mp4parser.util.CastUtils2.l2i;

/**
 * Abstract Chunk Offset Box
 */
public class ChunkOffset64BitBox2 extends ChunkOffsetBox2 {
    public static final String TYPE = "co64";
    private long[] chunkOffsets;

    public ChunkOffset64BitBox2() {
        super(TYPE);
    }

    @Override
    public long[] getChunkOffsets() {
        return chunkOffsets;
    }

    @Override
    protected long getContentSize() {
        return 8 + 8 * chunkOffsets.length;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int entryCount = l2i(IsoTypeReader2.readUInt32(content));
        chunkOffsets = new long[entryCount];
        for (int i = 0; i < entryCount; i++) {
            chunkOffsets[i] = IsoTypeReader2.readUInt64(content);
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeUInt32(byteBuffer, chunkOffsets.length);
        for (long chunkOffset : chunkOffsets) {
            IsoTypeWriter2.writeUInt64(byteBuffer, chunkOffset);
        }
    }


}
