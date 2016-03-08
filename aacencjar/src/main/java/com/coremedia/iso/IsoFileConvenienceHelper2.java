/*
 * Copyright 2012 Sebastian Annies, Hamburg
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
package com.coremedia.iso;

import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;
import com.googlecode.mp4parser.util.Path2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fine selection of useful methods.
 *
 * @author Andre John Mas
 * @author Sebastian Annies
 * @deprecated please use {@link Path2}. I will remove that class before 1.0.
 */
public class IsoFileConvenienceHelper2 {


    public static Box2 get(ContainerBox2 containerBox, String path) {

        String[] parts = path.split("/");
        if (parts.length == 0) {
            return null;
        }

        List<String> partList = new ArrayList<String>(Arrays.asList(parts));

        if ("".equals(partList.get(0))) {
            partList.remove(0);
        }

        if (partList.size() > 0) {
            return get((List<Box2>) containerBox.getBox2s(), partList);
        }
        return null;
    }

    private static Box2 get(List<Box2> box2s, List<String> path) {


        String typeInPath = path.remove(0);

        for (Box2 box2 : box2s) {
            if (box2 instanceof ContainerBox2) {
                ContainerBox2 boxContainer = (ContainerBox2) box2;
                String type = boxContainer.getType();

                if (typeInPath.equals(type)) {
                    List<Box2> children = boxContainer.getBox2s();
                    if (path.size() > 0) {
                        if (children.size() > 0) {
                            return get(children, path);
                        }
                    } else {
                        return box2;
                    }
                }

            } else {
                String type = box2.getType();

                if (path.size() == 0 && typeInPath.equals(type)) {
                    return box2;
                }

            }

        }

        return null;
    }
}

