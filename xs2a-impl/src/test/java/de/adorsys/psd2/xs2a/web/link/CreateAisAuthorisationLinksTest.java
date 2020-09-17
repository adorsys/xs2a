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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAisAuthorisationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCi";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String CONFIRMATION_LINK = "confirmation_link";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    private CreateAisAuthorisationLinks links;
    private CreateConsentAuthorizationResponse response;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new Links();

        response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setInternalRequestId(INTERNAL_REQUEST_ID);
    }

    @Test
    void isScaStatusMethodAuthenticated_redirectScaApproachOauth() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(eq(AUTHORISATION_ID))).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(eq(CONSENT_ID), eq(AUTHORISATION_ID), eq(INTERNAL_REQUEST_ID), eq(""), eq(ConsentType.AIS))).thenReturn(REDIRECT_LINK);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, false, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_redirectScaApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(eq(AUTHORISATION_ID))).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(eq(CONSENT_ID), eq(AUTHORISATION_ID), eq(INTERNAL_REQUEST_ID), eq(""), eq(ConsentType.AIS))).thenReturn(REDIRECT_LINK);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, false, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_redirectScaApproach_confirmation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(eq(AUTHORISATION_ID))).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(eq(CONSENT_ID), eq(AUTHORISATION_ID), eq(INTERNAL_REQUEST_ID), eq(""), eq(ConsentType.AIS))).thenReturn(REDIRECT_LINK);
        when(redirectLinkBuilder.buildConfirmationLink(eq(CONSENT_ID), eq(AUTHORISATION_ID), eq(ConsentType.AIS))).thenReturn(CONFIRMATION_LINK);

        // When
        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, true, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_embeddedScaApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, false, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_decoupledScaApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateAisAuthorisationLinks(HTTP_URL, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, false, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }
}
