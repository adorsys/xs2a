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

package de.adorsys.psd2.logger.context;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MdcLoggingContextServiceTest {
    private static final String CONSENT_STATUS_KEY = "consentStatus";
    private static final String TRANSACTION_STATUS_KEY = "transactionStatus";
    private static final String SCA_STATUS_KEY = "scaStatus";
    private static final String INTERNAL_REQUEST_ID_KEY = "internal-request-id";
    private static final String X_REQUEST_ID_KEY = "x-request-id";
    private static final String X_REQUEST_ID = "0d7f200e-09b4-46f5-85bd-f4ea89fccace";
    private static final String INTERNAL_REQUEST_ID = "9fe83704-6019-46fa-b8aa-53fb8fa667ea";
    private static final String INSTANCE_ID_KEY = "instance-id";
    private static final String INSTANCE_ID = "bank1";

    private MdcLoggingContextService mdcLoggingContextService = new MdcLoggingContextService();

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @Test
    void storeConsentStatus_shouldPutStatusIntoMdc() {
        // Given
        ConsentStatus status = ConsentStatus.RECEIVED;

        // When
        mdcLoggingContextService.storeConsentStatus(status);
        // Then
        assertEquals(status.getValue(), MDC.get(CONSENT_STATUS_KEY));
    }

    @Test
    void getConsentStatus_shouldTakeStatusFromMdc() {
        // Given
        String expectedStatus = ConsentStatus.REJECTED.getValue();
        MDC.put(CONSENT_STATUS_KEY, expectedStatus);

        // When
        String actualStatus = mdcLoggingContextService.getConsentStatus();

        // Then
        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    void storeTransactionStatus_shouldPutStatusIntoMdc() {
        // Given
        TransactionStatus status = TransactionStatus.ACSP;

        // When
        mdcLoggingContextService.storeTransactionStatus(status);

        // Then
        assertEquals(status.getTransactionStatus(), MDC.get(TRANSACTION_STATUS_KEY));
    }

    @Test
    void getTransactionStatus_shouldTakeStatusFromMdc() {
        // Given
        String expectedStatus = TransactionStatus.ACCC.getTransactionStatus();
        MDC.put(TRANSACTION_STATUS_KEY, expectedStatus);

        // When
        String actualStatus = mdcLoggingContextService.getTransactionStatus();

        // Then
        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    void storeTransactionAndScaStatus() {
        // Given
        TransactionStatus transactionStatus = TransactionStatus.ACSP;
        ScaStatus scaStatus = ScaStatus.PSUAUTHENTICATED;

        // When
        mdcLoggingContextService.storeTransactionAndScaStatus(transactionStatus, scaStatus);

        // Then
        assertEquals(transactionStatus.getTransactionStatus(), MDC.get(TRANSACTION_STATUS_KEY));
        assertEquals(scaStatus.getValue(), MDC.get(SCA_STATUS_KEY));
    }

    @Test
    void storeScaStatus_shouldPutStatusIntoMdc() {
        // Given
        ScaStatus status = ScaStatus.PSUAUTHENTICATED;

        // When
        mdcLoggingContextService.storeScaStatus(status);

        // Then
        assertEquals(status.getValue(), MDC.get(SCA_STATUS_KEY));
    }

    @Test
    void getScaStatus_shouldTakeStatusFromMdc() {
        // Given
        String expectedStatus = ScaStatus.PSUAUTHENTICATED.getValue();
        MDC.put(SCA_STATUS_KEY, expectedStatus);

        // When
        String actualStatus = mdcLoggingContextService.getScaStatus();

        // Then
        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    void storeRequestInformation_shouldPutRequestIdsIntoMdc() {
        // When
        mdcLoggingContextService.storeRequestInformation(new RequestInfo(INTERNAL_REQUEST_ID, X_REQUEST_ID, INSTANCE_ID));

        // Then
        assertEquals(INTERNAL_REQUEST_ID, MDC.get(INTERNAL_REQUEST_ID_KEY));
        assertEquals(X_REQUEST_ID, MDC.get(X_REQUEST_ID_KEY));
        assertEquals(INSTANCE_ID, MDC.get(INSTANCE_ID_KEY));
    }

    @Test
    void storeRequestInformation_nullValues_shouldOverwriteMdcValues() {
        // Given
        MDC.put(INTERNAL_REQUEST_ID_KEY, INTERNAL_REQUEST_ID);
        MDC.put(X_REQUEST_ID_KEY, X_REQUEST_ID);
        MDC.put(INSTANCE_ID_KEY, INSTANCE_ID);

        // When
        mdcLoggingContextService.storeRequestInformation(new RequestInfo(null, null, null));

        // Then
        assertNull(MDC.get(INTERNAL_REQUEST_ID_KEY));
        assertNull(MDC.get(X_REQUEST_ID_KEY));
        assertNull(MDC.get(INSTANCE_ID_KEY));
    }

    @Test
    void storeRequestInformation_null_shouldPreserveMdcValues() {
        // Given
        MDC.put(INTERNAL_REQUEST_ID_KEY, INTERNAL_REQUEST_ID);
        MDC.put(X_REQUEST_ID_KEY, X_REQUEST_ID);
        MDC.put(INSTANCE_ID_KEY, INSTANCE_ID);

        // When
        mdcLoggingContextService.storeRequestInformation(null);

        // Then
        assertEquals(INTERNAL_REQUEST_ID, MDC.get(INTERNAL_REQUEST_ID_KEY));
        assertEquals(X_REQUEST_ID, MDC.get(X_REQUEST_ID_KEY));
        assertEquals(INSTANCE_ID, MDC.get(INSTANCE_ID_KEY));
    }

    @Test
    void getRequestInformation_shouldTakeValuesFromMdc() {
        // Given
        MDC.put(INTERNAL_REQUEST_ID_KEY, INTERNAL_REQUEST_ID);
        MDC.put(X_REQUEST_ID_KEY, X_REQUEST_ID);
        MDC.put(INSTANCE_ID_KEY, INSTANCE_ID);
        RequestInfo expectedRequestInfo = new RequestInfo(INTERNAL_REQUEST_ID, X_REQUEST_ID, INSTANCE_ID);

        // When
        RequestInfo actualRequestInfo = mdcLoggingContextService.getRequestInformation();

        // Then
        assertEquals(expectedRequestInfo, actualRequestInfo);
    }

    @Test
    void getRequestInformation_nullValues_shouldReturnNullValues() {
        // Given
        RequestInfo expectedRequestInfo = new RequestInfo(null, null, null);

        // When
        RequestInfo actualRequestInfo = mdcLoggingContextService.getRequestInformation();

        // Then
        assertEquals(expectedRequestInfo, actualRequestInfo);
    }

    @Test
    void clearContext_shouldClearMdc() {
        // Given
        MDC.put(CONSENT_STATUS_KEY, "some consent status");
        MDC.put(TRANSACTION_STATUS_KEY, "some transaction status");
        MDC.put(SCA_STATUS_KEY, "some sca status");
        MDC.put(INSTANCE_ID_KEY, "some instance id");

        // When
        mdcLoggingContextService.clearContext();

        // Then
        assertNull(MDC.get(CONSENT_STATUS_KEY));
        assertNull(MDC.get(TRANSACTION_STATUS_KEY));
        assertNull(MDC.get(SCA_STATUS_KEY));
        assertNull(MDC.get(INSTANCE_ID_KEY));
    }
}
