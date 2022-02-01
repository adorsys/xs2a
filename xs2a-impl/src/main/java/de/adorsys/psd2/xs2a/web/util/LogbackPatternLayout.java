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

