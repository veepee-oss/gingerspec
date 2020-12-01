///*
// * Copyright (C) 2014 Stratio (http://stratio.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.privalia.qa.aspects;
//
//import gherkin.ast.Location;
//import gherkin.ast.Tag;
//import org.testng.annotations.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
//
//public class RunOnEnvTagAspectTest {
//
//    public RunOnTagAspect runontag = new RunOnTagAspect();
//
//    @Test
//    public void testGetParams() throws Exception {
//        String[] expectedResponse = {"HELLO", "BYE"};
//        assertThat(expectedResponse).as("Params are correctly obtained").isEqualTo(runontag.getParams("@runOnEnv(HELLO,BYE)"));
//    }
//
//    @Test
//    public void testCheckParams() throws Exception {
//        System.setProperty("HELLO_OK","OK");
//        assertThat(true).as("Params are correctly checked").isEqualTo(runontag.checkParams(runontag.getParams("@runOnEnv(HELLO_OK)")));
//    }
//
//    @Test
//    public void testCheckParams_2() throws Exception {
//        System.setProperty("HELLO_KO","KO");
//        assertThat(false).as("Params are correctly checked 2").isEqualTo(runontag.checkParams(runontag.getParams("@runOnEnv(HELLO_KO,BYE_KO)")));
//    }
//    @Test
//    public void testCheckEmptyParams() throws Exception {
//        assertThatExceptionOfType(Exception.class).isThrownBy(() -> runontag.checkParams(runontag.getParams("@runOnEnv()")))
//                .withMessage("Error while parsing params. Params must be at least one");
//    }
//    @Test
//    public void testGetEmptyParams() throws Exception {
//        assertThatExceptionOfType(Exception.class).isThrownBy(() -> runontag.getParams("@runOnEnv"))
//                .withMessage("Error while parsing params. Format is: \"runOnEnv(PARAM)\", but found: " + "@runOnEnv");
//    }
//
//    @Test
//    public void testTagIterationRun() throws Exception {
//        System.setProperty("HELLO","OK");
//        List<Tag> tagList = new ArrayList<>();
//        tagList.add(new Tag(new Location(1,1),"@runOnEnv(HELLO)"));
//        assertThat(false).isEqualTo(runontag.tagsIteration(tagList,new Location(1,1)));
//    }
//
//    @Test
//    public void testTagIterationIgnoreRun() throws Exception {
//        List<Tag> tagList = new ArrayList<>();
//        tagList.add(new Tag(new Location(1,1), "@runOnEnv(BYE)"));
//        assertThat(true).isEqualTo(runontag.tagsIteration(tagList,new Location(1,1)));
//    }
//
//    @Test
//    public void testTagIterationSkip() throws Exception {
//        List<Tag> tagList = new ArrayList<>();
//        tagList.add(new Tag(new Location(1,1), "@skipOnEnv(HELLO_NO)"));
//        assertThat(false).isEqualTo(runontag.tagsIteration(tagList,new Location(1,1)));
//    }
//
//    @Test
//    public void testTagIterationIgnoreSkip() throws Exception {
//        System.setProperty("HELLO","OK");
//        List<Tag> tagList = new ArrayList<>();
//        tagList.add(new Tag(new Location(1,1), "@skipOnEnv(HELLO)"));
//        assertThat(true).isEqualTo(runontag.tagsIteration(tagList,new Location(1,1)));
//    }
//}
