package com.coremedia.iso.boxes.apple;

/**
 * itunes MetaData comment box.
 */
public final class AppleCopyrightBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "cprt";


    public AppleCopyrightBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}