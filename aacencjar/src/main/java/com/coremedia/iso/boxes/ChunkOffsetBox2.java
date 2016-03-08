package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.AbstractFullBox2;

/**
 * Abstract Chunk Offset Box
 */
public abstract class ChunkOffsetBox2 extends AbstractFullBox2 {

    public ChunkOffsetBox2(String type) {
        super(type);
    }

    public abstract long[] getChunkOffsets();


    public String toString() {
        return this.getClass().getSimpleName() + "[entryCount=" + getChunkOffsets().length + "]";
    }

}
