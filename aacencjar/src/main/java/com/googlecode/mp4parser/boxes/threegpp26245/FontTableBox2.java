package com.googlecode.mp4parser.boxes.threegpp26245;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractBox2;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class FontTableBox2 extends AbstractBox2 {
    List<FontRecord> entries = new LinkedList<FontRecord>();

    public FontTableBox2() {
        super("ftab");
    }

    @Override
    protected long getContentSize() {
        int size = 2;
        for (FontRecord fontRecord : entries) {
            size += fontRecord.getSize();
        }
        return size;
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        int numberOfRecords = IsoTypeReader2.readUInt16(content);
        for (int i = 0; i < numberOfRecords; i++) {
            FontRecord fr = new FontRecord();
            fr.parse(content);
            entries.add(fr);
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        IsoTypeWriter2.writeUInt16(byteBuffer, entries.size());
        for (FontRecord record : entries) {
            record.getContent(byteBuffer);
        }
    }

    public List<FontRecord> getEntries() {
        return entries;
    }

    public void setEntries(List<FontRecord> entries) {
        this.entries = entries;
    }

    public static class FontRecord {
        int fontId;
        String fontname;

        public FontRecord() {
        }

        public FontRecord(int fontId, String fontname) {
            this.fontId = fontId;
            this.fontname = fontname;
        }

        public void parse(ByteBuffer bb) {
            fontId = IsoTypeReader2.readUInt16(bb);
            int length = IsoTypeReader2.readUInt8(bb);
            fontname = IsoTypeReader2.readString(bb, length);
        }

        public void getContent(ByteBuffer bb) {
            IsoTypeWriter2.writeUInt16(bb, fontId);
            IsoTypeWriter2.writeUInt8(bb, fontname.length());
            bb.put(Utf82.convert(fontname));
        }

        public int getSize() {
            return Utf82.utf8StringLengthInBytes(fontname) + 3;
        }

        @Override
        public String toString() {
            return "FontRecord{" +
                    "fontId=" + fontId +
                    ", fontname='" + fontname + '\'' +
                    '}';
        }
    }
}
