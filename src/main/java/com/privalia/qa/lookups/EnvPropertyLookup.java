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

package com.privalia.qa.lookups;

import com.privalia.qa.aspects.ReplacementAspect;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.text.lookup.StringLookup;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Custom lookup for getting values from properties files
 */
public class EnvPropertyLookup implements StringLookup {

    /**
     * The file that contains all common configuration for the project (environment
     * independent configuration) must be located in /resources/configuration/common.properties
     * <p>
     * Environment-specific configuration can be located in a separated file. This configuration can
     * override the configuration from the common file. All environment specific configuration files
     * can be included at runtime via maven variable, setting the 'env' to the name of the file.
     * <p>
     * for example, to use properties from the file pre.properties located in
     * /resources/configuration/pre.properties, just pass -Denv=pre when
     * running your tests
     * @param key   property to locate
     * @return      value of the property
     */
    @Override
    public String lookup(String key) {

        if (key == null) {
            return null;
        }

        try {
            Parameters params = new Parameters();
            CombinedConfiguration config = new CombinedConfiguration(new OverrideCombiner());

            /*If environment specific file is required, search it by its name and add it as a source of properties*/
            String environment = System.getProperty("env", null);
            if (environment != null) {
                FileBasedConfigurationBuilder<FileBasedConfiguration> config2 = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                        PropertiesConfiguration.class).configure(params.properties().setFile(getfile(environment)));
                config.addConfiguration(config2.getConfiguration());
            }

            /*Add the file common.properties as a source of properties*/
            FileBasedConfigurationBuilder<FileBasedConfiguration> config1 = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
                    PropertiesConfiguration.class)
                    .configure(params.properties().setFile(getfile("common")));

            config.addConfiguration(config1.getConfiguration());
            return config.getString(key);
        } catch (final ConfigurationException e) {
            throw new IllegalArgumentException(String.format("Could not find property %s in included properties files (under resources/configuration/).", key), e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static File getfile(String environment) throws URISyntaxException {

        URL url = ReplacementAspect.class.getClassLoader().getResource("configuration/" + environment + ".properties");

        if (url != null) {
            return new File(url.toURI());
        } else {
            throw new IllegalArgumentException(String.format("The configuration file %s.properties was not found", environment));
        }
    }
}
