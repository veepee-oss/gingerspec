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
package com.stratio.qa.specs;


import com.stratio.qa.exceptions.DBException;
import com.stratio.qa.utils.ThreadProperty;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MongoToolsIT extends BaseGSpec {
    GivenGSpec commonspecG;
    String doc;
    String db = "mongoITDB";
    String collection = "testCollection";
    public MongoToolsIT() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        this.commonspec = new CommonG();
        commonspecG = new GivenGSpec(this.commonspec);
    }

    @BeforeClass
    public void prepareMongo() throws DBException {
        commonspec.getMongoDBClient().connect();
        commonspec.getMongoDBClient().connectToMongoDBDataBase(db);
    }

    @Test
    public void insertionAtMongo_success() {
        doc = "{\n" +
                "  \"id\":\"id\",\n" +
                "  \"name\":\"name\",\n" +
                "  \"description\":\"description\",\n" +
                "  \"groups\": [{\"id\":\"groupname\",\"name\":\"groupname\"}],\n" +
                "  \"roles\": [\"rolesid\"]\n" +
                "}";

        commonspec.getLogger().debug("Verifying if the collection {} exists at {}", this.db, this.collection);
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, doc);
        assertThat(commonspec.getMongoDBClient().exitsCollections(collection)).as("The collection has been correctly created.").isEqualTo(true);
        commonspec.getLogger().debug("Verifying if a document exists at {}", this.collection);
        assertThat(commonspec.getMongoDBClient().getMongoDBCollection(collection).getCount()).as("One doc has been inserted.").isEqualTo(1);
        commonspec.getLogger().debug("Verifying the document {}", this.collection);
        assertThat(commonspec.getMongoDBClient().getMongoDBCollection(collection).find().one().toString()).as("Doc contains {}", doc).contains("rolesid");

    }


    @Test(expectedExceptions = com.mongodb.util.JSONParseException.class)
    public void insertionAtMongo_malformedFail() {
        doc = "}";
        commonspec.getLogger().debug("Verifying document can't be malformed");
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, doc);
    }

    @Test(expectedExceptions = java.lang.IllegalArgumentException.class)
    public void insertionAtMongo_nullFail() {
        doc = "";
        commonspec.getLogger().debug("Verifying document cant be null");
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, doc);
    }


    @AfterClass
    public void cleanMongo() {
        commonspec.getMongoDBClient().dropMongoDBDataBase(db);
        commonspec.getMongoDBClient().disconnect();
    }
}
