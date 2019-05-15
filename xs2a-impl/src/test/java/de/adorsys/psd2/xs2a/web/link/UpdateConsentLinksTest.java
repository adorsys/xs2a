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
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateConsentLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCiu4GLwd6mq";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private UpdateConsentLinks links;

    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();
    }

    @Test
    public void isScaStatusMethodAuthenticated() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED);
        links = new UpdateConsentLinks(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/status");
        expectedLinks.setSelectAuthenticationMethod("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isAnotherScaStatus_failed() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.FAILED);
        links = new UpdateConsentLinks(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/status");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodSelectedAndDecoupleApproach() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED);
        links = new UpdateConsentLinks(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/status");
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodSelectedAndRedirectApproach() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.REDIRECT);

        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED);
        links = new UpdateConsentLinks(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/status");
        expectedLinks.setAuthoriseTransaction("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusFinalised() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.FINALISED);
        links = new UpdateConsentLinks(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/status");
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodIdentified() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED);
        links = new UpdateConsentLinks(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/status");
        expectedLinks.setStartAuthorisationWithPsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    private UpdateConsentPsuDataResponse buildUpdateConsentPsuDataResponse(ScaStatus scaStatus) {
        return new UpdateConsentPsuDataResponse(scaStatus, CONSENT_ID, AUTHORISATION_ID);
    }
}
