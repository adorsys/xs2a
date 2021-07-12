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

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePisPsuDataLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final PaymentType PAYMENT_SERVICE = PaymentType.SINGLE;
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private UpdatePisPsuDataLinks links;
    private PaymentAuthorisationParameters request;
    private AuthenticationObject authenticationObject;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);

        JsonReader jsonReader = new JsonReader();
        request = new PaymentAuthorisationParameters();
        request.setPaymentService(PAYMENT_SERVICE);
        request.setPaymentProduct(PAYMENT_PRODUCT);
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);

        authenticationObject = jsonReader.getObjectFromFile("json/link/authentication-object.json", AuthenticationObject.class);
    }

    @Test
    void isScaStatusMethodAuthenticated() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.PSUAUTHENTICATED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setSelectAuthenticationMethod(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isAnotherScaStatus_failed() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.FAILED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodSelectedAndEmbeddedApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.EMBEDDED);

        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.SCAMETHODSELECTED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setAuthoriseTransaction(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusFinalised() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.FINALISED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodIdentified() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.PSUIDENTIFIED, authenticationObject);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }
}
