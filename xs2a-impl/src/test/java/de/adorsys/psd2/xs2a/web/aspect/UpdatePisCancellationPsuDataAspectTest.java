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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
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
class UpdatePisCancellationPsuDataAspectTest {

    @InjectMocks
    private UpdatePisCancellationPsuDataAspect aspect;

    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCommonPaymentPsuDataResponse;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject;
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;

    @BeforeEach
    void setUp() {
        request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentService(SINGLE);
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
        ResponseObject actualResponse = aspect.updatePisCancellationAuthorizationAspect(responseObject, request);
        verify(updatePisCommonPaymentPsuDataResponse, times(1)).setLinks(any(UpdatePisCancellationPsuDataLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void updatePisCancellationAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.updatePisCancellationAuthorizationAspect(responseObject, request);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
