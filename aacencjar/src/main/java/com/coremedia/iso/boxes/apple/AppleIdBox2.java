package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleIdBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "apID";


    public AppleIdBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}