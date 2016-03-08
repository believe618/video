package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleNetworkBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "tvnn";


    public AppleNetworkBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }


}