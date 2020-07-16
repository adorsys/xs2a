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
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.CreateConsentLinks;
import de.adorsys.psd2.xs2a.web.link.CreatePiisConsentLinks;
import de.adorsys.psd2.xs2a.web.link.UpdateConsentLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentAspectServiceTest {

    @InjectMocks
    private ConsentAspectService service;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private CreateConsentResponse createConsentResponse;
    @Mock
    private UpdateConsentPsuDataResponse updateConsentPsuDataResponse;
    @Mock
    private CreateConsentAuthorizationResponse createConsentAuthorisationResponse;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse;

    @Test
    void invokeCreateAccountConsentAspect_success() {
        // Given
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());
        when(createConsentResponse.isMultilevelScaRequired())
            .thenReturn(true);
        when(authorisationMethodDecider.isExplicitMethod(true, true))
            .thenReturn(true);

        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder()
                                                                   .body(createConsentResponse)
                                                                   .build();
        // When
        ResponseObject actualResponse = service.invokeCreateAccountConsentAspect(responseObject,true);

        // Then
        verify(createConsentResponse, times(1)).setLinks(any(CreateConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void invokeCreateAccountConsentAspect_withError_shouldAddTextErrorMessage() {
        // Given
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder()
                                                                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                   .build();
        // When
        ResponseObject actualResponse = service.invokeCreateAccountConsentAspect(responseObject, true);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CONSENT_UNKNOWN_400, responseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void invokeCreateConsentPsuDataAspect_success() {
        // Given
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(updateConsentPsuDataResponse.getScaStatus()).thenReturn(ScaStatus.RECEIVED);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(updateConsentPsuDataResponse)
                                                                   .build();
        // When
        ResponseObject actualResponse = service.invokeCreateConsentPsuDataAspect(responseObject);

        // Then
        verify(updateConsentPsuDataResponse, times(1)).setLinks(any(UpdateConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void invokeCreateConsentPsuDataAspect_scaStatusIsNull_success() {
        // Given
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(updateConsentPsuDataResponse)
                                                                   .build();
        // When
        ResponseObject actualResponse = service.invokeCreateConsentPsuDataAspect(responseObject);

        // Then
        verify(updateConsentPsuDataResponse, times(1)).setLinks(null);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void invokeCreateConsentPsuDataAspect_wrongResponseType() {
        // Given
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(createConsentAuthorisationResponse)
                                                                   .build();
        // When
        ResponseObject actualResponse = service.invokeCreateConsentPsuDataAspect(responseObject);

        // Then
        assertFalse(actualResponse.hasError());
        assertEquals(responseObject, actualResponse);
    }

    @Test
    void invokeCreateConsentPsuDataAspect_withError_shouldAddTextErrorMessage() {
        // Given
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                   .build();
        // When
        ResponseObject actualResponse = service.invokeCreateConsentPsuDataAspect(responseObject);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CONSENT_UNKNOWN_400, responseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void invokeUpdateConsentPsuDataAspect_success() {
        // Given
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(updateConsentPsuDataResponse.getScaStatus()).thenReturn(ScaStatus.RECEIVED);

        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .body(updateConsentPsuDataResponse)
                                                                          .build();
        // When
        ResponseObject actualResponse = service.invokeUpdateConsentPsuDataAspect(responseObject);

        // Then
        verify(updateConsentPsuDataResponse, times(1)).setLinks(any(UpdateConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void invokeUpdateConsentPsuDataAspect_scaStatusIsNull_success() {
        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .body(updateConsentPsuDataResponse)
                                                                          .build();
        ResponseObject actualResponse = service.invokeUpdateConsentPsuDataAspect(responseObject);

        verify(updateConsentPsuDataResponse, times(1)).setLinks(null);

        assertFalse(actualResponse.hasError());
    }

    @Test
    void invokeUpdateConsentPsuDataAspect_withError_shouldAddTextErrorMessage() {
        // Given
        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                          .build();
        // When
        ResponseObject actualResponse = service.invokeUpdateConsentPsuDataAspect(responseObject);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CONSENT_UNKNOWN_400, responseObject.getError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void createPiisConsentWithResponse_success() {
        // Given
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        ResponseObject<Xs2aConfirmationOfFundsResponse> responseObject = ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                                                                   .body(xs2aConfirmationOfFundsResponse)
                                                                   .build();
        // When
        ResponseObject actualResponse = service.createPiisConsentWithResponse(responseObject,true);

        // Then
        verify(xs2aConfirmationOfFundsResponse, times(1)).setLinks(any(CreatePiisConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }
}
