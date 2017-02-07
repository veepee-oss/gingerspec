/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.qa.utils;

public final class HashUtils {
    private static final int HASH = 7;
    private static final int MULTIPLIER = 31;

    private HashUtils() {
    }

    /**
     * doHash.
     *
     * @param str
     * @return String
     */
    public static String doHash(String str) {

        Integer hash = HASH;
        for (Integer i = 0; i < str.length(); i++) {
            hash = hash * MULTIPLIER + str.charAt(i);
        }

        return hash.toString();
    }
}