/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.specs;


import com.privalia.qa.exceptions.DBException;
import com.privalia.qa.utils.ThreadProperty;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class MongoToolsIT extends BaseGSpec {
    BigDataGSpec commonspecG;
    String doc;
    String db = "mongoITDB";
    String collection = "testCollection";
    public MongoToolsIT() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        this.commonspec = new CommonG();
        commonspecG = new BigDataGSpec(this.commonspec);
    }

    @BeforeClass(enabled = false)
    public void prepareMongo() throws DBException {
        commonspec.getMongoDBClient().connect();
        commonspec.getMongoDBClient().connectToMongoDBDataBase(db);
    }

    @Test(enabled = false)
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
        Assertions.assertThat(commonspec.getMongoDBClient().exitsCollections(collection)).as("The collection has been correctly created.").isEqualTo(true);
        commonspec.getLogger().debug("Verifying if a document exists at {}", this.collection);
        Assertions.assertThat(commonspec.getMongoDBClient().getMongoDBCollection(collection).getCount()).as("One doc has been inserted.").isEqualTo(1);
        commonspec.getLogger().debug("Verifying the document {}", this.collection);
        Assertions.assertThat(commonspec.getMongoDBClient().getMongoDBCollection(collection).find().one().toString()).as("Doc contains {}", doc).contains("rolesid");

    }


    @Test(enabled = false, expectedExceptions = com.mongodb.util.JSONParseException.class)
    public void insertionAtMongo_malformedFail() {
        doc = "}";
        commonspec.getLogger().debug("Verifying document can't be malformed");
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, doc);
    }

    @Test(enabled = false, expectedExceptions = IllegalArgumentException.class)
    public void insertionAtMongo_nullFail() {
        doc = "";
        commonspec.getLogger().debug("Verifying document cant be null");
        commonspec.getMongoDBClient().insertDocIntoMongoDBCollection(collection, doc);
    }


    @AfterClass(enabled = false)
    public void cleanMongo() {
        commonspec.getMongoDBClient().dropMongoDBDataBase(db);
        commonspec.getMongoDBClient().disconnect();
    }
}
