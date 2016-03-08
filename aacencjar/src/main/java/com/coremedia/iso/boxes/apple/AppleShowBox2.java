package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleShowBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "tvsh";


    public AppleShowBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}