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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.xs2a.integration.HttpHeadersMock;

public class HttpHeadersBuilder {
    public static HttpHeadersMock buildHttpHeaders() {
        HttpHeadersMock httpHeaders = new HttpHeadersMock();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");

        return httpHeaders;
    }
}
