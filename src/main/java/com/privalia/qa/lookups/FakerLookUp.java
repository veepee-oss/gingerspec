package com.privalia.qa.lookups;

import com.github.javafaker.Faker;
import org.apache.commons.text.lookup.StringLookup;

import java.util.Locale;

/**
 * Evaluates the given expression using the java faker library
 *
 * @see <a href="https://github.com/DiUS/java-faker">Java Faker</a>
 */
public class FakerLookUp implements StringLookup {

    Faker faker = new Faker();

    @Override
    public String lookup(String key) {
        if (key == null) {
            return null;
        }

        String[] arr = key.split(":");

        if (arr.length >=2) {
            this.faker = new Faker((new Locale(arr[0])));
            key = arr[1];
        } else {
            this.faker = new Faker();
        }

        return this.faker.expression("#{" + key + "}");
    }
}
