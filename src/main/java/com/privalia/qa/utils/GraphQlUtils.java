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

package com.privalia.qa.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Additional operations with rest api spec (graphql).
 */
public class GraphQlUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQlUtils.class);

    private JSONObject variables = new JSONObject();

    /**
     * Set GraphQl variables.
     *
     * @param variables Graphql variables as json
     */
    public void setVariables(JSONObject variables) {
        this.variables = variables;
    }

    /**
     * Add GraphQl variable.
     *
     * @param key   Graphql variables key
     * @param value Graphql variables value
     */
    public void addVariable(String key, String value) {
        if (value.equalsIgnoreCase("null")) {
            this.variables.put(key, Optional.ofNullable(null));

            return;
        }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            this.variables.put(key, value.equalsIgnoreCase("true"));

            return;
        }

        if (NumberUtils.isDigits(value)) {
            this.variables.put(key, NumberUtils.createNumber(value));

            return;
        }

        this.variables.put(key, value);
    }

    /**
     * Build GraphQl request
     *
     * @param query GraphQl query
     * @return String
     */
    public String build(String query) {
        return new JSONObject()
                .put("query", query)
                .put("variables", this.variables)
                .toString();
    }
}