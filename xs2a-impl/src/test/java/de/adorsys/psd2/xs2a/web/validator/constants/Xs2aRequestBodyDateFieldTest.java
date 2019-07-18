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

import org.junit.Test;

import static org.junit.Assert.*;

public class Xs2aRequestBodyDateFieldTest {

    @Test
    public void checkDateFieldFormats() {
        assertEquals("Check amount of described fields", 5, Xs2aRequestBodyDateField.values().length);

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
