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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
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
class CreatePiisAuthorisationLinksTest {
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

    private CreatePiisAuthorisationLinks links;
    private CreateConsentAuthorizationResponse response;

    private Links expectedLinks;
    private ScaRedirectFlow scaRedirectFlow;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);
        scaRedirectFlow = ScaRedirectFlow.REDIRECT;

        response = new CreateConsentAuthorizationResponse();
        response.setConsentId(CONSENT_ID);
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setInternalRequestId(INTERNAL_REQUEST_ID);
    }

    @Test
    void isScaStatusMethodAuthenticated_redirectScaApproachOauth() {
        scaRedirectFlow = ScaRedirectFlow.OAUTH;

        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaOauthRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID)).thenReturn(REDIRECT_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisAuthorisationLinks(linkParameters, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, scaRedirectFlow);

        // Then
        expectedLinks.setScaStatus(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_redirectScaApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "", ConsentType.PIIS_TPP)).thenReturn(REDIRECT_LINK);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisAuthorisationLinks(linkParameters, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, scaRedirectFlow);

        expectedLinks.setScaStatus(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_redirectScaApproach_confirmation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "", ConsentType.PIIS_TPP)).thenReturn(REDIRECT_LINK);
        when(redirectLinkBuilder.buildConfirmationLink(CONSENT_ID, AUTHORISATION_ID, ConsentType.PIIS_TPP)).thenReturn(CONFIRMATION_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(true)
            .instanceId("")
            .build();
        links = new CreatePiisAuthorisationLinks(linkParameters, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, scaRedirectFlow);

        expectedLinks.setScaStatus(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_embeddedScaApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisAuthorisationLinks(linkParameters, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, scaRedirectFlow);

        expectedLinks.setScaStatus(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_decoupledScaApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisAuthorisationLinks(linkParameters, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, scaRedirectFlow);

        expectedLinks.setScaStatus(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticated_oauthScaApproach() {
        scaRedirectFlow = ScaRedirectFlow.OAUTH;

        // Given
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildConsentScaOauthRedirectLink(CONSENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID)).thenReturn(REDIRECT_LINK);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePiisAuthorisationLinks(linkParameters, response, scaApproachResolver, redirectLinkBuilder, redirectIdService, scaRedirectFlow);

        // Then
        expectedLinks.setScaStatus(new HrefType("http://url/v2/consents/confirmation-of-funds/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        assertEquals(expectedLinks, links);
    }
}
