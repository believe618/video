package com.coremedia.iso.boxes.apple;

/**
 * Compilation.
 */
public final class AppleCompilationBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "cpil";


    public AppleCompilationBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint8AppleDataBox();
    }

}