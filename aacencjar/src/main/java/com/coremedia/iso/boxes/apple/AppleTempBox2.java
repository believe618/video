package com.coremedia.iso.boxes.apple;

/**
 * Beats per minute.
 */
public final class AppleTempBox2 extends AbstractAppleMetaDataBox2 {
    public static final String TYPE = "tmpo";


    public AppleTempBox2() {
        super(TYPE);
        appleDataBox = AppleDataBox2.getUint16AppleDataBox();
    }


    public int getTempo() {
        return appleDataBox.getData()[1];
    }

    public void setTempo(int tempo) {
        appleDataBox = new AppleDataBox2();
        appleDataBox.setVersion(0);
        appleDataBox.setFlags(21);
        appleDataBox.setFourBytes(new byte[4]);
        appleDataBox.setData(new byte[]{0, (byte) (tempo & 0xFF)});

    }
}