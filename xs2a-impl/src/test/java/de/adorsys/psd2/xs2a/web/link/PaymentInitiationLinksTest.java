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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
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

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

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

        psuIdData = jsonReader.getObjectFromFile("json/link/empty.json", PsuIdData.class);

        paymentRequestParameters = new PaymentInitiationParameters();
        paymentRequestParameters.setPaymentType(PaymentType.SINGLE);
        paymentRequestParameters.setPaymentProduct(PAYMENT_PRODUCT);
        paymentRequestParameters.setPsuData(psuIdData);
    }

    @Test
    public void transactionRejected() {
        response.setTransactionStatus(TransactionStatus.RJCT);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndExplicitMethodAndNotMultiLevelScaRequired() {
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisation("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisationWithPsuIdentification("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisationWithPsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, false);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuIdentification("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, false);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndExplicitMethodAndNotMultiLevelScaRequired() {
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisation("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisationWithPsuIdentification("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisationWithPsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, false);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuIdentification("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, false);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachOAuth() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.OAUTH);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, false);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaOAuth("scaOAuth");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirectAndExplicitMethod() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, true);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisation("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirectAndImplicitMethod() {
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID))).thenReturn(REDIRECT_LINK);

        links = new PaymentInitiationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, paymentRequestParameters,
                                           response, false);
        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }
}
