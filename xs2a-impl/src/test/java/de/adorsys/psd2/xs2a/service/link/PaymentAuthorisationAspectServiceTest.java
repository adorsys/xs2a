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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.CreatePisAuthorisationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisAuthorisationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisPsuDataLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAuthorisationAspectServiceTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";

    @InjectMocks
    private PaymentAuthorisationAspectService service;

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aCreatePisAuthorisationResponse createPisAuthorisationResponse;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCommonPaymentPsuDataResponse;
    @Mock
    private RequestProviderService requestProviderService;

    private AspspSettings aspspSettings;
    private ResponseObject responseObject;
    private Xs2aCreatePisAuthorisationRequest request;
    private PaymentAuthorisationParameters updateRequest;

    @BeforeEach
    void setUp() {
        request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, null, PAYMENT_PRODUCT, SINGLE, "");
        updateRequest = new PaymentAuthorisationParameters();
        updateRequest.setPaymentService(SINGLE);
    }

    @Test
    void createPisAuthorizationAspect_successOnCreatePisAuthorization() {
        aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        responseObject = ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                             .body(createPisAuthorisationResponse)
                             .build();
        ResponseObject actualResponse = service.createPisAuthorizationAspect(responseObject, request);

        verify(createPisAuthorisationResponse, times(1)).setLinks(any(CreatePisAuthorisationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void updatePisAuthorizationAspect_successOnUpdateAuthorization() {
        aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                             .body(updatePisCommonPaymentPsuDataResponse)
                             .build();
        ResponseObject actualResponse = service.createPisAuthorizationAspect(responseObject, request);

        verify(updatePisCommonPaymentPsuDataResponse, times(1)).setLinks(any(UpdatePisAuthorisationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = service.createPisAuthorizationAspect(responseObject, request);

        // Then
        assertTrue(actualResponse.hasError());
    }

    @Test
    void updatePisCancellationAuthorizationAspect_success() {
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(updatePisCommonPaymentPsuDataResponse.getScaStatus()).thenReturn(ScaStatus.PSUAUTHENTICATED);

        responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                             .body(updatePisCommonPaymentPsuDataResponse)
                             .build();
        ResponseObject actualResponse = service.updatePisAuthorizationAspect(responseObject, updateRequest);

        verify(updatePisCommonPaymentPsuDataResponse, times(1)).setLinks(any(UpdatePisPsuDataLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void updatePisCancellationAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = service.updatePisAuthorizationAspect(responseObject, updateRequest);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
