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

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class EventReportSqlParameterSourceBuilder extends MapSqlParameterSource {
    private final Map<String, Object> values = new HashMap<>();

    private EventReportSqlParameterSourceBuilder(Map<String, ?> values) {
        super(values);
    }

    public EventReportSqlParameterSourceBuilder() {
    }

    public EventReportSqlParameterSourceBuilder periodFrom(OffsetDateTime periodFrom) {
        values.put("periodFrom", periodFrom);
        return this;
    }

    public EventReportSqlParameterSourceBuilder periodTo(OffsetDateTime periodTo) {
        values.put("periodTo", periodTo);
        return this;
    }

    public EventReportSqlParameterSourceBuilder instanceId(String instanceId) {
        values.put("instanceId", instanceId);
        return this;
    }

    public EventReportSqlParameterSourceBuilder consentId(String consentId) {
        values.put("consentId", consentId);
        return this;
    }

    public EventReportSqlParameterSourceBuilder paymentId(String paymentId) {
        values.put("paymentId", paymentId);
        return this;
    }

    public EventReportSqlParameterSourceBuilder eventType(EventType eventType) {
        values.put("eventType", eventType.toString());
        return this;
    }

    public EventReportSqlParameterSourceBuilder eventOrigin(EventOrigin eventOrigin) {
        values.put("eventOrigin", eventOrigin.toString());
        return this;
    }

    public EventReportSqlParameterSourceBuilder build() {
        return new EventReportSqlParameterSourceBuilder(values);
    }
}
