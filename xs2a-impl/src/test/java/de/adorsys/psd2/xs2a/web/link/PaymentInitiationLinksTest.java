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
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentInitiationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";
    private static final String CONFIRMATION_LINK = "confirmation_link";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String PAYMENT_SERVICE = "payments";

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

    @BeforeEach
    void setUp() {
        jsonReader = new JsonReader();
        expectedLinks = new AbstractLinks(HTTP_URL);

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
    void transactionRejected() {
        response.setTransactionStatus(TransactionStatus.RJCT);
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService,
            paymentRequestParameters, response, null);
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndExplicitMethodAndNotMultiLevelScaRequired() {
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbedded_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);

        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndExplicitMethodAndNotMultiLevelScaRequired() {
        response.setMultilevelScaRequired(false);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproach_Decoupled_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        response.setMultilevelScaRequired(false);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        response.setMultilevelScaRequired(true);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndExplicitMethod() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setStartAuthorisation(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(PAYMENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "")).thenReturn(REDIRECT_LINK);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod_confirmation() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(PAYMENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "")).thenReturn(REDIRECT_LINK);
        when(redirectLinkBuilder.buildPisConfirmationLink(PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID)).thenReturn(CONFIRMATION_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(true)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111"));
        expectedLinks.setStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status"));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }

}
