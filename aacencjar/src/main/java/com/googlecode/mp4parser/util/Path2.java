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
package com.googlecode.mp4parser.util;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coremedia.iso.IsoFile2;
import com.coremedia.iso.boxes.Box2;
import com.coremedia.iso.boxes.ContainerBox2;

public class Path2 {

    IsoFile2 isoFile2;

    public Path2(IsoFile2 isoFile2) {
        this.isoFile2 = isoFile2;
    }

    private static Pattern component = Pattern.compile("(....)(\\[(.*)\\])?");

    public String createPath(Box2 box2) {
        return createPath(box2, "");
    }

    private String createPath(Box2 box2, String path) {
        if (box2 instanceof IsoFile2) {
            assert box2 == isoFile2;
            return path;
        } else {
            List<?> boxesOfBoxType = box2.getParent().getBoxes(box2.getClass());
            int index = boxesOfBoxType.indexOf(box2);
            path = String.format("/%s[%d]", box2.getType(), index) + path;

            return createPath(box2.getParent(), path);
        }
    }

    public Box2 getPath(String path) {
        List<Box2> all = getPath(isoFile2, path);
        return all.isEmpty() ?null:all.get(0);
    }

    public List<Box2> getPaths(String path) {
        return getPath(isoFile2, path);
    }

    public boolean isContained(Box2 box2, String path) {
        return getPath(isoFile2, path).contains(box2);
    }

    private List<Box2> getPath(Box2 box2, String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.length() == 0) {
            return Collections.singletonList(box2);
        } else {
            String later;
            String now;
            if (path.contains("/")) {
                later = path.substring(path.indexOf('/'));
                now = path.substring(0, path.indexOf('/'));
            } else {
                now = path;
                later = "";
            }

            Matcher m = component.matcher(now);
            if (m.matches()) {
                String type = m.group(1);
                int index = -1;
                if (m.group(2) != null) {
                    // we have a specific index
                    String indexString = m.group(3);
                    index = Integer.parseInt(indexString);
                }
                List<Box2> children = new LinkedList<Box2>();
                int currentIndex = 0;
                for (Box2 box21 : ((ContainerBox2) box2).getBox2s()) {
                    if (box21.getType().equals(type)) {
                        if (index == -1 || index == currentIndex) {
                            children.addAll(getPath(box21, later));
                        }
                        currentIndex++;
                    }
                }
                return children;

            } else {
                throw new RuntimeException("invalid path.");
            }
        }

    }
}
