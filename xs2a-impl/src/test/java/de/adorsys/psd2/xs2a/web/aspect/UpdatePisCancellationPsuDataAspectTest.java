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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.link.UpdatePisCancellationPsuDataLinks;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePisCancellationPsuDataAspectTest {

    @InjectMocks
    private UpdatePisCancellationPsuDataAspect aspect;

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCommonPaymentPsuDataResponse;

    private AspspSettings aspspSettings;
    private ResponseObject responseObject;
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentService(SINGLE);
    }

    @Test
    public void updatePisCancellationAuthorizationAspect_success() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(updatePisCommonPaymentPsuDataResponse.getScaStatus()).thenReturn(ScaStatus.PSUAUTHENTICATED);

        responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                             .body(updatePisCommonPaymentPsuDataResponse)
                             .build();
        ResponseObject actualResponse = aspect.updatePisCancellationAuthorizationAspect(responseObject, request);

        verify(aspspProfileService, times(1)).getAspspSettings();
        verify(updatePisCommonPaymentPsuDataResponse, times(1)).setLinks(any(UpdatePisCancellationPsuDataLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void updatePisCancellationAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.updatePisCancellationAuthorizationAspect(responseObject, request);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
