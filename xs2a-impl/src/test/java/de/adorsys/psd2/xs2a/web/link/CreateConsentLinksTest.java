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
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateConsentLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCi";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String CONFIRMATION_LINK = "confirmation_link";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    private CreateConsentLinks links;
    private CreateConsentResponse response;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);

        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, true, INTERNAL_REQUEST_ID, null);
        response.setAuthorizationId(AUTHORISATION_ID);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndPsuDataIsEmptyAndMultiLevelRequired() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndPsuDataIsNotEmptyAndMultiLevelRequired() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndMultiLevelNotRequired() {
        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, false, INTERNAL_REQUEST_ID, null);
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_Embedded_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, false, INTERNAL_REQUEST_ID, null);
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndImplicitMethodAndPsuDataIsNotEmpty() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndPsuDataIsEmptyAndMultiLevelRequired() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndPsuDataIsNotEmptyAndMultiLevelRequired() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndMultiLevelNotRequired() {
        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, false, INTERNAL_REQUEST_ID, null);
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_Decoupled_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, false, INTERNAL_REQUEST_ID, null);
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndImplicitMethodAndPsuDataIsNotEmpty() {

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndExplicitMethod() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "", ConsentType.AIS)).thenReturn(REDIRECT_LINK);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod_confirmation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "", ConsentType.AIS)).thenReturn(REDIRECT_LINK);
        when(redirectLinkBuilder.buildConfirmationLink(CONSENT_ID, AUTHORISATION_ID, ConsentType.AIS)).thenReturn(CONFIRMATION_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(true)
            .instanceId("")
            .build();
        links = new CreateConsentLinks(linkParameters, scaApproachResolver, response, redirectLinkBuilder, redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi"));
        expectedLinks.setStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/status"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }
}
