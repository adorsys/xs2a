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
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
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
class PisAuthorisationCancellationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    private PisAuthorisationCancellationLinks links;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);
    }

    @Test
    void scaApproachEmbedded() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);
        LinkParameters linkParameters = LinkParameters.builder().httpUrl(HTTP_URL)
            .paymentService(PAYMENT_SERVICE).paymentProduct(PAYMENT_PRODUCT).paymentId(PAYMENT_ID)
            .authorisationId(AUTHORISATION_ID).internalRequestId(null).instanceId("").build();

        links = new PisAuthorisationCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, null);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }


    @Test
    void scaApproachDecoupledAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder().httpUrl(HTTP_URL)
            .paymentService(PAYMENT_SERVICE).paymentProduct(PAYMENT_PRODUCT).paymentId(PAYMENT_ID)
            .authorisationId(AUTHORISATION_ID).internalRequestId(null).instanceId("").build();

        links = new PisAuthorisationCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, null);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }


    @Test
    void scaApproachRedirect() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(PAYMENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "")).thenReturn(REDIRECT_LINK);

        LinkParameters linkParameters = LinkParameters.builder().httpUrl(HTTP_URL)
            .paymentService(PAYMENT_SERVICE).paymentProduct(PAYMENT_PRODUCT).paymentId(PAYMENT_ID)
            .authorisationId(AUTHORISATION_ID).internalRequestId(INTERNAL_REQUEST_ID).instanceId("").build();
        links = new PisAuthorisationCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, ScaRedirectFlow.REDIRECT);

        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirect_OauthIntegrated_AndImplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);
        when(redirectLinkBuilder.buildPaymentCancellationScaOauthRedirectLink(PAYMENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID))
            .thenReturn(REDIRECT_LINK);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID))
            .thenReturn(AUTHORISATION_ID);

        // When
        LinkParameters linkParameters = LinkParameters.builder().httpUrl(HTTP_URL)
            .paymentService(PAYMENT_SERVICE).paymentProduct(PAYMENT_PRODUCT).paymentId(PAYMENT_ID)
            .authorisationId(AUTHORISATION_ID).internalRequestId(INTERNAL_REQUEST_ID).instanceId("").build();
        links = new PisAuthorisationCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      ScaRedirectFlow.OAUTH);

        // Then
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        assertEquals(expectedLinks, links);
    }
}
