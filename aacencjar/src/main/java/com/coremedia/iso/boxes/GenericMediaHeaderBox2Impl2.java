package com.coremedia.iso.boxes;

import java.nio.ByteBuffer;

public class GenericMediaHeaderBox2Impl2 extends AbstractMediaHeaderBox2 {

    ByteBuffer data;

    @Override
    protected long getContentSize() {
        return 4 + data.limit();
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.data = content.slice();
        content.position(content.remaining() + content.position());

    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put((ByteBuffer) data.rewind());
    }

    public GenericMediaHeaderBox2Impl2() {
        super("gmhd");
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
