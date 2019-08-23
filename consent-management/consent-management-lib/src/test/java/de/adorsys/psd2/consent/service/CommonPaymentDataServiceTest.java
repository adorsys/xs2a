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
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommonPaymentDataServiceTest {

    @InjectMocks
    private CommonPaymentDataService commonPaymentDataService;

    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    @Test
    public void getPisCommonPaymentData() {
        when(pisCommonPaymentDataSpecification.byPaymentIdAndInstanceId("payment_id", "instance_id")).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(pisCommonPaymentDataRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new PisCommonPaymentData()));

        Optional<PisCommonPaymentData> pisCommonPaymentData = commonPaymentDataService.getPisCommonPaymentData("payment_id", "instance_id");
        assertTrue(pisCommonPaymentData.isPresent());
    }

    @Test
    public void getPisCommonPaymentData_instanceIdNotPassed() {
        when(pisCommonPaymentDataSpecification.byPaymentId("payment_id")).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(pisCommonPaymentDataRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new PisCommonPaymentData()));

        Optional<PisCommonPaymentData> pisCommonPaymentData = commonPaymentDataService.getPisCommonPaymentData("payment_id", null);
        assertTrue(pisCommonPaymentData.isPresent());
    }

    @Test
    public void updateCancelTppRedirectURIs() {
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        paymentData.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updateCancelTppRedirectURIs(paymentData, tppRedirectUri);
        assertEquals("ok_url", paymentData.getAuthorisationTemplate().getCancelRedirectUri());
        assertEquals("nok_url", paymentData.getAuthorisationTemplate().getCancelNokRedirectUri());
    }

    @Test
    public void updateStatusInPaymentData() {
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        assertNull(paymentData.getTransactionStatus());

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(new PisCommonPaymentData());

        commonPaymentDataService.updateStatusInPaymentData(paymentData, TransactionStatus.ACSP);
        assertEquals(TransactionStatus.ACSP, paymentData.getTransactionStatus());
    }

    @Test
    public void updateCancelTppRedirectURIs_Fail() {
        PisCommonPaymentData paymentData = new PisCommonPaymentData();
        paymentData.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");

        when(pisCommonPaymentDataRepository.save(paymentData)).thenReturn(paymentData);

        assertFalse(commonPaymentDataService.updateCancelTppRedirectURIs(paymentData, tppRedirectUri));
    }
}
