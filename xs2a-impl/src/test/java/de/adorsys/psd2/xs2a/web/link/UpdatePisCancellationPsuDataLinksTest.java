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

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
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
class UpdatePisCancellationPsuDataLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private UpdatePisCancellationPsuDataLinks links;
    private PaymentAuthorisationParameters request;
    private AuthenticationObject authenticationObject;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);

        JsonReader jsonReader = new JsonReader();
        request = new PaymentAuthorisationParameters();
        request.setPaymentService(SINGLE);
        request.setPaymentProduct(PAYMENT_PRODUCT);
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);

        authenticationObject = jsonReader.getObjectFromFile("json/link/authentication-object.json", AuthenticationObject.class);
    }

    @Test
    void isScaStatusMethodAuthenticated() {
        links = new UpdatePisCancellationPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.PSUAUTHENTICATED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setSelectAuthenticationMethod(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodIdentified() {
        links = new UpdatePisCancellationPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.PSUIDENTIFIED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isAnotherScaStatus_failed() {
        links = new UpdatePisCancellationPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.FAILED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodSelectedAndNotDecoupled() {
        links = new UpdatePisCancellationPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.SCAMETHODSELECTED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setAuthoriseTransaction(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodNotSelectedOrDecoupled() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        links = new UpdatePisCancellationPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.RECEIVED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setAuthoriseTransaction(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusFinalised() {
        links = new UpdatePisCancellationPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.FINALISED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }
}
