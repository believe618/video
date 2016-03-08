package com.coremedia.iso.boxes.apple;

import com.googlecode.mp4parser.AbstractContainerBox2;

/**
 * undocumented iTunes MetaData Box.
 */
public class AppleItemListBox2 extends AbstractContainerBox2 {
    public static final String TYPE = "ilst";

    public AppleItemListBox2() {
        super(TYPE);
    }

}
