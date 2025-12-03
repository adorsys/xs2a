/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.header;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Builder class for constructing VOP (Verification of Party) response headers.
 * This class is responsible for building headers required by the VOP API specification,
 * including reference API information and request tracking headers.
 */
@Component
public class VopInitiationHeadersBuilder {
    private static final String DEFAULT_REFERENCE_API_NAME = "Berlin Group openFinance API";
    private static final String DEFAULT_REFERENCE_API_VERSION = "2.1.3";
    private static final String DEFAULT_REFERENCE_API_DOCUMENT = "Extended Payment Initiation Services";

    private final String referenceApiName;
    private final String referenceApiVersion;
    private final String referenceApiDocument;

    /**
     * Constructor with configurable reference API properties.
     * Uses default values from YAML spec if not configured.
     *
     * @param referenceApiName reference API name (defaults to Berlin Group openFinance API)
     * @param referenceApiVersion reference API version (defaults to 2.1.3)
     * @param referenceApiDocument reference API document (defaults to Extended Payment Initiation Services)
     */
    public VopInitiationHeadersBuilder(
            @Value("${xs2a.vop.reference-api-name:" + DEFAULT_REFERENCE_API_NAME + "}") String referenceApiName,
            @Value("${xs2a.vop.reference-api-version:" + DEFAULT_REFERENCE_API_VERSION + "}") String referenceApiVersion,
            @Value("${xs2a.vop.reference-api-document:" + DEFAULT_REFERENCE_API_DOCUMENT + "}") String referenceApiDocument) {
        this.referenceApiName = referenceApiName;
        this.referenceApiVersion = referenceApiVersion;
        this.referenceApiDocument = referenceApiDocument;
    }

    /**
     * Builds VOP response headers for successful VOP initiation requests.
     * According to the YAML spec (Ok_200_VOP), the following headers are required:
     * - X-Reference-API-Name: The name of the API (e.g., "Berlin Group openFinance API")
     * - X-Reference-API-Document: The name of the Implementation Guideline document
     * - X-Reference-API-Version: The version of the API (e.g., "2.1.3")
     * - X-Request-ID: The request ID for tracking
     *
     * @param requestId the X-Request-ID header value for request tracking
     * @return VopResponseHeaders containing all required headers
     */
    public VopResponseHeaders buildInitiateVopHeaders(@NotNull String requestId) {
        return VopResponseHeaders.builder()
                .referenceApiName(referenceApiName)
                .referenceApiDocument(referenceApiDocument)
                .referenceApiVersion(referenceApiVersion)
                .requestId(requestId)
                .build();
    }
}
