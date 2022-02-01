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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class Xs2aRequestBodyDateFieldTest {

    @Test
    void checkDateFieldFormats() {
        assertEquals(5, Xs2aRequestBodyDateField.values().length);

        assertEquals("requestedExecutionDate", Xs2aRequestBodyDateField.REQUESTED_EXECUTION_DATE.getFieldName());
        assertEquals(Xs2aBodyDateFormatter.ISO_DATE, Xs2aRequestBodyDateField.REQUESTED_EXECUTION_DATE.getFormatter());

        assertEquals("requestedExecutionTime", Xs2aRequestBodyDateField.REQUESTED_EXECUTION_TIME.getFieldName());
        assertEquals(Xs2aBodyDateFormatter.ISO_DATE_TIME, Xs2aRequestBodyDateField.REQUESTED_EXECUTION_TIME.getFormatter());

        assertEquals("startDate", Xs2aRequestBodyDateField.START_DATE.getFieldName());
        assertEquals(Xs2aBodyDateFormatter.ISO_DATE, Xs2aRequestBodyDateField.START_DATE.getFormatter());

        assertEquals("endDate", Xs2aRequestBodyDateField.END_DATE.getFieldName());
        assertEquals(Xs2aBodyDateFormatter.ISO_DATE, Xs2aRequestBodyDateField.END_DATE.getFormatter());

        assertEquals("validUntil", Xs2aRequestBodyDateField.VALID_UNTIL.getFieldName());
        assertEquals(Xs2aBodyDateFormatter.ISO_DATE, Xs2aRequestBodyDateField.VALID_UNTIL.getFormatter());
    }
}
