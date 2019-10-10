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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInitiationLinksTest {
    private static final String HTTP_URL = "http://url";
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

    private PsuIdData psuIdData;
    private PaymentInitiationLinks links;
    private PaymentInitiationResponse response;
    private PaymentInitiationParameters paymentRequestParameters;

    private Links expectedLinks;
    private JsonReader jsonReader;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();
        expectedLinks = new Links();

        response = new CommonPaymentInitiationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setInternalRequestId(INTERNAL_REQUEST_ID);

        psuIdData = jsonReader.getObjectFromFile("json/link/empty.json", PsuIdData.class);

        paymentRequestParameters = new PaymentInitiationParameters();
        paymentRequestParameters.setPaymentType(PaymentType.SINGLE);
        paymentRequestParameters.setPaymentProduct(PAYMENT_PRODUCT);
        paymentRequestParameters.setPsuData(psuIdData);
    }

    @Test
    public void transactionRejected() {
        response.setTransactionStatus(TransactionStatus.RJCT);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndExplicitMethodAndNotMultiLevelScaRequired() {
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbedded_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, true, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, false, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, false, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndExplicitMethodAndNotMultiLevelScaRequired() {
        response.setMultilevelScaRequired(false);

        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproach_Decoupled_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        response.setMultilevelScaRequired(false);

        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, true, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }
    @Test
    public void scaApproachDecoupledAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        response.setMultilevelScaRequired(true);

        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);

        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, false, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, false, false, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachOAuth() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.OAUTH);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, false, false, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaOAuth(new HrefType("scaOAuth"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirectAndExplicitMethod() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, true, false, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirectAndImplicitMethod() {
        when(scaApproachResolver.getInitiationScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        when(redirectIdService.generateRedirectId(eq(AUTHORISATION_ID))).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID), eq(INTERNAL_REQUEST_ID))).thenReturn(REDIRECT_LINK);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, false, false, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }
}
