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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAisConsentLinksImplTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCiu4GLwd6mq";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";

    @Mock
    private ScaApproachResolver scaApproachResolver;

    private UpdateAisConsentLinksImpl links;

    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);
    }

    @Test
    void isScaStatusMethodAuthenticated() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED);
        links = new UpdateAisConsentLinksImpl(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setSelectAuthenticationMethod(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isAnotherScaStatus_failed() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.FAILED);
        links = new UpdateAisConsentLinksImpl(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodSelectedAndDecoupleApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.DECOUPLED);

        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED);
        links = new UpdateAisConsentLinksImpl(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodSelectedAndRedirectApproach() {
        when(scaApproachResolver.getScaApproach(AUTHORISATION_ID)).thenReturn(ScaApproach.REDIRECT);

        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED);
        links = new UpdateAisConsentLinksImpl(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setAuthoriseTransaction(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusFinalised() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.FINALISED);
        links = new UpdateAisConsentLinksImpl(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void isScaStatusMethodIdentified() {
        UpdateConsentPsuDataResponse response = buildUpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED);
        links = new UpdateAisConsentLinksImpl(HTTP_URL, scaApproachResolver, response);

        expectedLinks.setScaStatus(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        expectedLinks.setUpdatePsuAuthentication(new HrefType("http://url/v1/consents/9mp1PaotpXSToNCiu4GLwd6mq/authorisations/463318a0-1e33-45d8-8209-e16444b18dda"));
        assertEquals(expectedLinks, links);
    }

    private UpdateConsentPsuDataResponse buildUpdateConsentPsuDataResponse(ScaStatus scaStatus) {
        return new UpdateConsentPsuDataResponse(scaStatus, CONSENT_ID, AUTHORISATION_ID, new PsuIdData());
    }
}
