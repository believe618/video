package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleTrackTitleBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9nam";


    public AppleTrackTitleBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}
