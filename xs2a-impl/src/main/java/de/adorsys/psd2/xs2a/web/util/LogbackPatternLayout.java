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

package de.adorsys.psd2.xs2a.web.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class LogbackPatternLayout extends PatternLayout {
    private final Map<Pattern, String> mapForReplacement = new HashMap<>();
    static final String MASK = "*****";

    public LogbackPatternLayout() {
        String replaceField = "$1\"" + MASK + "\"";
        String replaceHeader = "$1" + MASK;
        UnaryOperator<String> replace = name -> "\"" + name + "\":\"" + MASK + "\"";

        UnaryOperator<String> fieldRegex = name -> "(\"" + name + "\"\\s*:\\s*)\"[^\"]+\"";
        UnaryOperator<String> headerRegex = name -> "(" + name + "\\s*:\\s*)[^,]+";
        UnaryOperator<String> objectRegex = name -> "(\"" + name + "\"\\s*:\\s*\\{)[^}]+}";

        Function<String, Pattern> buildPattern = regex ->  Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        mapForReplacement.put(buildPattern.apply(fieldRegex.apply("ownerName")), replaceField);
        mapForReplacement.put(buildPattern.apply(fieldRegex.apply("\\w*[Pp]assword")), replaceField);
        mapForReplacement.put(buildPattern.apply(fieldRegex.apply("access_token")), replaceField);
        mapForReplacement.put(buildPattern.apply(fieldRegex.apply("refresh_token")), replaceField);
        mapForReplacement.put(buildPattern.apply(headerRegex.apply("Authorization")), replaceHeader);
        mapForReplacement.put(buildPattern.apply(objectRegex.apply("ownerAddress")), replace.apply("ownerAddress"));
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return modifyMessage(super.doLayout(event));
    }

    String modifyMessage(String message) {
        String logMessage = message;
        for (Map.Entry<Pattern, String> next : mapForReplacement.entrySet()) {
            logMessage = next.getKey().matcher(logMessage).replaceAll(next.getValue());
        }
        return logMessage;
    }
}

