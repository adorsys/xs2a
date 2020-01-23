/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import java.util.EnumSet;
import java.util.Set;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateField.*;

public enum Xs2aRequestBodyDateFields {
    PAYMENT_DATE_FIELDS(EnumSet.of(REQUESTED_EXECUTION_DATE, REQUESTED_EXECUTION_TIME, START_DATE, END_DATE)),

    AIS_CONSENT_DATE_FIELDS(EnumSet.of(VALID_UNTIL));

    private Set<Xs2aRequestBodyDateField> dateFields;

    Xs2aRequestBodyDateFields(Set<Xs2aRequestBodyDateField> dateFields) {
        this.dateFields = dateFields;
    }

    public Set<Xs2aRequestBodyDateField> getDateFields() {
        return dateFields;
    }
}
