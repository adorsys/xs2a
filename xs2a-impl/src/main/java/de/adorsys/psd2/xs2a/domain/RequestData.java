/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.domain;

import lombok.Value;

import java.util.Map;
import java.util.UUID;

/**
 * Contains general information about the incoming request.
 */
@Value
public class RequestData {
    /**
     * Returns the part of the request's URL after the server name and port number.
     * Example: '/v1/consents'
     */
    private String uri;

    /**
     * Unique ID of the request, which was received as X-Request-ID in the header.
     */
    private UUID requestId;

    /**
     * IP address, from which the request was sent.
     */
    private String ip;

    /**
     * Headers used in the request.
     */
    private Map<String, String> headers;
}
