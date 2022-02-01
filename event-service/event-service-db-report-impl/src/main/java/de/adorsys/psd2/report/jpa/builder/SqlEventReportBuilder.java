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
