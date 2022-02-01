/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.component.logger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
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
        private Map<String, String> logParams = new LinkedHashMap<>();
        private TppLogType tppLogType;

        TppLogBuilder(TppLogType tppLogType) {
            this.tppLogType = tppLogType;
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

        protected abstract T getThis();
    }
}
