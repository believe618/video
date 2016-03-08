package com.coremedia.iso.boxes.apple;

/**
 * iTunes Artist box.
 */
public final class AppleArtistBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9ART";


    public AppleArtistBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }


}
