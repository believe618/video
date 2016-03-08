package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.sampleentry.SampleEntry2;

import java.nio.ByteBuffer;

public class TimeCodeBox2 extends SampleEntry2 {
    byte[] data;


    public TimeCodeBox2() {
        super("tmcd");
    }

    @Override
    protected long getContentSize() {
        long size = 26;
        for (Box2 box2 : box2s) {
            size += box2.getSize();
        }
        return size;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        data = new byte[18];
        content.get(data);
        _parseChildBoxes(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        byteBuffer.put(data);
        _writeChildBoxes(byteBuffer);
    }
}
