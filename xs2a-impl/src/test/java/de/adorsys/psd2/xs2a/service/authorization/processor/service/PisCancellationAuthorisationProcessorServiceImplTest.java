/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCancellationAuthorisationProcessorServiceImplTest {
    private static final String TEST_PAYMENT_ID = "12345676";
    private static final String TEST_AUTHORISATION_ID = "assddsff";
    private static final PsuIdData TEST_PSU_DATA = new PsuIdData("test-user", null, null, null, null);
    private static final ScaApproach TEST_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final ScaStatus TEST_SCA_STATUS = ScaStatus.RECEIVED;

    @InjectMocks
    private PisCancellationAuthorisationProcessorServiceImpl pisCancellationAuthorisationProcessorService;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    @Test
    void doScaExempted_success() {
        // When
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        assertThrows(UnsupportedOperationException.class, () -> pisCancellationAuthorisationProcessorService.doScaExempted(authorisationProcessorRequest));
    }

    @Test
    void doScaStarted_success() {
        // When
        AuthorisationProcessorRequest authorisationProcessorRequest = buildEmptyAuthorisationProcessorRequest();
        assertThrows(UnsupportedOperationException.class, () -> pisCancellationAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest));
    }

    @Test
    void doScaFinalised_success() {
        // Given
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse)).thenReturn(new SpiSinglePayment("sepa-credit-transfers"));

        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        // When
        AuthorisationProcessorResponse actual = pisCancellationAuthorisationProcessorService.doScaFinalised(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat((actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaFailed_success() {
        // When
        AuthorisationProcessorRequest authorisationProcessorRequest = buildEmptyAuthorisationProcessorRequest();
        assertThrows(UnsupportedOperationException.class, () -> pisCancellationAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest));
    }

    private AuthorisationProcessorRequest buildEmptyAuthorisationProcessorRequest() {
        return new PisAuthorisationProcessorRequest(null,
                                                    null,
                                                    null,
                                                    null);
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentId(TEST_PAYMENT_ID);
        request.setAuthorisationId(TEST_AUTHORISATION_ID);
        request.setPsuData(TEST_PSU_DATA);
        return new PisAuthorisationProcessorRequest(TEST_SCA_APPROACH,
                                                    TEST_SCA_STATUS,
                                                    request,
                                                    new Authorisation());
    }
}
