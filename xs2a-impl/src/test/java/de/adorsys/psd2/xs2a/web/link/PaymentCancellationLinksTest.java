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
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final HrefType REDIRECT_LINK = new HrefType("built_redirect_link");
    private static final HrefType CANCEL_AUTH_LINK = new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations");
    private static final HrefType PIS_CANCELLATION_AUTH_LINK_URL = new HrefType(CANCEL_AUTH_LINK.getHref() + "/" + AUTHORISATION_ID);
    public static final HrefType START_AUTHORISATION_LINK = new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations");
    private static final HrefType SELF_LINK = new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111");
    private static final HrefType STATUS_LINK = new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
    private static final String CONFIRMATION_LINK = "confirmation_link";
    private static final String SCA_STATUS = "http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final PsuIdData PSU_DATA_EMPTY = new PsuIdData(null, null, null, null, null);
    private PaymentCancellationLinks links;
    private Links expectedLinks;
    private CancelPaymentResponse response;
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);

        response = new CancelPaymentResponse();
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setPaymentId(PAYMENT_ID);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(PaymentType.SINGLE);
        response.setPsuData(PSU_DATA);
        response.setTransactionStatus(TransactionStatus.ACCP);
        response.setInternalRequestId(INTERNAL_REQUEST_ID);
    }

    @Test
    void buildCancellationLinks_redirect_implicit() {
        // Given
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID), anyString(), eq(""))).thenReturn(REDIRECT_LINK.getHref());

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        expectedLinks.setScaStatus(PIS_CANCELLATION_AUTH_LINK_URL);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_redirect_implicit_confirmation() {
        // Given
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID), anyString(), eq(""))).thenReturn(REDIRECT_LINK.getHref());
        when(redirectLinkBuilder.buildPisCancellationConfirmationLink(PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID)).thenReturn(CONFIRMATION_LINK);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(true)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        expectedLinks.setScaStatus(PIS_CANCELLATION_AUTH_LINK_URL);
        expectedLinks.setConfirmation(new HrefType("http://url/confirmation_link"));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_redirect_explicit() {
        // Given
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisation(CANCEL_AUTH_LINK);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_embedded_implicit() {
        // Given
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setUpdatePsuAuthentication(PIS_CANCELLATION_AUTH_LINK_URL);
        expectedLinks.setScaStatus(PIS_CANCELLATION_AUTH_LINK_URL);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_embedded_explicit() {
        // Given
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(CANCEL_AUTH_LINK);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_OauthFlow() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.OAUTH);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaRedirect(new HrefType(null));
        expectedLinks.setScaStatus(new HrefType(SCA_STATUS));

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_decoupled_implicit() {
        // Given
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaStatus(PIS_CANCELLATION_AUTH_LINK_URL);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_decoupled_explicit() {
        // Given
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.DECOUPLED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisation(CANCEL_AUTH_LINK);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_redirect_oAuth_preStep_implicit() {
        // Given
        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        when(redirectIdService.generateRedirectId(AUTHORISATION_ID)).thenReturn(AUTHORISATION_ID);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID), anyString(), eq(""))).thenReturn(REDIRECT_LINK.getHref());

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.OAUTH_PRE_STEP);

        // Then
        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        expectedLinks.setScaStatus(PIS_CANCELLATION_AUTH_LINK_URL);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_scaOAuth_explicit() {
        // Given
        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.OAUTH);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisation(START_AUTHORISATION_LINK);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_embedded_implicit_psuEmpty() {
        // Given
        response.setPsuData(PSU_DATA_EMPTY);

        boolean isExplicitMethod = false;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setUpdatePsuAuthentication(PIS_CANCELLATION_AUTH_LINK_URL);
        expectedLinks.setScaStatus(PIS_CANCELLATION_AUTH_LINK_URL);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_embedded_explicit_psuEmpty() {
        // Given
        response.setPsuData(PSU_DATA_EMPTY);

        boolean isExplicitMethod = true;
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf(SELF_LINK);
        expectedLinks.setStatus(STATUS_LINK);
        expectedLinks.setStartAuthorisationWithPsuAuthentication(CANCEL_AUTH_LINK);

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void buildCancellationLinks_status_RJCT() {
        // Given
        boolean isExplicitMethod = true;
        response.setTransactionStatus(TransactionStatus.RJCT);

        // When
        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(isExplicitMethod)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("")
            .build();

        links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, ScaRedirectFlow.REDIRECT);

        // Then
        assertEquals(expectedLinks, links);
    }
}
