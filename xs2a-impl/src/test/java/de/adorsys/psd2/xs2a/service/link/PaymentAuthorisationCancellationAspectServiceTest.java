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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisCancellationPsuDataLinks;
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
class PaymentAuthorisationCancellationAspectServiceTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final Xs2aCreatePisAuthorisationRequest REQUEST =
        new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, EMPTY_PSU_DATA, PAYMENT_PRODUCT, SINGLE, null);

    @InjectMocks
    private PaymentAuthorisationCancellationAspectService service;

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private Xs2aCreatePisCancellationAuthorisationResponse createResponse;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse updateResponse;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private RequestProviderService requestProviderService;

    private ResponseObject<CancellationAuthorisationResponse> responseObject;
    private PaymentAuthorisationParameters updateRequest;

    @BeforeEach
    void setUp() {
        updateRequest = new PaymentAuthorisationParameters();
        updateRequest.setPaymentService(SINGLE);
    }

    @Test
    void createPisAuthorisationAspect_withStartResponseType_shouldSetAuthorisationCancellationLinks() {
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(createResponse.getAuthorisationResponseType()).thenReturn(AuthorisationResponseType.START);
        responseObject = ResponseObject.<CancellationAuthorisationResponse>builder()
                             .body(createResponse)
                             .build();
        ResponseObject<CancellationAuthorisationResponse> actualResponse =
            service.createPisAuthorisationAspect(responseObject, REQUEST);

        verify(createResponse, times(1)).setLinks(any(PisAuthorisationCancellationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void createPisAuthorisationAspect_withUpdateResponseType_shouldSetUpdatePsuDataLinks() {
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(updateResponse.getAuthorisationResponseType()).thenReturn(AuthorisationResponseType.UPDATE);

        responseObject = ResponseObject.<CancellationAuthorisationResponse>builder()
                             .body(updateResponse)
                             .build();
        ResponseObject<CancellationAuthorisationResponse> actualResponse =
            service.createPisAuthorisationAspect(responseObject, REQUEST);

        verify(updateResponse, times(1)).setLinks(any(UpdatePisCancellationPsuDataLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.<CancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject<CancellationAuthorisationResponse> actualResponse =
            service.createPisAuthorisationAspect(responseObject, REQUEST);

        // Then
        assertTrue(actualResponse.hasError());
    }

    @Test
    void updatePisCancellationAuthorizationAspect_success() {
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(updateResponse.getScaStatus()).thenReturn(ScaStatus.PSUAUTHENTICATED);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .body(updateResponse)
                                                                                       .build();
        ResponseObject actualResponse = service.updatePisCancellationAuthorizationAspect(responseObject, updateRequest);
        verify(updateResponse, times(1)).setLinks(any(UpdatePisCancellationPsuDataLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void updatePisCancellationAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                                       .build();
        ResponseObject actualResponse = service.updatePisCancellationAuthorizationAspect(responseObject, updateRequest);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
