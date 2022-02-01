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

package de.adorsys.psd2.event.persist.logger;

import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventLogMessage {
    private final String message;

    /**
     * Creates new {@link EventLogMessage} builder from given event
     *
     * @param eventPO event to be logged
     * @return new builder
     */
    public static EventLogMessageBuilder builder(EventPO eventPO) {
        return new EventLogMessageBuilder(eventPO);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EventLogMessageBuilder {
        private static final String TIMESTAMP = "timestamp";
        private static final String EVENT_ORIGIN = "eventOrigin";
        private static final String EVENT_TYPE = "eventType";
        private static final String INTERNAL_REQUEST_ID = "internalRequestId";
        private static final String X_REQUEST_ID = "xRequestId";
        private static final String CONSENT_ID = "consentId";
        private static final String PAYMENT_ID = "paymentId";
        private static final String TPP_AUTHORISATION_NUMBER = "tppAuthorisationNumber";
        private static final String PSU_ID = "psuId";
        private static final String PSU_ID_TYPE = "psuIdType";
        private static final String PSU_CORPORATE_ID = "psuCorporateId";
        private static final String PSU_CORPORATE_ID_TYPE = "psuCorporateIdType";
        private static final String PSU_PROPERTY_PREFIX = ": ";
        private static final String PSU_DATA_PARTS_SEPARATOR = ", ";
        private static final String PSU_DATA = "psuData";
        private static final String PAYLOAD = "payload";

        private final EventPO event;
        private Map<String, String> logParams = new LinkedHashMap<>();

        /**
         * Adds event timestamp to the log message
         *
         * @return this builder
         */
        public EventLogMessageBuilder withTimestamp() {
            logParams.put(TIMESTAMP, event.getTimestamp().toString());
            return this;
        }

        /**
         * Adds event origin (see {@link de.adorsys.psd2.event.core.model.EventOrigin} to the log message
         *
         * @return this builder
         */
        public EventLogMessageBuilder withEventOrigin() {
            logParams.put(EVENT_ORIGIN, event.getEventOrigin().toString());
            return this;
        }

        /**
         * Adds event type (see {@link de.adorsys.psd2.event.core.model.EventType} to the log message
         *
         * @return this builder
         */
        public EventLogMessageBuilder withEventType() {
            logParams.put(EVENT_TYPE, event.getEventType().toString());
            return this;
        }

        /**
         * Adds internal request id from the event to the log message
         *
         * @return this builder
         */
        public EventLogMessageBuilder withInternalRequestId() {
            logParams.put(INTERNAL_REQUEST_ID, event.getInternalRequestId());
            return this;
        }

        /**
         * Adds x-request-id from the event to the log message
         *
         * @return this builder
         */
        public EventLogMessageBuilder withXRequestId() {
            logParams.put(X_REQUEST_ID, event.getXRequestId());
            return this;
        }

        /**
         * Adds consent ID from the event to the log message
         * <p>
         * Nothing will be added to the message if consent ID is not present in the event
         *
         * @return this builder
         */
        public EventLogMessageBuilder withConsentId() {
            putOptionalParameter(CONSENT_ID, event.getConsentId());
            return this;
        }

        /**
         * Adds payment ID from the event to the log message
         * <p>
         * Nothing will be added to the message if payment ID is not present in the event
         *
         * @return this builder
         */
        public EventLogMessageBuilder withPaymentId() {
            putOptionalParameter(PAYMENT_ID, event.getPaymentId());
            return this;
        }

        /**
         * Adds TPP authorisation number from event to the log message
         *
         * @return this builder
         */
        public EventLogMessageBuilder withTppAuthorisationNumber() {
            logParams.put(TPP_AUTHORISATION_NUMBER, event.getTppAuthorisationNumber());
            return this;
        }

        /**
         * Adds PSU Data from event to the log message
         * <p>
         * Only non-null properties of the PSU Data will be added to the message.
         * Nothing will be added if PSU Data is not present in the event.
         *
         * @return this builder
         */
        public EventLogMessageBuilder withPsuData() {
            PsuIdDataPO psuIdDataPO = event.getPsuIdData();

            if (psuIdDataPO != null) {
                List<String> messageParts = new ArrayList<>();
                addOptionalValueWithPrefix(messageParts, psuIdDataPO.getPsuId(), PSU_ID + PSU_PROPERTY_PREFIX);
                addOptionalValueWithPrefix(messageParts, psuIdDataPO.getPsuIdType(), PSU_ID_TYPE + PSU_PROPERTY_PREFIX);
                addOptionalValueWithPrefix(messageParts, psuIdDataPO.getPsuCorporateId(), PSU_CORPORATE_ID + PSU_PROPERTY_PREFIX);
                addOptionalValueWithPrefix(messageParts, psuIdDataPO.getPsuCorporateIdType(), PSU_CORPORATE_ID_TYPE + PSU_PROPERTY_PREFIX);

                logParams.put(PSU_DATA, String.join(PSU_DATA_PARTS_SEPARATOR, messageParts));
            }

            return this;
        }

        /**
         * Adds event payload to the log message
         * <p>
         * Payload is extracted from event as UTF-8 string
         *
         * @return this builder
         */
        public EventLogMessageBuilder withPayload() {
            byte[] payloadBytes = event.getPayload();

            if (payloadBytes != null) {
                String payload = new String(event.getPayload(), StandardCharsets.UTF_8);
                logParams.put(PAYLOAD, payload);
            }

            return this;
        }

        /**
         * Constructs new instance of {@link EventLogMessage} with a message from this builder
         *
         * @return new instance of {@link EventLogMessage}
         */
        public EventLogMessage build() {
            String logMessage = logParams.entrySet()
                                    .stream()
                                    .map(e -> e.getKey() + ": [" + e.getValue() + "]")
                                    .collect(Collectors.joining(", "));

            return new EventLogMessage(logMessage);
        }

        private void putOptionalParameter(String name, @Nullable String value) {
            if (value != null) {
                logParams.put(name, value);
            }
        }

        private void addOptionalValueWithPrefix(Collection<String> collection, @Nullable String value, String prefix) {
            if (value == null) {
                return;
            }

            collection.add(prefix + value);
        }
    }
}
