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

import de.adorsys.aspsp.cmsclient.core.util.HttpUriParams;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRequestMethod<T, R> implements RestRequestMethod<T, R> {
    private final Class<R> responseClass;
    private final T requestObject;
    private final HttpMethod httpMethod;
    private final String path;
    private HttpUriParams uriParams;

    @SuppressWarnings("unchecked")
    public AbstractRequestMethod(final T requestObject, final HttpMethod httpMethod, final String path) {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        responseClass = (Class<R>) genericSuperclass.getActualTypeArguments()[1];
        this.requestObject = requestObject;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    public AbstractRequestMethod(final T requestObject, final HttpMethod httpMethod, final String path, HttpUriParams uriParams) {
        this(requestObject, httpMethod, path);
        this.uriParams = uriParams;
    }

    public AbstractRequestMethod(final HttpMethod httpMethod, final String path, HttpUriParams uriParams) {
        this(null, httpMethod, path);
        this.uriParams = uriParams;
    }

    @Override
    public Class<R> responseClass() {
        return responseClass;
    }

    @Override
    public T requestBody() {
        return this.requestObject;
    }

    @Override
    public HttpMethod httpMethod() {
        return this.httpMethod;
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public List<Header> headers() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
        headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        return headers;
    }

    @Override
    public HttpUriParams uriParams() {
        return uriParams;
    }
}
