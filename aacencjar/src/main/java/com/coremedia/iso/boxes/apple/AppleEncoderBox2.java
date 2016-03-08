package com.coremedia.iso.boxes.apple;

/**
 * itunes MetaData comment box.
 */
public final class AppleEncoderBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9too";


    public AppleEncoderBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}