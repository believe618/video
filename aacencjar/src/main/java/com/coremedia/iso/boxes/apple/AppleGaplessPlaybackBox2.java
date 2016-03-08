package com.coremedia.iso.boxes.apple;

/**
 * Gapless Playback.
 */
public final class AppleGaplessPlaybackBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "pgap";


    public AppleGaplessPlaybackBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint8AppleDataBox();
    }

}
