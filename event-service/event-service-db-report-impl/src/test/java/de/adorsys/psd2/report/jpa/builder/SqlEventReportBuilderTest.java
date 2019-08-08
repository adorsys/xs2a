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

package de.adorsys.psd2.report.jpa.builder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlEventReportBuilderTest {
    public static final String TEST_REQUEST_NAME = "json/test_event_report_db.sql";

    private SqlEventReportBuilder builder;

    @Before
    public void init() {
        builder = new SqlEventReportBuilder(TEST_REQUEST_NAME);
    }

    @Test
    public void test_period() {
        // Given
        String expectedSql = "select * from event\n" +
                                 "where timestamp between :periodFrom and :periodTo order by timestamp ";

        // When
        String actualSql = builder
                               .baseRequest()
                               .period()
                               .build();

        // Then
        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void test_instanceId() {
        // Given
        String expectedSql = "select * from event\n" +
                                 "where ev.instance_id = :instanceId order by timestamp ";

        // When
        String actualSql = builder
                               .baseRequest()
                               .instanceId()
                               .build();

        // Then
        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void test_consentId() {
        // Given
        String expectedSql = "select * from event\n" +
                                 "where ev.consent_id = :consentId order by timestamp ";

        // When
        String actualSql = builder
                               .baseRequest()
                               .consentId()
                               .build();

        // Then
        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void test_paymentId() {
        // Given
        String expectedSql = "select * from event\n" +
                                 "where ev.payment_id = :paymentId order by timestamp ";

        // When
        String actualSql = builder
                               .baseRequest()
                               .paymentId()
                               .build();

        // Then
        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void test_eventType() {
        // Given
        String expectedSql = "select * from event\n" +
                                 "where ev.event_type = :eventType order by timestamp ";

        // When
        String actualSql = builder
                               .baseRequest()
                               .eventType()
                               .build();

        // Then
        assertEquals(expectedSql, actualSql);
    }

    @Test
    public void test_eventOrigin() {
        // Given
        String expectedSql = "select * from event\n" +
                                 "where ev.event_origin = :eventOrigin order by timestamp ";

        // When
        String actualSql = builder
                               .baseRequest()
                               .eventOrigin()
                               .build();

        // Then
        assertEquals(expectedSql, actualSql);
    }

}
