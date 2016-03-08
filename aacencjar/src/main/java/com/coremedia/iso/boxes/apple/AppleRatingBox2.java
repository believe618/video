package com.coremedia.iso.boxes.apple;

/**
 * iTunes Rating Box.
 */
public final class AppleRatingBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "rtng";


    public AppleRatingBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint8AppleDataBox();
    }


}
