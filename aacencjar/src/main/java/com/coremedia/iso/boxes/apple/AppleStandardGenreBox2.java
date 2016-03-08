package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleStandardGenreBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "gnre";


    public AppleStandardGenreBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint16AppleDataBox();
    }
}