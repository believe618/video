package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractBox2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;
import com.googlecode.mp4parser.util.ByteBufferByteChannel2;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public abstract class AbstractAppleMetaDataBox2 extends AbstractBox2 implements ContainerBox2 {
    private static Logger LOG = Logger.getLogger(AbstractAppleMetaDataBox2.class.getName());
    AppleDataBox2 appleDataBox = new AppleDataBox2();

    public List<Box2> getBox2s() {
        return Collections.singletonList((Box2) appleDataBox);
    }

    public void setBox2s(List<Box2> box2s) {
        if (box2s.size() == 1 && box2s.get(0) instanceof AppleDataBox2) {
            appleDataBox = (AppleDataBox2) box2s.get(0);
        } else {
            throw new IllegalArgumentException("This box only accepts one AppleDataBox child");
        }
    }

    public <T extends Box2> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    public <T extends Box2> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        //todo recursive?
        if (clazz.isAssignableFrom(appleDataBox.getClass())) {
            return (List<T>) Collections.singletonList(appleDataBox);
        }
        return null;
    }

    public AbstractAppleMetaDataBox2(String type) {
        super(type);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        long dataBoxSize = IsoTypeReader2.readUInt32(content);
        String thisShouldBeData = IsoTypeReader2.read4cc(content);
        assert "data".equals(thisShouldBeData);
        appleDataBox = new AppleDataBox2();
        try {
            appleDataBox.parse(new ByteBufferByteChannel2(content), null, content.remaining(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        appleDataBox.setParent(this);
    }


    protected long getContentSize() {
        return appleDataBox.getSize();
    }

    protected void getContent(ByteBuffer byteBuffer) {
        try {
            appleDataBox.getBox(new ByteBufferByteChannel2(byteBuffer));
        } catch (IOException e) {
            throw new RuntimeException("The Channel is based on a ByteBuffer and therefore it shouldn't throw any exception");
        }
    }

    public long getNumOfBytesToFirstChild() {
        return getSize() - appleDataBox.getSize();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "appleDataBox=" + getValue() +
                '}';
    }

    static long toLong(byte b) {
        return b < 0 ? b + 256 : b;
    }

    public void setValue(String value) {
        if (appleDataBox.getFlags() == 1) {
            appleDataBox = new AppleDataBox2();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(1);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setData(Utf82.convert(value));
        } else if (appleDataBox.getFlags() == 21) {
            byte[] content = appleDataBox.getData();
            appleDataBox = new AppleDataBox2();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(21);
            appleDataBox.setFourBytes(new byte[4]);

            ByteBuffer bb = ByteBuffer.allocate(content.length);
            if (content.length == 1) {
                IsoTypeWriter2.writeUInt8(bb, (Byte.parseByte(value) & 0xFF));
            } else if (content.length == 2) {
                IsoTypeWriter2.writeUInt16(bb, Integer.parseInt(value));
            } else if (content.length == 4) {
                IsoTypeWriter2.writeUInt32(bb, Long.parseLong(value));
            } else if (content.length == 8) {
                IsoTypeWriter2.writeUInt64(bb, Long.parseLong(value));
            } else {
                throw new Error("The content length within the appleDataBox is neither 1, 2, 4 or 8. I can't handle that!");
            }
            appleDataBox.setData(bb.array());
        } else if (appleDataBox.getFlags() == 0) {
            appleDataBox = new AppleDataBox2();
            appleDataBox.setVersion(0);
            appleDataBox.setFlags(0);
            appleDataBox.setFourBytes(new byte[4]);
            appleDataBox.setData(hexStringToByteArray(value));

        } else {
            LOG.warning("Don't know how to handle appleDataBox with flag=" + appleDataBox.getFlags());
        }
    }

    public String getValue() {
        if (appleDataBox.getFlags() == 1) {
            return Utf82.convert(appleDataBox.getData());
        } else if (appleDataBox.getFlags() == 21) {
            byte[] content = appleDataBox.getData();
            long l = 0;
            int current = 1;
            int length = content.length;
            for (byte b : content) {
                l += toLong(b) << (8 * (length - current++));
            }
            return "" + l;
        } else if (appleDataBox.getFlags() == 0) {
            return String.format("%x", new BigInteger(appleDataBox.getData()));
        } else {
            return "unknown";
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


}
