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

package de.adorsys.psd2.report.jpa.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlEventReportBuilderTest {
    private static final String TEST_REQUEST_NAME = "json/test_event_report_db.sql";

    private SqlEventReportBuilder builder;

    @BeforeEach
    void init() {
        builder = new SqlEventReportBuilder();
        ReflectionTestUtils.setField(builder, "schemaName", "event");
        ReflectionTestUtils.setField(builder, "sqlRequestFileName", TEST_REQUEST_NAME);
    }

    @Test
    void test_period() {
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
    void test_instanceId() {
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
    void test_consentId() {
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
    void test_paymentId() {
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
    void test_eventType() {
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
    void test_eventOrigin() {
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
