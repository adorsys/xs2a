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
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentServiceInternalTest {

    @InjectMocks
    private PisCommonPaymentServiceInternal pisCommonPaymentService;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;

    private PisCommonPaymentData pisCommonPaymentData;
    private List<PisAuthorization> pisAuthorizationList = new ArrayList<>();

    private final long PIS_PAYMENT_DATA_ID = 1;
    private static final String EXTERNAL_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private final static PsuData PSU_DATA = new PsuData("id", "type", "corporate ID", "corporate type");
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @BeforeEach
    void setUp() {
        pisCommonPaymentData = buildPisCommonPaymentData();
        pisAuthorizationList.add(buildPisAuthorisation(EXTERNAL_ID, PaymentAuthorisationType.CANCELLED));
        pisAuthorizationList.add(buildPisAuthorisation(AUTHORISATION_ID, PaymentAuthorisationType.CREATED));
    }

    @Test
    void updateMultilevelSca_ShouldReturnTrue() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentDataRepository.save(pisCommonPaymentData)).thenReturn(pisCommonPaymentData);

        // When
        boolean actualResponse = pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse);
    }

    @Test
    void updateMultilevelSca_ShouldReturnFalse() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        boolean actualResponse = pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertFalse(actualResponse);
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        return buildPisCommonPaymentData(new AuthorisationTemplateEntity());
    }

    private PisCommonPaymentData buildPisCommonPaymentData(AuthorisationTemplateEntity authorisationTemplateEntity) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setId(PIS_PAYMENT_DATA_ID);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setTransactionStatus(RCVD);
        pisCommonPaymentData.setAuthorizations(pisAuthorizationList);
        pisCommonPaymentData.setAuthorisationTemplate(authorisationTemplateEntity);
        pisCommonPaymentData.setInternalRequestId(INTERNAL_REQUEST_ID);
        return pisCommonPaymentData;
    }

    private PisAuthorization buildPisAuthorisation(String externalId, PaymentAuthorisationType authorisationType) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(externalId);
        pisAuthorization.setAuthorizationType(authorisationType);
        pisAuthorization.setScaStatus(SCA_STATUS);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        pisAuthorization.setPsuData(PSU_DATA);
        return pisAuthorization;
    }
}
