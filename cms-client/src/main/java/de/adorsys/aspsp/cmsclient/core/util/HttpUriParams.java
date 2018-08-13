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

package de.adorsys.aspsp.cmsclient.core.util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

public class HttpUriParams {
    private final Map<String, String> requestParams;
    private final Map<String, String> pathVariables;

    private HttpUriParams(final Map<String, String> requestParams, final Map<String, String> pathVariables) {
        this.requestParams = requestParams;
        this.pathVariables = pathVariables;
    }

    public static HttpUriParams.Builder builder() {
        return new HttpUriParams.Builder();
    }

    public Map<String, String> getPathVariables() {
        return Collections.unmodifiableMap(this.pathVariables);
    }

    public List<NameValuePair> getRequestParams() {
        List<NameValuePair> paramsList = new ArrayList<>();
        for (String name : this.requestParams.keySet()) {
            paramsList.add(new BasicNameValuePair(name, this.requestParams.get(name)));
        }
        return paramsList;
    }

    public static class Builder {
        private final Map<String, String> requestParams;
        private final Map<String, String> pathVariables;

        Builder() {
            requestParams = new LinkedHashMap<>();
            pathVariables = new LinkedHashMap<>();
        }

        public HttpUriParams.Builder addRequestParam(final String name, final String value) {
            requestParams.put(name, value);
            return this;
        }

        public HttpUriParams.Builder addPathVariable(final String name, final String value) {
            pathVariables.put(name, value);
            return this;
        }

        public HttpUriParams build() {
            return new HttpUriParams(this.requestParams, this.pathVariables);
        }
    }
}
