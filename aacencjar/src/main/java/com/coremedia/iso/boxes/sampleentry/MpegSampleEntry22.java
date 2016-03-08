package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MpegSampleEntry22 extends SampleEntry2 implements ContainerBox2 {

    private BoxParser2 boxParser;

    public MpegSampleEntry22(String type) {
        super(type);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        _parseChildBoxes(content);

    }

    @Override
    protected long getContentSize() {
        long contentSize = 8;
        for (Box2 boxe : box2s) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public String toString() {
        return "MpegSampleEntry" + Arrays.asList(getBox2s());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        _writeChildBoxes(byteBuffer);
    }
}
