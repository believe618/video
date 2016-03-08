/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.fragment;

import com.googlecode.mp4parser.AbstractContainerBox2;

/**
 * aligned(8) class MovieExtendsBox extends Box('mvex'){
 * }
 */
public class MovieExtendsBox2 extends AbstractContainerBox2 {
    public static final String TYPE = "mvex";

    public MovieExtendsBox2() {
        super(TYPE);
    }
}