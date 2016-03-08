package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleAlbumBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9alb";


    public AppleAlbumBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}