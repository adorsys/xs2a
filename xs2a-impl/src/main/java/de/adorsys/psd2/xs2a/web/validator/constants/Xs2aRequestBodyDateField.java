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
