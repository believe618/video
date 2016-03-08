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

package com.coremedia.iso.boxes;


import com.googlecode.mp4parser.AbstractContainerBox2;

/**
 * The media declaration container contains all the objects that declare information about the media data within a
 * track.
 */
public class MediaBox2 extends AbstractContainerBox2 {
    public static final String TYPE = "mdia";

    public MediaBox2() {
        super(TYPE);
    }

    public MediaInformationBox2 getMediaInformationBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof MediaInformationBox2) {
                return (MediaInformationBox2) box2;
            }
        }
        return null;
    }

    public MediaHeaderBox2 getMediaHeaderBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof MediaHeaderBox2) {
                return (MediaHeaderBox2) box2;
            }
        }
        return null;
    }

    public HandlerBox2 getHandlerBox() {
        for (Box2 box2 : box2s) {
            if (box2 instanceof HandlerBox2) {
                return (HandlerBox2) box2;
            }
        }
        return null;
    }


}
