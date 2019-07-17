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
        values.put("eventType", eventType);
        return this;
    }

    public EventReportSqlParameterSourceBuilder eventOrigin(EventOrigin eventOrigin) {
        values.put("eventOrigin", eventOrigin);
        return this;
    }

    private EventReportSqlParameterSourceBuilder(Map<String, ?> values) {
        super(values);
    }

    public EventReportSqlParameterSourceBuilder() {
    }

    EventReportSqlParameterSourceBuilder build() {
        return new EventReportSqlParameterSourceBuilder(values);
    }
}
