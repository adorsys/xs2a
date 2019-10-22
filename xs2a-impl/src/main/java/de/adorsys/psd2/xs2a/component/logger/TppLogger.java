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

package de.adorsys.psd2.xs2a.component.logger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j(topic = "access-log")
public class TppLogger {

    private TppLogger() {
    }

    public static TppRequestLogBuilder logRequest(HttpServletRequest request) {
        return new TppRequestLogBuilder(request);
    }

    public static TppResponseLogBuilder logResponse(HttpServletResponse response) {
        return new TppResponseLogBuilder(response);
    }

    public abstract static class TppLogBuilder<T extends TppLogBuilder<T>> {
        protected static final String X_REQUEST_ID = "X-Request-ID";
        private static final String INTERNAL_REQUEST_ID = "InR-ID";

        private Map<String, String> logParams = new LinkedHashMap<>();
        private TppLogType tppLogType;

        TppLogBuilder(TppLogType tppLogType) {
            this.tppLogType = tppLogType;
        }

        public T withInternalRequestId(@NotNull UUID internalRequestId) {
            putLogParameter(INTERNAL_REQUEST_ID, internalRequestId.toString());
            return getThis();
        }

        public T withXRequestId() {
            putLogParameter(X_REQUEST_ID, getXRequestIdValue());
            return getThis();
        }

        public T withParam(String paramName, String paramValue) {
            if (StringUtils.isNotBlank(paramValue)) {
                putLogParameter(paramName, paramValue);
            }
            return getThis();
        }

        public void perform() {
            String logMessageParams = logParams.entrySet()
                                          .stream()
                                          .map(e -> e.getKey() + ": [" + e.getValue() + "]")
                                          .collect(Collectors.joining(", "));

            log.info(tppLogType.name() + " - " + logMessageParams);
        }

        void putLogParameter(String parameterName, String parameterValue) {
            logParams.put(parameterName, parameterValue);
        }

        protected abstract String getXRequestIdValue();

        protected abstract T getThis();
    }
}
