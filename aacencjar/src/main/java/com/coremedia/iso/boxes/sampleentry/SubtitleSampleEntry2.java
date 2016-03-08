package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: magnus
 * Date: 2012-03-08
 * Time: 11:36
 * To change this template use File | Settings | File Templates.
 */
public class SubtitleSampleEntry2 extends SampleEntry2 {

    public static final String TYPE1 = "stpp";

    public static final String TYPE_ENCRYPTED = ""; // This is not known!

    private String namespace;
    private String schemaLocation;
    private String imageMimeType;

    public SubtitleSampleEntry2(String type) {
        super(type);
    }

    @Override
    protected long getContentSize() {
        long contentSize = 8 + namespace.length() + schemaLocation.length() + imageMimeType.length() + 3;
        return contentSize;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        _parseReservedAndDataReferenceIndex(content);
        namespace = IsoTypeReader2.readString(content);
        schemaLocation = IsoTypeReader2.readString(content);
        imageMimeType = IsoTypeReader2.readString(content);
        _parseChildBoxes(content);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        _writeReservedAndDataReferenceIndex(byteBuffer);
        IsoTypeWriter2.writeUtf8String(byteBuffer, namespace);
        IsoTypeWriter2.writeUtf8String(byteBuffer, schemaLocation);
        IsoTypeWriter2.writeUtf8String(byteBuffer, imageMimeType);
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getImageMimeType() {
        return imageMimeType;
    }

    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }
}

