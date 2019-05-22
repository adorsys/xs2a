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
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePisAuthorisationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private UpdatePisAuthorisationLinks links;
    private Xs2aCreatePisAuthorisationRequest request;
    private Xs2aUpdatePisCommonPaymentPsuDataResponse response;
    private Xs2aAuthenticationObject authenticationObject;

    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();

        JsonReader jsonReader = new JsonReader();
        request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, null, PAYMENT_PRODUCT, PAYMENT_SERVICE, "");
        response = new Xs2aUpdatePisCommonPaymentPsuDataResponse();
        response.setAuthorisationId(AUTHORISATION_ID);

        authenticationObject = jsonReader.getObjectFromFile("json/link/authentication-object.json", Xs2aAuthenticationObject.class);
    }

    @Test
    public void isScaStatusMethodAuthenticated() {
        response.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        links = new UpdatePisAuthorisationLinks(HTTP_URL, scaApproachResolver, response, request);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setSelectAuthenticationMethod("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isAnotherScaStatus_failed() {
        response.setScaStatus(ScaStatus.FAILED);
        links = new UpdatePisAuthorisationLinks(HTTP_URL, scaApproachResolver, response, request);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodSelectedAndEmbedded() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setChosenScaMethod(authenticationObject);
        links = new UpdatePisAuthorisationLinks(HTTP_URL, scaApproachResolver, response, request);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setAuthoriseTransaction("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusFinalised() {
        response.setScaStatus(ScaStatus.FINALISED);
        links = new UpdatePisAuthorisationLinks(HTTP_URL, scaApproachResolver, response, request);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodIdentified() {
        response.setScaStatus(ScaStatus.PSUIDENTIFIED);
        links = new UpdatePisAuthorisationLinks(HTTP_URL, scaApproachResolver, response, request);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }
}
