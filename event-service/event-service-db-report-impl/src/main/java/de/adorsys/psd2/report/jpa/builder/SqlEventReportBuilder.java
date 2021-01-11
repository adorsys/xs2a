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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

@Slf4j
@Component
public class SqlEventReportBuilder extends MapSqlParameterSource {
    private static final String PLACEHOLDER = "<schema_name>";

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schemaName;
    @Value("base_event_report_db.sql")
    private String sqlRequestFileName;
    private StringBuilder sqlRequest;
    private StringBuilder filterRequest;

    public String getBasePartOfRequest() throws IOException {
        return IOUtils.toString(Objects.requireNonNull(getClass().getClassLoader().getResource(sqlRequestFileName)).openStream(), Charset.defaultCharset()).replace(PLACEHOLDER, schemaName);
    }

    public SqlEventReportBuilder baseRequest() {
        try {
            sqlRequest = new StringBuilder(getBasePartOfRequest());
        } catch (IOException e) {
            log.error("Request query was not found!");
        }
        filterRequest = new StringBuilder();
        return this;
    }

    public SqlEventReportBuilder period() {
        appendToRequest("timestamp between :periodFrom and :periodTo ");
        return this;
    }

    public SqlEventReportBuilder instanceId() {
        appendToRequest("ev.instance_id = :instanceId ");
        return this;
    }

    public SqlEventReportBuilder consentId() {
        appendToRequest("ev.consent_id = :consentId ");
        return this;
    }

    public SqlEventReportBuilder paymentId() {
        appendToRequest("ev.payment_id = :paymentId ");
        return this;
    }

    public SqlEventReportBuilder eventType() {
        appendToRequest("ev.event_type = :eventType ");
        return this;
    }

    public SqlEventReportBuilder eventOrigin() {
        appendToRequest("ev.event_origin = :eventOrigin ");
        return this;
    }

    public String build() {
        return sqlRequest
                   .append(filterRequest)
                   .append("order by timestamp ")
                   .toString();
    }

    private void appendToRequest(String filter) {
        if (filterRequest.length() == 0) {
            filterRequest.append("where ");
        } else {
            filterRequest.append("and  ");
        }

        filterRequest.append(filter);
    }
}
