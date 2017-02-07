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
package com.stratio.qa.cucumber.converter;

import cucumber.api.Transformer;

import java.util.ArrayList;
import java.util.List;


public class ArrayListConverter extends Transformer<List<String>> {

    @Override
    public List<String> transform(String input) {

        List<String> response = new ArrayList<String>();
        String[] aInput = input.split(",");
        for (String content : aInput) {
            if (content.trim().equals("")) {
                response.add(content);
            } else {
                response.add(content.trim());
            }
        }

        return response;
    }
}
