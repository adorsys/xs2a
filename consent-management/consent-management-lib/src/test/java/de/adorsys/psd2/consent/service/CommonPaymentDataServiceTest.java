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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonPaymentDataServiceTest {
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private CommonPaymentDataService commonPaymentDataService;

    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    @Test
    void getPisCommonPaymentData() {
        when(pisCommonPaymentDataSpecification.byPaymentIdAndInstanceId("payment_id", "instance_id")).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(pisCommonPaymentDataRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new PisCommonPaymentData()));

        Optional<PisCommonPaymentData> pisCommonPaymentData = commonPaymentDataService.getPisCommonPaymentData("payment_id", "instance_id");
        assertTrue(pisCommonPaymentData.isPresent());
    }

    @Test
    void getPisCommonPaymentData_instanceIdNotPassed() {
        when(pisCommonPaymentDataSpecification.byPaymentId("payment_id")).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(pisCommonPaymentDataRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new PisCommonPaymentData()));

        Optional<PisCommonPaymentData> pisCommonPaymentData = commonPaymentDataService.getPisCommonPaymentData("payment_id", null);
        assertTrue(pisCommonPaymentData.isPresent());
    }

    @Test
    void updateCancelTppRedirectURIs() {
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        paymentData.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updateCancelTppRedirectURIs(paymentData, tppRedirectUri);
        assertEquals("ok_url", paymentData.getAuthorisationTemplate().getCancelRedirectUri());
        assertEquals("nok_url", paymentData.getAuthorisationTemplate().getCancelNokRedirectUri());
    }

    @Test
    void updateStatusInPaymentData_TransactionStatusACSP() {
        TransactionStatus transactionStatus = TransactionStatus.ACSP;
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        assertNull(paymentData.getTransactionStatus());

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updateStatusInPaymentData(paymentData, transactionStatus);
        assertEquals(transactionStatus, paymentData.getTransactionStatus());
    }

    @Test
    void updateInternalStatusInPaymentData() {
        InternalPaymentStatus transactionStatus = InternalPaymentStatus.INITIATED;
        PisCommonPaymentData paymentData = new PisCommonPaymentData();

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updateInternalStatusInPaymentData(paymentData, transactionStatus);
        assertThat(paymentData.getInternalPaymentStatus()).isEqualTo(transactionStatus);
    }

    @Test
    void updatePaymentData() {
        byte[] payment = "payment".getBytes();
        PisCommonPaymentData paymentData = new PisCommonPaymentData();

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updatePaymentData(paymentData, payment);
        assertThat(paymentData.getPayment()).isEqualTo(payment);
    }

    @Test
    void updateStatusInPaymentData_TransactionStatusPATC() {
        TransactionStatus transactionStatus = TransactionStatus.PATC;
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        assertNull(paymentData.getTransactionStatus());
        assertFalse(paymentData.isMultilevelScaRequired());

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updateStatusInPaymentData(paymentData, transactionStatus);
        assertEquals(transactionStatus, paymentData.getTransactionStatus());
        assertTrue(paymentData.isMultilevelScaRequired());
    }

    @Test
    void updateCancelTppRedirectURIs_Fail() {
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        paymentData.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(paymentData);

        assertFalse(commonPaymentDataService.updateCancelTppRedirectURIs(paymentData, tppRedirectUri));
    }

    @Test
    void updatePaymentCancellationInternalRequestId_Success() {
        //Given
        String paymentId = "YK1f8zkXTBIkVeJWOiHzAE";
        OffsetDateTime creationTimestamp = OffsetDateTime.now();
        PisCommonPaymentData paymentData = buildPisCommonPaymentData(paymentId, creationTimestamp);
        PisCommonPaymentData paymentDataWithCancellationInternalRequestId = buildPisCommonPaymentData(paymentId, creationTimestamp);
        paymentDataWithCancellationInternalRequestId.setCancellationInternalRequestId(INTERNAL_REQUEST_ID);
        when(pisCommonPaymentDataRepository.save(paymentDataWithCancellationInternalRequestId)).thenReturn(paymentDataWithCancellationInternalRequestId);
        //When
        boolean updatePaymentCancellationInternalRequestId = commonPaymentDataService.updatePaymentCancellationInternalRequestId(paymentData, INTERNAL_REQUEST_ID);
        //Then
        assertTrue(updatePaymentCancellationInternalRequestId);
    }

    private PisCommonPaymentData buildPisCommonPaymentData(String paymentId, OffsetDateTime creationTimestamp) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setPaymentId(paymentId);
        pisCommonPaymentData.setCreationTimestamp(creationTimestamp);
        return pisCommonPaymentData;
    }
}
