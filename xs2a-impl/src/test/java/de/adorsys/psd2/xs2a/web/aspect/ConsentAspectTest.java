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
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.link.CreateConsentLinks;
import de.adorsys.psd2.xs2a.web.link.UpdateConsentLinks;
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
public class ConsentAspectTest {
    private static final String CONSENT_ID = "some consent id";
    private static final String ERROR_TEXT = "Error occurred while processing";

    @InjectMocks
    private ConsentAspect aspect;

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private MessageService messageService;
    @Mock
    private CreateConsentResponse createConsentResponse;
    @Mock
    private UpdateConsentPsuDataResponse updateConsentPsuDataResponse;
    @Mock
    private CreateConsentAuthorizationResponse createConsentAuthorisationResponse;

    private AspspSettings aspspSettings;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
    }

    @Test
    public void invokeCreateAccountConsentAspect_success() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(createConsentResponse.isMultilevelScaRequired()).thenReturn(true);
        when(authorisationMethodDecider.isExplicitMethod(true, true)).thenReturn(true);

        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder()
                                                                   .body(createConsentResponse)
                                                                   .build();
        ResponseObject actualResponse = aspect.invokeCreateAccountConsentAspect(responseObject, new CreateConsentReq(), null, true, null);

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(createConsentResponse, times(1)).setLinks(any(CreateConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void invokeCreateAccountConsentAspect_withError_shouldAddTextErrorMessage() {
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder()
                                                                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                   .build();
        ResponseObject actualResponse = aspect.invokeCreateAccountConsentAspect(responseObject, new CreateConsentReq(), null, true, null);

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

    @Test
    public void invokeCreateConsentPsuDataAspect_success() {
        when(updateConsentPsuDataResponse.getScaStatus()).thenReturn(ScaStatus.RECEIVED);
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(updateConsentPsuDataResponse)
                                                                   .build();
        ResponseObject actualResponse = aspect.invokeCreateConsentPsuDataAspect(responseObject, null, CONSENT_ID, "");

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(updateConsentPsuDataResponse, times(1)).setLinks(any(UpdateConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void invokeCreateConsentPsuDataAspect_scaStatusIsNull_success() {
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(updateConsentPsuDataResponse)
                                                                   .build();
        ResponseObject actualResponse = aspect.invokeCreateConsentPsuDataAspect(responseObject, null, CONSENT_ID, "");

        verify(aspspProfileService, never()).getAspspSettings();
        verify(updateConsentPsuDataResponse, times(1)).setLinks(null);

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void invokeCreateConsentPsuDataAspect_wrongResponseType() {
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(createConsentAuthorisationResponse)
                                                                   .build();
        ResponseObject actualResponse = aspect.invokeCreateConsentPsuDataAspect(responseObject, null, CONSENT_ID, "");

        verify(aspspProfileService, never()).getAspspSettings();

        assertFalse(actualResponse.hasError());
        assertEquals(responseObject, actualResponse);
    }

    @Test
    public void invokeCreateConsentPsuDataAspect_withError_shouldAddTextErrorMessage() {
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                   .build();
        ResponseObject actualResponse = aspect.invokeCreateConsentPsuDataAspect(responseObject, null, CONSENT_ID, "");

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

    @Test
    public void invokeUpdateConsentPsuDataAspect_success() {
        when(updateConsentPsuDataResponse.getScaStatus()).thenReturn(ScaStatus.RECEIVED);
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);

        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .body(updateConsentPsuDataResponse)
                                                                          .build();
        ResponseObject actualResponse = aspect.invokeUpdateConsentPsuDataAspect(responseObject, new UpdateConsentPsuDataReq());

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(updateConsentPsuDataResponse, times(1)).setLinks(any(UpdateConsentLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void invokeUpdateConsentPsuDataAspect_scaStatusIsNull_success() {
        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .body(updateConsentPsuDataResponse)
                                                                          .build();
        ResponseObject actualResponse = aspect.invokeUpdateConsentPsuDataAspect(responseObject, new UpdateConsentPsuDataReq());

        verify(aspspProfileService, never()).getAspspSettings();
        verify(updateConsentPsuDataResponse, times(1)).setLinks(null);

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void invokeUpdateConsentPsuDataAspect_withError_shouldAddTextErrorMessage() {
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                                                          .build();
        ResponseObject actualResponse = aspect.invokeUpdateConsentPsuDataAspect(responseObject, new UpdateConsentPsuDataReq());

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

}
