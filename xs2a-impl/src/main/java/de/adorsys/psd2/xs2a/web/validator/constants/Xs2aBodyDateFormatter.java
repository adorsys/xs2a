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

package de.adorsys.psd2.xs2a.web.validator.constants;

import java.time.format.DateTimeFormatter;

public enum Xs2aBodyDateFormatter {
    ISO_DATE(DateTimeFormatter.ISO_DATE, "YYYY-MM-DD"),
    ISO_DATE_TIME(DateTimeFormatter.ISO_DATE_TIME, "YYYY-MM-DD'T'HH:mm:ssZ");

    private DateTimeFormatter formatter;
    private String pattern;

    Xs2aBodyDateFormatter(DateTimeFormatter formatter, String pattern) {
        this.formatter = formatter;
        this.pattern = pattern;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public String getPattern() {
        return pattern;
    }
}
