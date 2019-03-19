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
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentAspectTest {
    private static final String CONSENT_ID = "some consent id";
    private static final String DEFAULT_BASE_URL = "http://localhost";
    private static final String FORCED_BASE_URL = "http://base.url";
    private static final String AUTHORISATION_ID = "some authorisation id";
    private static final String REDIRECT_LINK = "http://redirect.link";

    private static final PsuIdData PSU_DATA = new PsuIdData("psu id", null,
                                                            null, null);
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null,
                                                                  null, null);

    @InjectMocks
    private ConsentAspect consentAspect;

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    @Before
    public void setUp() {
        // Sets mock servlet request to be used for link generation when base URL is not forced
        RequestContextHolder.setRequestAttributes(new ServletWebRequest(buildMockHttpServletRequest()));

        when(redirectLinkBuilder.buildConsentScaRedirectLink(anyString(), anyString())).thenReturn(REDIRECT_LINK);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inRedirectImplicitMode_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), null, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksRedirectImplicit(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inRedirectImplicitMode_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), null, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksRedirectImplicit(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inRedirectExplicitMode_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(true);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), null, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksRedirectExplicit(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inRedirectExplicitMode_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(true);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), null, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksRedirectExplicit(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inEmbeddedImplicitMode_withPsuDataInRequest_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuAuthentication(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inEmbeddedImplicitMode_withPsuDataInRequest_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuAuthentication(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inEmbeddedImplicitMode_withoutPsuDataInRequest_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuIdentification(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inEmbeddedImplicitMode_withoutPsuDataInRequest_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuIdentification(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inEmbeddedExplicitMode_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(true);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledExplicit(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inEmbeddedExplicitMode_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(true);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledExplicit(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inDecoupledImplicitMode_withPsuDataInRequest_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuAuthentication(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inDecoupledImplicitMode_withPsuDataInRequest_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuAuthentication(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inDecoupledImplicitMode_withoutPsuDataInRequest_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuIdentification(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inDecoupledImplicitMode_withoutPsuDataInRequest_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(false);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuIdentification(enriched.getBody(), FORCED_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inDecoupledExplicitMode_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(false));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(true);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledExplicit(enriched.getBody(), DEFAULT_BASE_URL);
    }

    @Test
    public void invokeCreateAccountConsentAspect_inDecoupledExplicitMode_withForcedUrl_shouldAddCorrectLinks() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(true));
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);
        when(authorisationMethodDecider.isExplicitMethod(anyBoolean(), anyBoolean())).thenReturn(true);

        CreateConsentResponse response = buildCreateConsentResponse();
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder().body(response).build();

        // When
        ResponseObject<CreateConsentResponse> enriched = consentAspect.invokeCreateAccountConsentAspect(responseObject, buildCreateConsentReq(), EMPTY_PSU_DATA, false, null);

        // Then
        assertFalse(enriched.hasError());
        assertCreateAccountConsentLinksEmbeddedDecoupledExplicit(enriched.getBody(), FORCED_BASE_URL);
    }

    private void assertCreateAccountConsentLinksRedirectImplicit(CreateConsentResponse createConsentResponse, String baseUrl) {
        Links links = createConsentResponse.getLinks();

        String expectedSelfLink = String.format("%s/v1/consents/%s", baseUrl, CONSENT_ID);
        assertEquals(expectedSelfLink, links.getSelf());

        String expectedStatusLink = String.format("%s/v1/consents/%s/status", baseUrl, CONSENT_ID);
        assertEquals(expectedStatusLink, links.getStatus());

        assertEquals(REDIRECT_LINK, links.getScaRedirect());

        String expectedScaStatusLink = String.format("%s/v1/consents/%s/authorisations/%s", baseUrl, CONSENT_ID, AUTHORISATION_ID);
        assertEquals(expectedScaStatusLink, links.getScaStatus());

        assertNull(links.getStartAuthorisation());
    }

    private void assertCreateAccountConsentLinksRedirectExplicit(CreateConsentResponse createConsentResponse, String baseUrl) {
        Links links = createConsentResponse.getLinks();

        String expectedSelfLink = String.format("%s/v1/consents/%s", baseUrl, CONSENT_ID);
        assertEquals(expectedSelfLink, links.getSelf());

        String expectedStatusLink = String.format("%s/v1/consents/%s/status", baseUrl, CONSENT_ID);
        assertEquals(expectedStatusLink, links.getStatus());

        String expectedStartAuthorisationLink = String.format("%s/v1/consents/%s/authorisations", baseUrl, CONSENT_ID);
        assertEquals(expectedStartAuthorisationLink, links.getStartAuthorisation());

        assertNull(links.getScaRedirect());
        assertNull(links.getScaStatus());
    }

    private void assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuIdentification(CreateConsentResponse createConsentResponse, String baseUrl) {
        Links links = createConsentResponse.getLinks();

        String expectedSelfLink = String.format("%s/v1/consents/%s", baseUrl, CONSENT_ID);
        assertEquals(expectedSelfLink, links.getSelf());

        String expectedStatusLink = String.format("%s/v1/consents/%s/status", baseUrl, CONSENT_ID);
        assertEquals(expectedStatusLink, links.getStatus());

        String expectedStartAuthorisationWithPsuIdentificationLink = String.format("%s/v1/consents/%s/authorisations/%s", baseUrl, CONSENT_ID, AUTHORISATION_ID);
        assertEquals(expectedStartAuthorisationWithPsuIdentificationLink, links.getStartAuthorisationWithPsuIdentification());

        String expectedScaStatusLink = String.format("%s/v1/consents/%s/authorisations/%s", baseUrl, CONSENT_ID, AUTHORISATION_ID);
        assertEquals(expectedScaStatusLink, links.getScaStatus());

        assertNull(links.getStartAuthorisationWithPsuAuthentication());
        assertNull(links.getScaRedirect());
    }

    private void assertCreateAccountConsentLinksEmbeddedDecoupledImplicitPsuAuthentication(CreateConsentResponse createConsentResponse, String baseUrl) {
        Links links = createConsentResponse.getLinks();

        String expectedSelfLink = String.format("%s/v1/consents/%s", baseUrl, CONSENT_ID);
        assertEquals(expectedSelfLink, links.getSelf());

        String expectedStatusLink = String.format("%s/v1/consents/%s/status", baseUrl, CONSENT_ID);
        assertEquals(expectedStatusLink, links.getStatus());

        String expectedStartAuthorisationWithPsuAuthenticationLink = String.format("%s/v1/consents/%s/authorisations/%s", baseUrl, CONSENT_ID, AUTHORISATION_ID);
        assertEquals(expectedStartAuthorisationWithPsuAuthenticationLink, links.getStartAuthorisationWithPsuAuthentication());

        String expectedScaStatusLink = String.format("%s/v1/consents/%s/authorisations/%s", baseUrl, CONSENT_ID, AUTHORISATION_ID);
        assertEquals(expectedScaStatusLink, links.getScaStatus());

        assertNull(links.getStartAuthorisationWithPsuIdentification());
        assertNull(links.getScaRedirect());
    }

    private void assertCreateAccountConsentLinksEmbeddedDecoupledExplicit(CreateConsentResponse createConsentResponse, String baseUrl) {
        Links links = createConsentResponse.getLinks();

        String expectedSelfLink = String.format("%s/v1/consents/%s", baseUrl, CONSENT_ID);
        assertEquals(expectedSelfLink, links.getSelf());

        String expectedStatusLink = String.format("%s/v1/consents/%s/status", baseUrl, CONSENT_ID);
        assertEquals(expectedStatusLink, links.getStatus());

        String expectedStartAuthorisationLink = String.format("%s/v1/consents/%s/authorisations", baseUrl, CONSENT_ID);
        assertEquals(expectedStartAuthorisationLink, links.getStartAuthorisation());

        assertNull(links.getStartAuthorisationWithPsuIdentification());
        assertNull(links.getStartAuthorisationWithPsuAuthentication());
        assertNull(links.getScaRedirect());
        assertNull(links.getScaStatus());
    }

    private CreateConsentResponse buildCreateConsentResponse() {
        CreateConsentResponse createConsentResponse = new CreateConsentResponse(null, CONSENT_ID,
                                                                                null, null,
                                                                                null, null, false);
        createConsentResponse.setAuthorizationId(AUTHORISATION_ID);
        return createConsentResponse;
    }

    private CreateConsentReq buildCreateConsentReq() {
        return new CreateConsentReq();
    }

    private AspspSettings buildAspspSettings(boolean forceBaseUrl) {
        return new AspspSettings(1, false, false, null, null,
                                 null, false, null, null,
                                 1, 1, false, false, false,
                                 false, false, false, 1,
                                 null, 1, 1,
                                 null, 1, false,
                                 false, false, forceBaseUrl, FORCED_BASE_URL);
    }

    private MockHttpServletRequest buildMockHttpServletRequest() {
        return new MockHttpServletRequest();
    }
}
