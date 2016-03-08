package com.coremedia.iso.boxes.apple;

/**
 * Tv Season.
 */
public final class AppleTvSeasonBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "tvsn";


    public AppleTvSeasonBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint32AppleDataBox();
    }

}