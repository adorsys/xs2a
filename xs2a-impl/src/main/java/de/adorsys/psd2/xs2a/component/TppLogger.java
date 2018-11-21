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

package de.adorsys.psd2.xs2a.component;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j(topic = "access-log")
public class TppLogger {

    public static TppLogBuilder logRequest() {
        return new TppLogBuilder(TppLogType.REQUEST);
    }

    public static TppLogBuilder logResponse() {
        return new TppLogBuilder(TppLogType.RESPONSE);
    }

    public static class TppLogBuilder {
        private Map<String, String> logParams = new LinkedHashMap<>();
        private TppLogType tppLogType;

        private TppLogBuilder(TppLogType tppLogType) {
            this.tppLogType = tppLogType;
        }

        public TppLogBuilder withParam(String paramName, String paramValue) {
            logParams.put(paramName, paramValue);
            return this;
        }

        public void perform() {
            String logMessageParams = logParams.entrySet()
                                 .stream()
                                 .map(e -> e.getKey() + ": [" + e.getValue() + "]")
                                 .collect(Collectors.joining(", "));

            log.info(tppLogType.name() + " - " + logMessageParams);
        }
    }

    private enum TppLogType {
        REQUEST,
        RESPONSE
    }
}
