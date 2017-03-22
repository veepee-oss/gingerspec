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
package com.stratio.qa.aspects;

import cucumber.runtime.io.Resource;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Aspect
public class LoopTagAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());


    @Pointcut("execution (private * cucumber.runtime.FeatureBuilder.read(..)) &&" + "args (resource)")
    protected void addLoopTagPointcutScenario(Resource resource) {
    }

    /**
     * @param resource
     * @throws Throwable
     */
    @Around(value = "addLoopTagPointcutScenario(resource)")
    public String aroundAddLoopTagPointcutScenario(Resource resource) throws Throwable {
        List<String> lines = Files.readAllLines(Paths.get(resource.getPath()), StandardCharsets.UTF_8);
        String listParams;
        String paramReplace;

        for (int s = 0; s < lines.size(); s++) {
            String[] elems;
            if (lines.get(s).matches("\\s*@loop.*")) {
                listParams = lines.get(s).substring((lines.get(s).lastIndexOf("(") + 1), (lines.get(s).length()) - 1).split(",")[0];
                try {
                    elems = System.getProperty(listParams).split(",");
                } catch (Exception e) {
                    logger.error("-> Error while parsing params. {} is not defined.", listParams);
                    throw new Exception("-> Error while parsing params. {} is not defined." + listParams);
                }
                paramReplace = lines.get(s).substring((lines.get(s).lastIndexOf("(") + 1), (lines.get(s).length()) - 1).split(",")[1];
                lines.set(s, " ");
                while (!(lines.get(s).toUpperCase().contains("SCENARIO:"))) {
                    s ++;
                }
                lines.set(s,lines.get(s).replaceAll("Scenario", "Scenario Outline"));
                s++;
                while ( s < lines.size()) {
                    if ((lines.get(s).toUpperCase().contains("SCENARIO")) || lines.get(s).matches(".*@[^\\{].*")) {
                        break;
                    }
                    s++;
                }
                lines.add(s, "Examples:");
                exampleLines(paramReplace, elems, lines,  s +1);
                s = s + elems.length;
            }
        }
        return String.join("\n", lines);
    }

    public void exampleLines (String name, String[] params, List<String> lines, int num) {
        lines.add(num, "| " + name + " | " + name + ".id |");
        for (int i = 0; i < params.length; i++) {
            num++;
            lines.add(num, "| " + params[i] + " | " + i + " |");
        }
    }
}
