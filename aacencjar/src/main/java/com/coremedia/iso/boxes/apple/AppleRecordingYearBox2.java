package com.coremedia.iso.boxes.apple;

/**
 *
 */
public class AppleRecordingYearBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "\u00a9day";


    public AppleRecordingYearBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getStringAppleDataBox();
    }


}