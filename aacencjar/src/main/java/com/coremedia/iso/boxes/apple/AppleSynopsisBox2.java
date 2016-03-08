package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleSynopsisBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "ldes";


    public AppleSynopsisBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }


}