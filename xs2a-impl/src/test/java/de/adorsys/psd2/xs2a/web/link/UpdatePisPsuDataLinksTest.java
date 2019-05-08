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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePisPsuDataLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private UpdatePisPsuDataLinks links;
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;
    private Xs2aAuthenticationObject authenticationObject;

    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();

        JsonReader jsonReader = new JsonReader();
        request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentService(PAYMENT_SERVICE);
        request.setPaymentProduct(PAYMENT_PRODUCT);
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);

        authenticationObject = jsonReader.getObjectFromFile("json/link/authentication-object.json", Xs2aAuthenticationObject.class);
    }

    @Test
    public void isScaStatusMethodAuthenticated() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.PSUAUTHENTICATED, authenticationObject);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setSelectAuthenticationMethod("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isAnotherScaStatus_failed() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.FAILED, authenticationObject);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodSelectedAndEmbeddedApproach() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.SCAMETHODSELECTED, authenticationObject);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setAuthoriseTransaction("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusFinalised() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.FINALISED, authenticationObject);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodIdentified() {
        links = new UpdatePisPsuDataLinks(HTTP_URL, scaApproachResolver, request, ScaStatus.PSUIDENTIFIED, authenticationObject);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }
}
