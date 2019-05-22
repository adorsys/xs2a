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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.link.PisAuthorisationCancellationLinks;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreatePisAuthorisationCancellationAspectTest {

    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String ERROR_TEXT = "Error occurred while processing";

    @InjectMocks
    private CreatePisAuthorisationCancellationAspect aspect;

    @Mock private ScaApproachResolver scaApproachResolver;
    @Mock private MessageService messageService;
    @Mock private AspspProfileService aspspProfileService;
    @Mock private Xs2aCreatePisCancellationAuthorisationResponse response;

    private AspspSettings aspspSettings;
    private ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> responseObject;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
    }

    @Test
    public void createPisAuthorizationAspect() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .body(response)
                             .build();
        ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse =
            aspect.createPisAuthorizationAspect(responseObject, PAYMENT_ID, null, PaymentType.SINGLE, PAYMENT_PRODUCT);

        verify(aspspProfileService, times(3)).getAspspSettings();
        verify(response, times(1)).setLinks(any(PisAuthorisationCancellationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // Given
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        // When
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse =
            aspect.createPisAuthorizationAspect(responseObject, PAYMENT_ID, null, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

}
