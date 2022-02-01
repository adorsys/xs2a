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
