package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.AbstractBox2;

import java.nio.ByteBuffer;

/**
 *
 */
public class ItemDataBox2 extends AbstractBox2 {
    ByteBuffer data = ByteBuffer.allocate(0);
    public static final String TYPE = "idat";


    public ItemDataBox2() {
        super(TYPE);
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    @Override
    protected long getContentSize() {
        return data.limit();
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        data = content.slice();
        content.position(content.position() + content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(data);
    }
}
