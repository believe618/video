package com.coremedia.iso.boxes.apple;

/**
 * Tv Episode.
 */
public class AppleTvEpisodeBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "tves";


    public AppleTvEpisodeBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint32AppleDataBox();
    }

}