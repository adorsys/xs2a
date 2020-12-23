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

package de.adorsys.psd2.event.persist.entity;

import de.adorsys.psd2.consent.domain.PsuDataEmbeddable;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity(name = "event")
public class EventEntity {
    static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_generator")
    @SequenceGenerator(name = "event_generator", sequenceName = "event_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "payment_id")
    private String paymentId;

    @Lob
    @Column(name = "payload")
    private byte[] payload;

    @Column(name = "event_origin", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private EventOrigin eventOrigin;

    @Column(name = "event_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;

    @Embedded
    private PsuDataEmbeddable psuData;

    @Column(name = "tpp_authorisation_number")
    private String tppAuthorisationNumber;

    @Column(name = "x_request_id")
    private String xRequestId;

    @Column(name = "instance_id", nullable = false, updatable = false)
    private String instanceId = DEFAULT_SERVICE_INSTANCE_ID;

    @Column(name = "internal_request_id")
    private String internalRequestId;
}
