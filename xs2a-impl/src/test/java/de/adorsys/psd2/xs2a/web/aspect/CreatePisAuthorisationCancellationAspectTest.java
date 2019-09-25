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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisCancellationPsuDataLinks;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreatePisAuthorisationCancellationAspectTest {

    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null);
    private static final Xs2aCreatePisAuthorisationRequest REQUEST =
        new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, EMPTY_PSU_DATA, PAYMENT_PRODUCT, PaymentType.SINGLE.getValue(), null);

    @InjectMocks
    private CreatePisAuthorisationCancellationAspect aspect;

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private Xs2aCreatePisCancellationAuthorisationResponse createResponse;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse updateResponse;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    private AspspSettings aspspSettings;
    private ResponseObject<CancellationAuthorisationResponse> responseObject;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());
    }

    @Test
    public void createPisAuthorisationAspect_withStartResponseType_shouldSetAuthorisationCancellationLinks() {
        when(createResponse.getAuthorisationResponseType()).thenReturn(AuthorisationResponseType.START);
        responseObject = ResponseObject.<CancellationAuthorisationResponse>builder()
                             .body(createResponse)
                             .build();
        ResponseObject<CancellationAuthorisationResponse> actualResponse =
            aspect.createPisAuthorisationAspect(responseObject, REQUEST);

        verify(createResponse, times(1)).setLinks(any(PisAuthorisationCancellationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void createPisAuthorisationAspect_withUpdateResponseType_shouldSetUpdatePsuDataLinks() {
        when(updateResponse.getAuthorisationResponseType()).thenReturn(AuthorisationResponseType.UPDATE);

        responseObject = ResponseObject.<CancellationAuthorisationResponse>builder()
                             .body(updateResponse)
                             .build();
        ResponseObject<CancellationAuthorisationResponse> actualResponse =
            aspect.createPisAuthorisationAspect(responseObject, REQUEST);

        verify(updateResponse, times(1)).setLinks(any(UpdatePisCancellationPsuDataLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.<CancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject<CancellationAuthorisationResponse> actualResponse =
            aspect.createPisAuthorisationAspect(responseObject, REQUEST);

        // Then
        assertTrue(actualResponse.hasError());
    }

}
