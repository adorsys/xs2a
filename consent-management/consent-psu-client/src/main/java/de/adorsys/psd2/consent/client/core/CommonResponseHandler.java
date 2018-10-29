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

package de.adorsys.psd2.consent.client.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static de.adorsys.psd2.consent.client.core.util.ObjectMapperUtil.toObject;


public class CommonResponseHandler<R> implements ResponseHandler<R> {
    private static final Log logger = LogFactory.getLog(CommonResponseHandler.class);
    private final Class<R> responseClass;

    public CommonResponseHandler(Class<R> responseClass) {
        this.responseClass = responseClass;
    }

    @Override
    public R handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        logger.info("Http status: " + status);
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            String asString = null;
            if (entity != null) {
                asString = EntityUtils.toString(entity);
            }
            logger.info("body: " + asString);
            return toObject(asString, responseClass)
                       .orElse(null);
        }
        throw new ClientProtocolException("Unexpected response status: " + status);
    }
}
