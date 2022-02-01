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
import de.adorsys.psd2.xs2a.web.link.CreatePiisAuthorisationLinks;
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
    @Mock
    private CreateConsentAuthorizationResponse createConsentAuthorizationResponse;

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

    @Test
    void invokeCreatePiisAuthorisationAspect() {
        //Given
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl())
            .thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        ResponseObject<AuthorisationResponse> authorisationResponseResponseObject = ResponseObject.<AuthorisationResponse>builder().body(createConsentAuthorizationResponse).build();
        //When
        ResponseObject<AuthorisationResponse> authorisationResponseResponseObjectWithLinks = service.invokeCreatePiisAuthorisationAspect(authorisationResponseResponseObject);
        //Then
        verify(createConsentAuthorizationResponse, times(1)).setLinks(any(CreatePiisAuthorisationLinks.class));
        assertFalse(authorisationResponseResponseObjectWithLinks.hasError());
    }
}
