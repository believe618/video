package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleDescriptionBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "desc";


    public AppleDescriptionBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}