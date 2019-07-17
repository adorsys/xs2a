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

package de.adorsys.psd2.event.persist.jpa;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.entity.EventEntityForReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class EventReportRepository {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Autowired
    private SqlEventReportBuilder sqlEventReportBuilder;

    public List<EventEntityForReport> getEventsForPeriod(OffsetDateTime periodFrom, OffsetDateTime periodTo, String instanceId) {
        EventReportSqlParameterSourceBuilder parameters = new EventReportSqlParameterSourceBuilder()
                                                              .periodFrom(periodFrom)
                                                              .periodTo(periodTo)
                                                              .instanceId(instanceId)
                                                              .build();

        String sqlRequest = sqlEventReportBuilder
                                .period()
                                .instanceId()
                                .build();

        return namedParameterJdbcTemplate.query(sqlRequest, parameters, new BeanPropertyRowMapper<>(EventEntityForReport.class));
    }

    public List<EventEntityForReport> findByTimestampBetweenAndConsentIdAndInstanceIdOrderByTimestampAsc(OffsetDateTime periodFrom, OffsetDateTime periodTo, String consentId, String instanceId) {
        EventReportSqlParameterSourceBuilder parameters = new EventReportSqlParameterSourceBuilder()
                                                              .periodFrom(periodFrom)
                                                              .periodTo(periodTo)
                                                              .instanceId(instanceId)
                                                              .consentId(consentId)
                                                              .build();

        String sqlRequest = sqlEventReportBuilder
                                .period()
                                .instanceId()
                                .consentId()
                                .build();

        return namedParameterJdbcTemplate.query(sqlRequest, parameters, new BeanPropertyRowMapper<>(EventEntityForReport.class));
    }

    public List<EventEntityForReport> findByTimestampBetweenAndPaymentIdAndInstanceIdOrderByTimestampAsc(OffsetDateTime periodFrom, OffsetDateTime periodTo, String paymentId, String instanceId) {
        EventReportSqlParameterSourceBuilder parameters = new EventReportSqlParameterSourceBuilder()
                                                              .periodFrom(periodFrom)
                                                              .periodTo(periodTo)
                                                              .instanceId(instanceId)
                                                              .paymentId(paymentId)
                                                              .build();

        String sqlRequest = sqlEventReportBuilder
                                .period()
                                .instanceId()
                                .paymentId()
                                .build();

        return namedParameterJdbcTemplate.query(sqlRequest, parameters, new BeanPropertyRowMapper<>(EventEntityForReport.class));
    }

    public List<EventEntityForReport> findByTimestampBetweenAndEventTypeAndInstanceIdOrderByTimestampAsc(OffsetDateTime periodFrom, OffsetDateTime periodTo, EventType eventType, String instanceId) {
        EventReportSqlParameterSourceBuilder parameters = new EventReportSqlParameterSourceBuilder()
                                                              .periodFrom(periodFrom)
                                                              .periodTo(periodTo)
                                                              .instanceId(instanceId)
                                                              .eventType(eventType)
                                                              .build();

        String sqlRequest = sqlEventReportBuilder
                                .period()
                                .instanceId()
                                .eventType()
                                .build();

        return namedParameterJdbcTemplate.query(sqlRequest, parameters, new BeanPropertyRowMapper<>(EventEntityForReport.class));
    }

    public List<EventEntityForReport> findByTimestampBetweenAndEventOriginAndInstanceIdOrderByTimestampAsc(OffsetDateTime periodFrom, OffsetDateTime periodTo, EventOrigin eventOrigin, String instanceId) {
        EventReportSqlParameterSourceBuilder parameters = new EventReportSqlParameterSourceBuilder()
                                                              .periodFrom(periodFrom)
                                                              .periodTo(periodTo)
                                                              .instanceId(instanceId)
                                                              .eventOrigin(eventOrigin)
                                                              .build();

        String sqlRequest = sqlEventReportBuilder
                                .period()
                                .instanceId()
                                .eventOrigin()
                                .build();

        return namedParameterJdbcTemplate.query(sqlRequest, parameters, new BeanPropertyRowMapper<>(EventEntityForReport.class));
    }
}
