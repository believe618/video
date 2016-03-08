package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class ApplePurchaseDateBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "purd";


    public ApplePurchaseDateBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}