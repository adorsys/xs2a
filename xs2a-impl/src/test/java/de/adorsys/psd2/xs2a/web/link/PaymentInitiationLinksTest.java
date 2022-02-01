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
    private static final String SELF_LINK = "http://url/v1/payments/sepa-credit-transfers/1111111111111";
    private static final String STATUS_LINK = "http://url/v1/payments/sepa-credit-transfers/1111111111111/status";
    private static final String START_AUTHORISATION = "http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations";
    private static final String SCA_STATUS = "http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda";

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
        // Given
        response.setTransactionStatus(TransactionStatus.RJCT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService,
            paymentRequestParameters, response, null);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndExplicitMethodAndNotMultiLevelScaRequired() {
        // Given
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType(START_AUTHORISATION));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbedded_Explicit_MultiLevelScaNotRequired_SigningBasketModeActive() {
        // Given
        response.setMultilevelScaRequired(false);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(true)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setStartAuthorisation(new HrefType(START_AUTHORISATION));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataIsEmpty() {
        // Given
        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);

        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType(START_AUTHORISATION));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndExplicitMethodAndMultiLevelScaRequiredAndPsuDataNotEmpty() {
        // Given
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        response.setMultilevelScaRequired(true);
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setStartAuthorisationWithPsuAuthentication(new HrefType(START_AUTHORISATION));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsEmpty() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setUpdatePsuAuthentication(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachEmbeddedAndImplicitMethodAndPsuDataIsNotEmpty() {
        // Given
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);
        paymentRequestParameters.setPsuData(psuIdData);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setUpdatePsuAuthentication(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndExplicitMethod() {
        // Given
        response.setMultilevelScaRequired(true);

        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setStartAuthorisation(new HrefType(START_AUTHORISATION));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachDecoupledAndImplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, null);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndExplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(true)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setStartAuthorisation(new HrefType(START_AUTHORISATION));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndOauthFlow() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.OAUTH);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setScaRedirect(new HrefType(null));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproach_isNull() {
        // Given
        response.setAuthorizationId(null);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
                                            .httpUrl(HTTP_URL)
                                            .isExplicitMethod(false)
                                            .isSigningBasketModeActive(false)
                                            .isAuthorisationConfirmationRequestMandated(false)
                                            .instanceId("")
                                            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.OAUTH);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void scaApproachRedirectAndImplicitMethod() {
        // Given
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID))
            .thenReturn(ScaApproach.REDIRECT);

        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentScaRedirectLink(PAYMENT_ID, AUTHORISATION_ID, INTERNAL_REQUEST_ID, "")).thenReturn(REDIRECT_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isSigningBasketModeActive(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentInitiationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, paymentRequestParameters,
                                           response, ScaRedirectFlow.REDIRECT);
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));

        // Then
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
        expectedLinks.setSelf(new HrefType(SELF_LINK));
        expectedLinks.setStatus(new HrefType(STATUS_LINK));
        expectedLinks.setScaRedirect(new HrefType(REDIRECT_LINK));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }

}
