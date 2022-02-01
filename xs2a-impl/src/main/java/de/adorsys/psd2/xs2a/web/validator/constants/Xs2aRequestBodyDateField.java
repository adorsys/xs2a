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

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aBodyDateFormatter.ISO_DATE;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aBodyDateFormatter.ISO_DATE_TIME;

public enum Xs2aRequestBodyDateField {
    REQUESTED_EXECUTION_DATE("requestedExecutionDate", ISO_DATE),
    REQUESTED_EXECUTION_TIME("requestedExecutionTime", ISO_DATE_TIME),

    START_DATE("startDate", ISO_DATE),
    END_DATE("endDate", ISO_DATE),

    VALID_UNTIL("validUntil", ISO_DATE);

    private String fieldName;
    private Xs2aBodyDateFormatter formatter;

    Xs2aRequestBodyDateField(String fieldName, Xs2aBodyDateFormatter formatter) {
        this.fieldName = fieldName;
        this.formatter = formatter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Xs2aBodyDateFormatter getFormatter() {
        return formatter;
    }
}
