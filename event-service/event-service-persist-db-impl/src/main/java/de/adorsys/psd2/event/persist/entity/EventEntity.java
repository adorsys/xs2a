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

package de.adorsys.psd2.event.persist.entity;

import de.adorsys.psd2.consent.domain.PsuDataEmbeddable;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity(name = "event")
@SuppressWarnings("common-java:DuplicatedBlocks")
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
