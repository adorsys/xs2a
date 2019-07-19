/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.component;

import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletResponse;

/**
 * HttpServletResponse wrapper that allows response content to be stored and retrieved
 */
public class MultiReadHttpServletResponse extends ContentCachingResponseWrapper {
    /**
     * Create a new ContentCachingResponseWrapper for the given servlet response.
     *
     * @param response the original servlet response
     */
    public MultiReadHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    /**
     * Returns cached response content
     *
     * @return cached response
     */
    public byte[] getCachedContent() {
        return this.getContentAsByteArray();
    }
}
