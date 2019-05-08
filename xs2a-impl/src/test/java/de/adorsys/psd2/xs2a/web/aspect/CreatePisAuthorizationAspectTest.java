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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.link.CreatePisAuthorisationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisAuthorisationLinks;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreatePisAuthorizationAspectTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String ERROR_TEXT = "Error occurred while processing";

    @InjectMocks
    private CreatePisAuthorizationAspect aspect;

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private MessageService messageService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private Xs2aCreatePisAuthorisationResponse createPisAuthorisationResponse;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCommonPaymentPsuDataResponse;

    private AspspSettings aspspSettings;
    private ResponseObject responseObject;
    private Xs2aCreatePisAuthorisationRequest request;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);

        request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, null, PAYMENT_PRODUCT, PAYMENT_SERVICE, "");
    }

    @Test
    public void createPisAuthorizationAspect_successOnCreatePisAuthorization() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(null);

        responseObject = ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                             .body(createPisAuthorisationResponse)
                             .build();
        ResponseObject actualResponse = aspect.createPisAuthorizationAspect(responseObject, request);

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(createPisAuthorisationResponse, times(1)).setLinks(any(CreatePisAuthorisationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void updatePisAuthorizationAspect_successOnUpdateAuthorization() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(null);

        responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                             .body(updatePisCommonPaymentPsuDataResponse)
                             .build();
        ResponseObject actualResponse = aspect.createPisAuthorizationAspect(responseObject, request);

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(updatePisCommonPaymentPsuDataResponse, times(1)).setLinks(any(UpdatePisAuthorisationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // Given
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        // When
        responseObject = ResponseObject.builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.createPisAuthorizationAspect(responseObject, request);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }
}
