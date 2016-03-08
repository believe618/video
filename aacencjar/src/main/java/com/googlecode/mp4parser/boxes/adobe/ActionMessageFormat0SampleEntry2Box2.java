package com.googlecode.mp4parser.boxes.adobe;

import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.sampleentry.SampleEntry2;

import java.nio.ByteBuffer;

/**
 * Sample Entry as used for Action Message Format tracks.
 */
public class ActionMessageFormat0SampleEntry2Box2 extends SampleEntry2 {
    public ActionMessageFormat0SampleEntry2Box2() {
        super("amf0");
    }

    @Override
    protected long getContentSize() {
        long size = 8;
        for (Box2 box2 : box2s) {
            size += box2.getSize();
        }

        return size;
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        _parseChildBoxes(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        _writeChildBoxes(byteBuffer);
    }
}
