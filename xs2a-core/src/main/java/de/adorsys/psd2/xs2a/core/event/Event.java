/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.event;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Contains information about the event.
 */
@Data
public class Event {
    /**
     * Date and time indicating when the event has occurred.
     */
    private OffsetDateTime timestamp;

    /**
     * Id of the consent that can be associated with this event.
     * Can be null if the event isn't connected with the specific consent.
     */
    private String consentId;

    /**
     * Id of the payment that can be associated with this event.
     * Can be null if the event isn't connected with the specific payment.
     */
    private String paymentId;

    /**
     * Object that may contain additional information about the event.
     * Can be null if the event doesn't provide any additional information.
     */
    private Object payload;

    /**
     * Indicates the origin of the event.
     */
    private EventOrigin eventOrigin;

    /**
     * Indicates what happened in this event.
     */
    private EventType eventType;

    /**
     * The id of particular bank instance.
     */
    private String instanceId;
}
