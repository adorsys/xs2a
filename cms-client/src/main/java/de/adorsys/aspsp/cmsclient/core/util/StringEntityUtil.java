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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public final class StringEntityUtil {
    private static final Log logger = LogFactory.getLog(StringEntityUtil.class);

    private StringEntityUtil() {}

    public static <R> Optional<StringEntity> buildStringEntity(R requestBody) {
        try {
            String asJson = ObjectMapperUtil.toJson(requestBody)
                                .orElse("");
            return Optional.of(new StringEntity(asJson));
        } catch (UnsupportedEncodingException e) {
            logger.error("Can't convert object to StringEntity");
        }
        return Optional.empty();
    }
}
