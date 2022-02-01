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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.PaymentCancellationLinks;
import de.adorsys.psd2.xs2a.web.link.holder.LinkParameters;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationAspectServiceTest {
    private static final String HTTP_URL = "http://base.url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private CancelPaymentResponse response;

    @InjectMocks
    private PaymentCancellationAspectService service;

    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private RequestProviderService requestProviderService;

    private ResponseObject<CancelPaymentResponse> responseObject;
    private PisPaymentCancellationRequest paymentCancellationRequest;

    @BeforeEach
    void setUp() {
        response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(true);
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setPaymentId(PAYMENT_ID);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(PaymentType.SINGLE);
        response.setPsuData(PSU_DATA);
        response.setTransactionStatus(TransactionStatus.ACCP);

        paymentCancellationRequest = new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID,
            false, new TppRedirectUri("ok_url", "nok_url"));
    }

    @Test
    void cancelPayment_status_RJCT() {
        // Given
        response.setTransactionStatus(TransactionStatus.RJCT);

        responseObject = ResponseObject.<CancelPaymentResponse>builder()
            .body(response)
            .build();
        // When
        ResponseObject<CancelPaymentResponse> actualResponse = service.cancelPayment(responseObject, paymentCancellationRequest);

        // Then
        assertFalse(actualResponse.hasError());
        assertEquals(actualResponse.getBody(), response);
    }

    @Test
    void cancelPayment_success() {
        when(requestProviderService.getInstanceId()).thenReturn("instanceId");

        JsonReader jsonReader = new JsonReader();
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);

        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());
        when(cancellationScaNeededDecider.isScaRequired(true)).thenReturn(true);

        when(authorisationMethodDecider.isExplicitMethod(false, false)).thenReturn(false);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        when(aspspProfileServiceWrapper.getScaRedirectFlow()).thenReturn(ScaRedirectFlow.REDIRECT);

        LinkParameters linkParameters = LinkParameters.builder()
            .httpUrl(HTTP_URL)
            .isExplicitMethod(false)
            .isAuthorisationConfirmationRequestMandated(false)
            .instanceId("instanceId")
            .build();

        PaymentCancellationLinks links = new PaymentCancellationLinks(linkParameters, scaApproachResolver, redirectLinkBuilder,
            redirectIdService, response, ScaRedirectFlow.REDIRECT);

        responseObject = ResponseObject.<CancelPaymentResponse>builder()
            .body(response)
            .build();
        ResponseObject<CancelPaymentResponse> actualResponse = service.cancelPayment(responseObject, paymentCancellationRequest);

        assertFalse(actualResponse.hasError());
        assertEquals(actualResponse.getBody().getLinks(), links);
    }

    @Test
    void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.<CancelPaymentResponse>builder()
            .fail(AIS_400, of(CONSENT_UNKNOWN_400))
            .build();
        ResponseObject<CancelPaymentResponse> actualResponse = service.cancelPayment(responseObject, paymentCancellationRequest);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
