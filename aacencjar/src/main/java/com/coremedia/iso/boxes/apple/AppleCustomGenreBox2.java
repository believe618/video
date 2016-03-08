package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf82;

/**
 *
 */
public final class AppleCustomGenreBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9gen";


    public AppleCustomGenreBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }

    public void setGenre(String genre) {
        appleDataBox = new AppleDataBox2();
        appleDataBox.setVersion(0);
        appleDataBox.setFlags(1);
        appleDataBox.setFourBytes(new byte[4]);
        appleDataBox.setData(Utf82.convert(genre));
    }

    public String getGenre() {
        return Utf82.convert(appleDataBox.getData());
    }
}