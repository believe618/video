package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Apple Meaning box. Allowed as subbox of "----" box.
 *
 * @see AppleGenericBox2
 */
public final class AppleMeanBox2 extends AbstractFullBox2 {
    public static final String TYPE = "mean";
    private String meaning;

    public AppleMeanBox2() {
        super(TYPE);
    }

    protected long getContentSize() {
        return 4 + Utf82.utf8StringLengthInBytes(meaning);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        meaning = IsoTypeReader2.readString(content, content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf82.convert(meaning));
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }


}
