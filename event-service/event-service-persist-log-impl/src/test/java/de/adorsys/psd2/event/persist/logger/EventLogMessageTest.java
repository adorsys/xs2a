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

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventLogMessageTest {
    private static final String TIMESTAMP_MESSAGE_FORMAT = "timestamp: [%s]";
    private static final String EVENT_ORIGIN_MESSAGE_FORMAT = "eventOrigin: [%s]";
    private static final String EVENT_TYPE_MESSAGE_FORMAT = "eventType: [%s]";
    private static final String INTERNAL_REQUEST_ID_MESSAGE_FORMAT = "internalRequestId: [%s]";
    private static final String X_REQUEST_ID_MESSAGE_FORMAT = "xRequestId: [%s]";
    private static final String CONSENT_ID_MESSAGE_FORMAT = "consentId: [%s]";
    private static final String PAYMENT_ID_MESSAGE_FORMAT = "paymentId: [%s]";
    private static final String TPP_AUTHORISATION_NUMBER_MESSAGE_FORMAT = "tppAuthorisationNumber: [%s]";
    private static final String PSU_DATA_MESSAGE_FORMAT = "psuData: [psuId: %s, psuIdType: %s, psuCorporateId: %s, psuCorporateIdType: %s]";
    private static final String EMPTY_PSU_DATA_MESSAGE = "psuData: []";
    private static final String PARTIAL_PSU_DATA_MESSAGE_FORMAT = "psuData: [psuId: %s, psuIdType: %s]";
    private static final String PARTIAL_CORPORATE_PSU_DATA_MESSAGE_FORMAT = "psuData: [psuCorporateId: %s, psuCorporateIdType: %s]";
    private static final String PAYLOAD_MESSAGE_FORMAT = "payload: [%s]";

    @Test
    void withTimestamp_shouldAddTimestampFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String timestamp = "2019-07-09T13:29:50+03:00";
        eventPO.setTimestamp(OffsetDateTime.parse(timestamp));
        String expectedMessage = String.format(TIMESTAMP_MESSAGE_FORMAT, timestamp);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withTimestamp()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withEventOrigin_shouldAddEventOriginFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        EventOrigin eventOrigin = EventOrigin.TPP;
        eventPO.setEventOrigin(eventOrigin);
        String expectedMessage = String.format(EVENT_ORIGIN_MESSAGE_FORMAT, eventOrigin.name());

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withEventOrigin()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withEventType_shouldAddEventTypeFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        EventType eventType = EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED;
        eventPO.setEventType(eventType);
        String expectedMessage = String.format(EVENT_TYPE_MESSAGE_FORMAT, eventType.name());

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withEventType()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withInternalRequestId_shouldAddInternalRequestIdFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String internalRequestId = "0cbaf14a-a861-454e-9a11-e21464c69de9";
        eventPO.setInternalRequestId(internalRequestId);
        String expectedMessage = String.format(INTERNAL_REQUEST_ID_MESSAGE_FORMAT, internalRequestId);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withInternalRequestId()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withXRequestId_shouldAddXRequestIdFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String xRequestId = "3437e005-07d6-4a89-b3d3-94a095906941";
        eventPO.setXRequestId(xRequestId);
        String expectedMessage = String.format(X_REQUEST_ID_MESSAGE_FORMAT, xRequestId);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withXRequestId()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withConsentId_shouldAddConsentIdFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String consentId = "consent id";
        eventPO.setConsentId(consentId);
        String expectedMessage = String.format(CONSENT_ID_MESSAGE_FORMAT, consentId);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withConsentId()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withConsentId_withNullId_shouldSkipConsentId() {
        // Given
        EventPO eventPO = new EventPO();

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withConsentId()
                                         .build();

        // Then
        assertEquals("", logMessage.getMessage());
    }

    @Test
    void withPaymentId_shouldAddPaymentIdFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String paymentId = "payment id";
        eventPO.setPaymentId(paymentId);
        String expectedMessage = String.format(PAYMENT_ID_MESSAGE_FORMAT, paymentId);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPaymentId()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withPaymentId_withNullId_shouldSkipPaymentId() {
        // Given
        EventPO eventPO = new EventPO();

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPaymentId()
                                         .build();

        // Then
        assertEquals("", logMessage.getMessage());
    }

    @Test
    void withTppAuthorisationNumber_shouldAddTppAuthorisationNumberFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String tppAuthorisationNumber = "tpp authorisation number";
        eventPO.setTppAuthorisationNumber(tppAuthorisationNumber);
        String expectedMessage = String.format(TPP_AUTHORISATION_NUMBER_MESSAGE_FORMAT, tppAuthorisationNumber);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withTppAuthorisationNumber()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withPsuData_shouldAddPsuDataFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        PsuIdDataPO psuData = new PsuIdDataPO();
        String psuId = "psu id";
        psuData.setPsuId(psuId);
        String psuIdType = "some id type";
        psuData.setPsuIdType(psuIdType);
        String corporateId = "corporate id";
        psuData.setPsuCorporateId(corporateId);
        String corporateIdType = "some corporate id type";
        psuData.setPsuCorporateIdType(corporateIdType);

        eventPO.setPsuIdData(psuData);

        String expectedMessage = String.format(PSU_DATA_MESSAGE_FORMAT,
                                               psuId, psuIdType, corporateId, corporateIdType);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPsuData()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withPsuData_withNullData_shouldSkipPsuData() {
        // Given
        EventPO eventPO = new EventPO();

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPsuData()
                                         .build();

        // Then
        assertEquals("", logMessage.getMessage());
    }

    @Test
    void withPsuData_withAllFieldsNull_shouldAddEmptyPsuData() {
        // Given
        EventPO eventPO = new EventPO();
        PsuIdDataPO psuData = new PsuIdDataPO();
        eventPO.setPsuIdData(psuData);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPsuData()
                                         .build();

        // Then
        assertEquals(EMPTY_PSU_DATA_MESSAGE, logMessage.getMessage());
    }

    @Test
    void withPsuData_withSomeFieldsNull_shouldSkipNulls() {
        // Given
        EventPO eventPO = new EventPO();
        PsuIdDataPO psuData = new PsuIdDataPO();
        String psuId = "psu id";
        psuData.setPsuId(psuId);
        String psuIdType = "some id type";
        psuData.setPsuIdType(psuIdType);
        eventPO.setPsuIdData(psuData);

        String expectedMessage = String.format(PARTIAL_PSU_DATA_MESSAGE_FORMAT, psuId, psuIdType);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPsuData()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withPsuData_withFirstFieldsNull_shouldSkipNulls() {
        // Given
        EventPO eventPO = new EventPO();
        PsuIdDataPO psuData = new PsuIdDataPO();
        String corporateId = "corporate id";
        psuData.setPsuCorporateId(corporateId);
        String corporateIdType = "some corporate id type";
        psuData.setPsuCorporateIdType(corporateIdType);
        eventPO.setPsuIdData(psuData);

        String expectedMessage = String.format(PARTIAL_CORPORATE_PSU_DATA_MESSAGE_FORMAT, corporateId, corporateIdType);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPsuData()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }

    @Test
    void withPayload_shouldAddPayloadFromEvent() {
        // Given
        EventPO eventPO = new EventPO();
        String payload = "some payload";
        eventPO.setPayload(payload.getBytes());

        String expectedMessage = String.format(PAYLOAD_MESSAGE_FORMAT, payload);

        // When
        EventLogMessage logMessage = EventLogMessage.builder(eventPO)
                                         .withPayload()
                                         .build();

        // Then
        assertEquals(expectedMessage, logMessage.getMessage());
    }
}
