package com.coremedia.iso.boxes.apple;

import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Most stupid box of the world. Encapsulates actual data within
 */
public final class AppleDataBox2 extends AbstractFullBox2 {
    public static final String TYPE = "data";

    private byte[] fourBytes = new byte[4];
    private byte[] data;

    private static AppleDataBox2 getEmpty() {
        AppleDataBox2 appleDataBox = new AppleDataBox2();
        appleDataBox.setVersion(0);
        appleDataBox.setFourBytes(new byte[4]);
        return appleDataBox;
    }

    public static AppleDataBox2 getStringAppleDataBox() {
        AppleDataBox2 appleDataBox = getEmpty();
        appleDataBox.setFlags(1);
        appleDataBox.setData(new byte[]{0});
        return appleDataBox;
    }

    public static AppleDataBox2 getUint8AppleDataBox() {
        AppleDataBox2 appleDataBox = new AppleDataBox2();
        appleDataBox.setFlags(21);
        appleDataBox.setData(new byte[]{0});
        return appleDataBox;
    }

    public static AppleDataBox2 getUint16AppleDataBox() {
        AppleDataBox2 appleDataBox = new AppleDataBox2();
        appleDataBox.setFlags(21);
        appleDataBox.setData(new byte[]{0, 0});
        return appleDataBox;
    }

    public static AppleDataBox2 getUint32AppleDataBox() {
        AppleDataBox2 appleDataBox = new AppleDataBox2();
        appleDataBox.setFlags(21);
        appleDataBox.setData(new byte[]{0, 0, 0, 0});
        return appleDataBox;
    }

    public AppleDataBox2() {
        super(TYPE);
    }

    protected long getContentSize() {
        return data.length + 8;
    }

    public void setData(byte[] data) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public void setFourBytes(byte[] fourBytes) {
        System.arraycopy(fourBytes, 0, this.fourBytes, 0, 4);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        fourBytes = new byte[4];
        content.get(fourBytes);
        data = new byte[content.remaining()];
        content.get(data);
    }


    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(fourBytes, 0, 4);
        byteBuffer.put(data);
    }

    public byte[] getFourBytes() {
        return fourBytes;
    }

    public byte[] getData() {
        return data;
    }
}
