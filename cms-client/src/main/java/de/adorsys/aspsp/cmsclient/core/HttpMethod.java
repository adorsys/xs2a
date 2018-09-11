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

package de.adorsys.aspsp.cmsclient.core;

import org.apache.http.client.methods.*;

import static de.adorsys.aspsp.cmsclient.core.util.StringEntityUtil.buildStringEntity;

public enum HttpMethod implements HttpRequestBody {
    POST {
        @Override
        public <R> HttpRequestBase getHttpRequest(R requestBody) {
            HttpPost post = new HttpPost();
            buildStringEntity(requestBody)
                .ifPresent(post::setEntity);
            return post;
        }
    },
    GET {
        @Override
        public HttpRequestBase getHttpRequest() {
            return new HttpGet();
        }
    },
    PUT {
        @Override
        public HttpRequestBase getHttpRequest() {
            return new HttpPut();
        }

        @Override
        public <R> HttpRequestBase getHttpRequest(R requestBody) {
            HttpPut put = new HttpPut();
            buildStringEntity(requestBody)
                .ifPresent(put::setEntity);
            return put;
        }
    },
    DELETE {
        @Override
        public HttpRequestBase getHttpRequest() {
            return new HttpDelete();
        }
    }
}
