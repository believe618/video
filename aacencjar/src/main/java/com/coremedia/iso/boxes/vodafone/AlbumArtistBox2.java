/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.boxes.vodafone;


import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.Utf82;
import com.coremedia.iso.boxes.UserDataBox2;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Special box used by Vodafone in their DCF containing information about the artist. Mainly used for OMA DCF files
 * containing music. Resides in the {@link UserDataBox2}.
 */
public class AlbumArtistBox2 extends AbstractFullBox2 {
    public static final String TYPE = "albr";

    private String language;
    private String albumArtist;

    public AlbumArtistBox2() {
        super(TYPE);
    }

    public String getLanguage() {
        return language;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    protected long getContentSize() {
        return 6 + Utf82.utf8StringLengthInBytes(albumArtist) + 1;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader2.readIso639(content);
        albumArtist = IsoTypeReader2.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeIso639(byteBuffer, language);
        byteBuffer.put(Utf82.convert(albumArtist));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "AlbumArtistBox[language=" + getLanguage() + ";albumArtist=" + getAlbumArtist() + "]";
    }
}
