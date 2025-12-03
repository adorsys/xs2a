/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.util;

import java.util.Map;
import java.util.Set;

import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for resolving VOP (Verification of Party) path variables and determining
 * whether a request is for single or bulk VOP operations.
 *
 * Handles the ambiguous mapping issue where OpenAPI-generated interfaces use path variables
 * that can resolve to the same concrete paths (e.g., /v1/payee-verifications).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VopPathResolver {

    // Single VOP endpoint patterns
    private static final Set<String> SINGLE_VOP_ENDPOINTS = Set.of(
        "payee-verifications",
        "payer-verifications",
        "party-verifications"
    );

    // Bulk VOP endpoint patterns
    private static final Set<String> BULK_VOP_ENDPOINTS = Set.of(
        "bulk-payee-verifications",
        "bulk-payer-verifications",
        "bulk-party-verifications"
    );

    /**
     * Extracts the VOP endpoint name from a request path.
     *
     * @param requestPath the full request path (e.g., "/v1/payee-verifications")
     * @return the endpoint name (e.g., "payee-verifications")
     */
    public static String extractEndpointFromPath(String requestPath) {
        if (requestPath == null || !requestPath.startsWith("/v1/")) {
            throw new IllegalArgumentException("Invalid VOP request path: " + requestPath);
        }

        // Remove the "/v1/" prefix to get the endpoint name
        return requestPath.substring(4);
    }

    /**
     * Determines if the current request is for a bulk VOP operation.
     *
     * @param request the HTTP servlet request
     * @return true if the request is for bulk VOP, false for single VOP
     */
    public static boolean isBulkVopRequest(HttpServletRequest request) {
        // First, try to get from path variables (Spring MVC resolved)
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables != null) {
            // Check bulk path variable first
            String bulkEndpoint = pathVariables.get("bulk-party-verification-endpoint");
            if (bulkEndpoint != null && BULK_VOP_ENDPOINTS.contains(bulkEndpoint)) {
                return true;
            }

            // Check single path variable
            String singleEndpoint = pathVariables.get("party-verification-endpoint");
            if (singleEndpoint != null && SINGLE_VOP_ENDPOINTS.contains(singleEndpoint)) {
                return false;
            }
        }

        // Fallback: parse from request URI directly
        String requestUri = request.getRequestURI();
        return parseBulkFromUri(requestUri);
    }

    /**
     * Gets the VOP endpoint type from the request (e.g., "payee-verifications").
     *
     * @param request the HTTP servlet request
     * @return the endpoint type string, or null if not found
     */
    public static String getVopEndpointType(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (pathVariables != null) {
            // Check bulk path variable first
            String bulkEndpoint = pathVariables.get("bulk-party-verification-endpoint");
            if (bulkEndpoint != null) {
                return bulkEndpoint;
            }

            // Check single path variable
            String singleEndpoint = pathVariables.get("party-verification-endpoint");
            if (singleEndpoint != null) {
                return singleEndpoint;
            }
        }

        // Fallback: parse from URI
        return parseEndpointFromUri(request.getRequestURI());
    }

    /**
     * Parses the request URI to determine if it's a bulk request.
     *
     * @param requestUri the request URI
     * @return true if bulk request, false otherwise
     */
    private static boolean parseBulkFromUri(String requestUri) {
        if (requestUri == null) {
            return false;
        }

        // Extract the path segment that indicates VOP type
        // Expected format: /v1/{endpoint-type} or /v1/{endpoint-type}/additional/paths
        String[] pathSegments = requestUri.split("/");
        if (pathSegments.length >= 3) {
            String endpointSegment = pathSegments[2]; // v1 is at index 1, endpoint at index 2
            return BULK_VOP_ENDPOINTS.contains(endpointSegment);
        }

        return false;
    }

    /**
     * Extracts the endpoint type from the request URI.
     *
     * @param requestUri the request URI
     * @return the endpoint type, or null if not found
     */
    private static String parseEndpointFromUri(String requestUri) {
        if (requestUri == null) {
            return null;
        }

        String[] pathSegments = requestUri.split("/");
        if (pathSegments.length >= 3) {
            String endpointSegment = pathSegments[2];
            if (SINGLE_VOP_ENDPOINTS.contains(endpointSegment) || BULK_VOP_ENDPOINTS.contains(endpointSegment)) {
                return endpointSegment;
            }
        }

        return null;
    }
}
