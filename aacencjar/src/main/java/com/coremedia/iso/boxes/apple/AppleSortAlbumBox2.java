package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleSortAlbumBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "soal";


    public AppleSortAlbumBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }
}