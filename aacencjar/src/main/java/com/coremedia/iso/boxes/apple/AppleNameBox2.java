package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Apple Name box. Allowed as subbox of "----" box.
 *
 * @see AppleGenericBox2
 */
public final class AppleNameBox2 extends AbstractFullBox2 {
    public static final String TYPE = "name";
    private String name;

    public AppleNameBox2() {
        super(TYPE);
    }

    protected long getContentSize() {
        return 4 + Utf82.convert(name).length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        name = IsoTypeReader2.readString(content, content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf82.convert(name));
    }
}
