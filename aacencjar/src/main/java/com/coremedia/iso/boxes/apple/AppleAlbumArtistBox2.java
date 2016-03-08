package com.coremedia.iso.boxes.apple;

/**
 * itunes MetaData comment box.
 */
public class AppleAlbumArtistBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "aART";


    public AppleAlbumArtistBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }


}