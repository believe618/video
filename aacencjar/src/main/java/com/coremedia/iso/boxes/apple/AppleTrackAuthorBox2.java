package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleTrackAuthorBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9wrt";


    public AppleTrackAuthorBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }


}