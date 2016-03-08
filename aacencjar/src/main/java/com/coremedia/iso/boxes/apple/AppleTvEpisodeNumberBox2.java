package com.coremedia.iso.boxes.apple;

/**
 * Tv Episode.
 */
public class AppleTvEpisodeNumberBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "tven";


    public AppleTvEpisodeNumberBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

}