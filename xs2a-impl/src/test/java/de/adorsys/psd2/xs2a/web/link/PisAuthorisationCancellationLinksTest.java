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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
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
        expectedLinks = new Links();
    }

    @Test
    void scaApproachEmbeddedAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      null, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndPsuDataIsNotEmpty() {
        when(scaApproachResolver.getScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      null, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      null, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndPsuDataIsNotEmpty() {
        when(scaApproachResolver.getScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      null, "");

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirect() {
        when(scaApproachResolver.getScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(eq(AUTHORISATION_ID))).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID), eq(INTERNAL_REQUEST_ID), eq(""))).thenReturn(REDIRECT_LINK);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      INTERNAL_REQUEST_ID, "");

        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirect_OauthPreStep_AndImplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(eq(AUTHORISATION_ID))).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID), eq(INTERNAL_REQUEST_ID), eq(""))).thenReturn(REDIRECT_LINK);

        // When
        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      INTERNAL_REQUEST_ID, "");

        // Then
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

}
