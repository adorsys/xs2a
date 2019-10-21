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

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.PisCommonPaymentRemoteUrls;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentServiceRemoteTest {

    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String URL = "http://base.url";
    private static final String UPDATE_MULTILEVEL_SCA_URL = "http://base.url/pis/common-payments/{payment-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
    private static final String PAYMENT_ID = "paymentId";

    @InjectMocks
    private PisCommonPaymentServiceRemote service;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private PisCommonPaymentRemoteUrls remotePisCommonPaymentUrls;

    @Test
    public void getAuthorisationScaApproach_created() {
        when(remotePisCommonPaymentUrls.getAuthorisationScaApproach()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        service.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        verify(remotePisCommonPaymentUrls).getAuthorisationScaApproach();
        verify(consentRestTemplate).getForEntity(eq(URL), eq(AuthorisationScaApproachResponse.class), eq(AUTHORISATION_ID));
    }

    @Test
    public void getAuthorisationScaApproach_cancelled() {
        when(remotePisCommonPaymentUrls.getCancellationAuthorisationScaApproach()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        service.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED);

        verify(remotePisCommonPaymentUrls).getCancellationAuthorisationScaApproach();
        verify(consentRestTemplate).getForEntity(eq(URL), eq(AuthorisationScaApproachResponse.class), eq(AUTHORISATION_ID));
    }

    @Test
    public void getPisAuthorisationById() {
        when(remotePisCommonPaymentUrls.getPisAuthorisationById()).thenReturn(URL);
        GetPisAuthorisationResponse expected = new GetPisAuthorisationResponse();

        when(consentRestTemplate.exchange(URL, HttpMethod.GET, null, GetPisAuthorisationResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(expected));

        Optional<GetPisAuthorisationResponse> actual = service.getPisAuthorisationById(AUTHORISATION_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());

        verify(remotePisCommonPaymentUrls).getPisAuthorisationById();
    }

    @Test
    public void getPisAuthorisationById_withNoBody_shouldReturnEmpty() {
        when(remotePisCommonPaymentUrls.getPisAuthorisationById()).thenReturn(URL);

        when(consentRestTemplate.exchange(URL, HttpMethod.GET, null, GetPisAuthorisationResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok().build());

        Optional<GetPisAuthorisationResponse> actual = service.getPisAuthorisationById(AUTHORISATION_ID);

        assertFalse(actual.isPresent());
    }

    @Test
    public void getPisAuthorisationById_withCmsRestException_shouldReturnEmpty() {
        when(remotePisCommonPaymentUrls.getPisAuthorisationById()).thenReturn(URL);

        when(consentRestTemplate.exchange(URL, HttpMethod.GET, null, GetPisAuthorisationResponse.class, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        Optional<GetPisAuthorisationResponse> actual = service.getPisAuthorisationById(AUTHORISATION_ID);

        assertFalse(actual.isPresent());
    }

    @Test
    public void getPisCancellationAuthorisationById() {
        when(remotePisCommonPaymentUrls.getPisCancellationAuthorisationById()).thenReturn(URL);
        GetPisAuthorisationResponse expected = new GetPisAuthorisationResponse();

        when(consentRestTemplate.exchange(URL, HttpMethod.GET, null, GetPisAuthorisationResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(expected));

        Optional<GetPisAuthorisationResponse> actual = service.getPisCancellationAuthorisationById(AUTHORISATION_ID);

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());

        verify(remotePisCommonPaymentUrls).getPisCancellationAuthorisationById();
    }

    @Test
    public void getPisCancellationAuthorisationById_withNoBody_shouldReturnEmpty() {
        when(remotePisCommonPaymentUrls.getPisCancellationAuthorisationById()).thenReturn(URL);

        when(consentRestTemplate.exchange(URL, HttpMethod.GET, null, GetPisAuthorisationResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok().build());

        Optional<GetPisAuthorisationResponse> actual = service.getPisCancellationAuthorisationById(AUTHORISATION_ID);

        assertFalse(actual.isPresent());
    }

    @Test
    public void getPisCancellationAuthorisationById_withCmsRestException_shouldReturnEmpty() {
        when(remotePisCommonPaymentUrls.getPisCancellationAuthorisationById()).thenReturn(URL);

        when(consentRestTemplate.exchange(URL, HttpMethod.GET, null, GetPisAuthorisationResponse.class, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        Optional<GetPisAuthorisationResponse> actual = service.getPisCancellationAuthorisationById(AUTHORISATION_ID);

        assertFalse(actual.isPresent());
    }

    @Test
    public void updateMultilevelSca() {
        when(remotePisCommonPaymentUrls.updateMultilevelScaRequired()).thenReturn(UPDATE_MULTILEVEL_SCA_URL);
        when(consentRestTemplate.exchange(UPDATE_MULTILEVEL_SCA_URL, HttpMethod.PUT, null, Boolean.class, PAYMENT_ID, true)).thenReturn(ResponseEntity.ok(true));

        boolean actualResponse = service.updateMultilevelSca(PAYMENT_ID, true);

        assertTrue(actualResponse);
    }
}
