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
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
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
public class PisAuthorisationCancellationLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String REDIRECT_LINK = "built_redirect_link";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    private PsuIdData psuIdData;
    private PisAuthorisationCancellationLinks links;

    private Links expectedLinks;
    private JsonReader jsonReader;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();
        expectedLinks = new Links();
    }

    @Test
    public void scaApproachEmbeddedAndPsuDataIsEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/empty.json", PsuIdData.class);
        when(scaApproachResolver.getCancellationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      psuIdData, null);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setUpdatePsuIdentification("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachEmbeddedAndPsuDataIsNotEmpty() {
        when(scaApproachResolver.getCancellationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      psuIdData, null);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndPsuDataIsEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/empty.json", PsuIdData.class);
        when(scaApproachResolver.getCancellationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      psuIdData, null);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setUpdatePsuIdentification("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachDecoupledAndPsuDataIsNotEmpty() {
        when(scaApproachResolver.getCancellationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      psuIdData, null);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirect() {
        when(scaApproachResolver.getCancellationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.REDIRECT);
        when(redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(eq(PAYMENT_ID), eq(AUTHORISATION_ID))).thenReturn(REDIRECT_LINK);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      psuIdData, ScaRedirectFlow.REDIRECT);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaRedirect(REDIRECT_LINK);
        expectedLinks.setScaStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachOAuth() {
        when(scaApproachResolver.getCancellationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.OAUTH);

        links = new PisAuthorisationCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder,
                                                      PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID, AUTHORISATION_ID,
                                                      psuIdData, null);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setScaOAuth("scaOAuth");
        assertEquals(expectedLinks, links);
    }
}
