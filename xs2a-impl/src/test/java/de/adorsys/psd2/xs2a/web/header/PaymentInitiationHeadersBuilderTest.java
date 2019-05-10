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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInitiationHeadersBuilderTest {
    private static final String AUTHORISATION_ID = "123";
    private static final String SELF_LINK = "http://v1/payments/some-payment-product/some-payment-id";
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final String ASPSP_SCA_APPROACH_HEADER = "Aspsp-Sca-Approach";
    private static final String LOCATION_HEADER = "Location";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @InjectMocks
    private PaymentInitiationHeadersBuilder paymentInitiationHeadersBuilder;

    @Test
    public void buildInitiatePaymentHeaders_withAuthorisationId_shouldReturnLocationAndScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(LOCATION_HEADER, SELF_LINK);
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(AUTHORISATION_ID, SELF_LINK);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    public void buildInitiatePaymentHeaders_withoutAuthorisationId_shouldReturnLocationOnly() {
        // Given
        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(LOCATION_HEADER, SELF_LINK);

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(null, SELF_LINK);

        // Then
        verify(scaApproachResolver, never()).getInitiationScaApproach(any());

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    public void buildErrorInitiatePaymentHeaders_shouldReturnResolvedScaApproach() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildErrorInitiatePaymentHeaders();

        // Then
        verify(scaApproachResolver, never()).getInitiationScaApproach(any());

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    public void buildStartPaymentAuthorisationHeaders_shouldReturnScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildStartPaymentAuthorisationHeaders(AUTHORISATION_ID);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    public void buildErrorStartPaymentAuthorisationHeaders_shouldReturnResolvedScaApproach() {
        // Given
        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildErrorStartPaymentAuthorisationHeaders();

        // Then
        verify(scaApproachResolver, never()).getInitiationScaApproach(any());

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    public void buildUpdatePaymentInitiationPsuDataHeaders_shouldReturnScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildUpdatePaymentInitiationPsuDataHeaders(AUTHORISATION_ID);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    public void buildErrorUpdatePaymentInitiationPsuDataHeaders_shouldReturnScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildErrorUpdatePaymentInitiationPsuDataHeaders(AUTHORISATION_ID);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }
}
