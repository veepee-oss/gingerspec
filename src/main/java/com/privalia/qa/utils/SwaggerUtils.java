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

import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import io.swagger.models.HttpMethod;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Additional operations with rest api spec (swagger).
 */
public class SwaggerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUtils.class);

    private OpenAPI spec;

    private String path;

    private URL server;

    private Map<String, OpenAPI> cache = new HashMap<>();

    /**
     * Get swagger spec.
     *
     * @return OpenAPI
     */
    public OpenAPI getSpec() {
        return this.spec;
    }

    /**
     * Swagger server is secured.
     *
     * @return Boolean
     */
    public Boolean isSecured() {
        return this.server.getProtocol().equalsIgnoreCase("https");
    }

    /**
     * Get swagger server host.
     *
     * @return String
     */
    public String getHost() {
        if (this.server.getPort() == -1) {
            return this.server.getHost();
        }

        return this.server.getHost() + ":" + this.server.getPort();
    }

    /**
     * Get swagger validator.
     *
     * @return OpenApiValidationFilter
     */
    public OpenApiValidationFilter getValidator() {
        return new OpenApiValidationFilter(this.path);
    }

    /**
     * Read the swagger spec.
     *
     * @param path   File path to swagger spec or URI to swagger spec
     * @param server Server index to which requests will be sent (default: 0)
     */
    public void initialize(String path, Integer server) throws MalformedURLException {

        if (!this.cache.containsKey(path)) {
            SwaggerParseResult result = new OpenAPIParser().readLocation(path, null, null);

            Assertions
                    .assertThat(result.getMessages())
                    .as("Incorrect swagger specification: " + result.getMessages().toString())
                    .isNotNull();

            this.spec = result.getOpenAPI();
        } else {
            this.spec = this.cache.get(path);
        }

        this.path = path;

        if (server == null) {
            server = 0;
        }

        Assertions
                .assertThat(this.spec.getServers())
                .as("Server with index " + server + " not found in swagger spec")
                .hasSizeGreaterThanOrEqualTo(server + 1);

        this.server = new URL(this.spec.getServers().get(server).getUrl());
    }

    /**
     * Prepared request and returns the method and base path for the request based on the swagger spec
     *
     * @param operationId Operation ID from swagger spec
     * @return SwaggerMethod
     */
    public SwaggerMethod getMethod(String operationId) {

        Assertions.assertThat(this.spec).as("Incorrect swagger specification").isNotNull();

        String basePath = this.trim(this.server.getPath(), "/");

        for (Map.Entry<String, PathItem> resource: this.spec.getPaths().entrySet()) {
            String path = resource.getKey();
            PathItem item = resource.getValue();

            if (item.getGet() != null && item.getGet().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.GET, "/" + this.ltrim(basePath + path, "/"));
            }

            if (item.getPost() != null && item.getPost().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.POST, "/" + this.ltrim(basePath + path, "/"));
            }

            if (item.getPut() != null && item.getPut().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.PUT, "/" + this.ltrim(basePath + path, "/"));
            }

            if (item.getDelete() != null && item.getDelete().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.DELETE, "/" + this.ltrim(basePath + path, "/"));
            }

            if (item.getOptions() != null && item.getOptions().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.OPTIONS, "/" + this.ltrim(basePath + path, "/"));
            }

            if (item.getHead() != null && item.getHead().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.HEAD, "/" + this.ltrim(basePath + path, "/"));
            }

            if (item.getPatch() != null && item.getPatch().getOperationId().equals(operationId)) {
                return new SwaggerMethod(HttpMethod.PATCH, "/" + this.ltrim(basePath + path, "/"));
            }
        }

        return null;

    }

    /**
     * Both trim
     *
     * @param text
     * @param trimBy
     * @return String
     */
    private String trim(String text, String trimBy) {
        return this.ltrim(this.rtrim(text, trimBy), trimBy);
    }

    /**
     * Left trim
     *
     * @param text
     * @param trimBy
     * @return String
     */
    private String ltrim(String text, String trimBy) {
        int beginIndex = 0;
        int endIndex = text.length();

        while (text.substring(beginIndex, endIndex).startsWith(trimBy)) {
            beginIndex += trimBy.length();
        }

        return text.substring(beginIndex, endIndex);
    }

    /**
     * Right trim
     *
     * @param text
     * @param trimBy
     * @return String
     */
    private String rtrim(String text, String trimBy) {
        int beginIndex = 0;
        int endIndex = text.length();

        while (text.substring(beginIndex, endIndex).endsWith(trimBy)) {
            endIndex -= trimBy.length();
        }

        return text.substring(beginIndex, endIndex);
    }
}