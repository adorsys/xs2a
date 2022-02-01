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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePisAuthorisationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String SCA_STATUS = "http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    private PsuIdData psuIdData;
    private CreatePisAuthorisationLinks links;
    private Xs2aCreatePisAuthorisationRequest request;

    private Links expectedLinks;
    private JsonReader jsonReader;

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReader();
        psuIdData = jsonReader.getObjectFromFile("json/link/empty.json", PsuIdData.class);
        expectedLinks = new AbstractLinks(HTTP_URL);

        request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, psuIdData, PAYMENT_PRODUCT, SINGLE, "");
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndPsuDataIsEmpty() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .authorisationId(AUTHORISATION_ID)
            .internalRequestId(null)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePisAuthorisationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, request, null);

        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setUpdatePsuAuthentication(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndPsyDataIsNotEmpty() {
        // Given
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, psuIdData, PAYMENT_PRODUCT, SINGLE, "");

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .authorisationId(AUTHORISATION_ID)
            .internalRequestId(null)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePisAuthorisationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, request, null);

        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setUpdatePsuAuthentication(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirect() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(PAYMENT_ID, AUTHORISATION_ID,
            INTERNAL_REQUEST_ID, "")).thenReturn(REDIRECT_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .authorisationId(AUTHORISATION_ID)
            .internalRequestId(INTERNAL_REQUEST_ID)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePisAuthorisationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, request, ScaRedirectFlow.REDIRECT);

        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndOauthFlow() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .authorisationId(AUTHORISATION_ID)
            .internalRequestId(INTERNAL_REQUEST_ID)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();
        links = new CreatePisAuthorisationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, request, ScaRedirectFlow.OAUTH);

        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setScaRedirect(new HrefType(null));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndOauthFlowAndMandatedAuthorisation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .authorisationId(AUTHORISATION_ID)
            .internalRequestId(INTERNAL_REQUEST_ID)
            .isAuthorisationConfirmationRequestMandated(true)
            .instanceId("")
            .build();
        links = new CreatePisAuthorisationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, request, ScaRedirectFlow.OAUTH);

        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setScaRedirect(new HrefType(null));
        expectedLinks.setConfirmation(new HrefType(HTTP_URL));

        // Then
        assertEquals(expectedLinks, links);
    }
}
