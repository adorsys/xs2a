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

package de.adorsys.psd2.xs2a.web.header;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentInitiationHeadersBuilderTest {
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
    void buildInitiatePaymentHeaders_withAuthorisationId_shouldReturnLocationAndScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(LOCATION_HEADER, SELF_LINK);
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());
        NotificationModeResponseHeaders responseHeadersModel = new NotificationModeResponseHeaders(null, null);

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(AUTHORISATION_ID, SELF_LINK, responseHeadersModel);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    void buildInitiatePaymentHeaders_withoutAuthorisationId_shouldReturnLocationOnly() {
        // Given
        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(LOCATION_HEADER, SELF_LINK);
        NotificationModeResponseHeaders responseHeadersModel = new NotificationModeResponseHeaders(null, null);

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(null, SELF_LINK, responseHeadersModel);

        // Then
        verify(scaApproachResolver, never()).getScaApproach(any());

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    void buildStartPaymentAuthorisationHeaders_shouldReturnScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildStartAuthorisationHeaders(AUTHORISATION_ID);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }

    @Test
    void buildUpdatePaymentInitiationPsuDataHeaders_shouldReturnScaApproachFromAuthorisation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(SCA_APPROACH);

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(ASPSP_SCA_APPROACH_HEADER, SCA_APPROACH.name());

        // When
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildUpdatePsuDataHeaders(AUTHORISATION_ID);

        // Then
        verify(scaApproachResolver, never()).resolveScaApproach();

        HttpHeaders actualHttpHeaders = responseHeaders.getHttpHeaders();
        assertEquals(expectedHttpHeaders, actualHttpHeaders);
    }
}
