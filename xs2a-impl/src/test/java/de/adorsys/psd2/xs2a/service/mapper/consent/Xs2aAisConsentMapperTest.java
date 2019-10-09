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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Xs2aAisConsentMapper.class, Xs2aToSpiPsuDataMapper.class, Xs2aToSpiAccountAccessMapper.class,
    Xs2aToSpiAccountReferenceMapper.class})
public class Xs2aAisConsentMapperTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @Autowired
    private Xs2aAisConsentMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    public void mapToAccountConsent() {
        AisAccountConsent aisAccountConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/account-consent.json", AisAccountConsent.class);
        AccountConsent expectedAccountConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-consent.json", AccountConsent.class);

        AccountConsent actualAccountConsent = mapper.mapToAccountConsent(aisAccountConsent);
        assertEquals(expectedAccountConsent, actualAccountConsent);
    }

    @Test
    public void mapToAccountConsent_nullValue() {
        AccountConsent actualAccountConsent = mapper.mapToAccountConsent(null);
        assertNull(actualAccountConsent);
    }

    @Test
    public void mapToAccountConsentWithNewStatus() {
        AccountConsent accountConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-consent.json", AccountConsent.class);
        AccountConsent expectedAccountConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-consent-rejected.json", AccountConsent.class);

        AccountConsent actualAccountConsent = mapper.mapToAccountConsentWithNewStatus(accountConsent, ConsentStatus.REJECTED);
        assertEquals(expectedAccountConsent, actualAccountConsent);
    }

    @Test
    public void mapToAccountConsentWithNewStatus_nullValue() {
        AccountConsent actualAccountConsent = mapper.mapToAccountConsentWithNewStatus(null, ConsentStatus.REJECTED);
        assertNull(actualAccountConsent);
    }

    @Test
    public void mapToAisAccountAccessInfo() {
        Xs2aAccountAccess xs2aAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-access.json", Xs2aAccountAccess.class);
        AisAccountAccessInfo expectedAisAccountAccessInfo = jsonReader.getObjectFromFile("json/service/mapper/consent/account-access-info.json", AisAccountAccessInfo.class);

        AisAccountAccessInfo aisAccountAccessInfo = mapper.mapToAisAccountAccessInfo(xs2aAccountAccess);
        assertEquals(expectedAisAccountAccessInfo, aisAccountAccessInfo);
    }

    @Test
    public void mapToAisAccountAccessInfo_emptyFields() {
        Xs2aAccountAccess xs2aAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-access-empty.json", Xs2aAccountAccess.class);
        AisAccountAccessInfo expectedAisAccountAccessInfo = jsonReader.getObjectFromFile("json/service/mapper/consent/account-access-info-empty.json", AisAccountAccessInfo.class);

        AisAccountAccessInfo aisAccountAccessInfo = mapper.mapToAisAccountAccessInfo(xs2aAccountAccess);
        assertEquals(expectedAisAccountAccessInfo, aisAccountAccessInfo);
    }

    @Test
    public void mapToSpiScaConfirmation() {
        PsuIdData psuIdData = new PsuIdData("psuId", "", "", "");
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setConsentId(CONSENT_ID);
        request.setScaAuthenticationData("123456");

        SpiScaConfirmation spiScaConfirmation = mapper.mapToSpiScaConfirmation(request, psuIdData);

        assertEquals(CONSENT_ID, spiScaConfirmation.getConsentId());
        assertEquals("psuId", spiScaConfirmation.getPsuId());
        assertEquals("123456", spiScaConfirmation.getTanNumber());
    }

    @Test
    public void mapToSpiScaConfirmation_psuIdDataIsNull() {
        SpiScaConfirmation spiScaConfirmation = mapper.mapToSpiScaConfirmation(new UpdateConsentPsuDataReq(), null);
        assertNotNull(spiScaConfirmation);
        assertNull(spiScaConfirmation.getPsuId());
    }

    @Test
    public void mapToSpiUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, CONSENT_ID, AUTHORISATION_ID);
        response.setScaAuthenticationData("123456");
        Xs2aAuthenticationObject chosenScaMethod = new Xs2aAuthenticationObject();
        chosenScaMethod.setAuthenticationMethodId("3284932jk6456");
        response.setChosenScaMethod(chosenScaMethod);

        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        PsuIdData psuData = new PsuIdData("1", "2", "3", "4");
        request.setPsuData(psuData);
        request.setConsentId(CONSENT_ID);
        request.setAuthorizationId(AUTHORISATION_ID);

        UpdateConsentPsuDataReq updateConsentPsuDataReq = mapper.mapToSpiUpdateConsentPsuDataReq(response, request);
        assertEquals(psuData, updateConsentPsuDataReq.getPsuData());
        assertEquals(CONSENT_ID, updateConsentPsuDataReq.getConsentId());
        assertEquals(AUTHORISATION_ID, updateConsentPsuDataReq.getAuthorizationId());
        assertEquals("3284932jk6456", updateConsentPsuDataReq.getAuthenticationMethodId());
        assertEquals("123456", updateConsentPsuDataReq.getScaAuthenticationData());
        assertEquals(ScaStatus.SCAMETHODSELECTED, updateConsentPsuDataReq.getScaStatus());
    }

    @Test
    public void mapToSpiUpdateConsentPsuDataReq_nullValue() {
        UpdateConsentPsuDataReq updateConsentPsuDataReq = mapper.mapToSpiUpdateConsentPsuDataReq(null, new UpdateConsentPsuDataReq());
        assertNull(updateConsentPsuDataReq);
    }

    @Test
    public void mapToCreateAisConsentRequest() {
        PsuIdData psuData = new PsuIdData("1", "2", "3", "4");
        TppInfo tppInfo = new TppInfo();
        CreateConsentReq request = new CreateConsentReq();
        request.setFrequencyPerDay(3);
        LocalDate validUntil = LocalDate.now();
        request.setValidUntil(validUntil);
        request.setRecurringIndicator(true);
        request.setCombinedServiceIndicator(true);
        TppRedirectUri tppRedirectUri = new TppRedirectUri("12", "13");
        request.setTppRedirectUri(tppRedirectUri);
        request.setAccess(jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-access.json", Xs2aAccountAccess.class));

        AisAccountAccessInfo expectedAisAccountAccessInfo = jsonReader.getObjectFromFile("json/service/mapper/consent/account-access-info.json", AisAccountAccessInfo.class);


        CreateAisConsentRequest createAisConsentRequest = mapper.mapToCreateAisConsentRequest(request, psuData, tppInfo, 34, INTERNAL_REQUEST_ID);

        assertEquals(psuData, createAisConsentRequest.getPsuData());
        assertEquals(tppInfo, createAisConsentRequest.getTppInfo());
        assertEquals(3, createAisConsentRequest.getRequestedFrequencyPerDay());
        assertEquals(34, (int) createAisConsentRequest.getAllowedFrequencyPerDay());
        assertEquals(validUntil, createAisConsentRequest.getValidUntil());
        assertTrue(createAisConsentRequest.isRecurringIndicator());
        assertTrue(createAisConsentRequest.isCombinedServiceIndicator());
        assertEquals(tppRedirectUri, createAisConsentRequest.getTppRedirectUri());
        assertEquals(expectedAisAccountAccessInfo, createAisConsentRequest.getAccess());
    }

    @Test
    public void mapToCreateAisConsentRequest_nullValue() {
        PsuIdData psuData = new PsuIdData("1", "2", "3", "4");
        TppInfo tppInfo = new TppInfo();

        CreateAisConsentRequest createAisConsentRequest = mapper.mapToCreateAisConsentRequest(null, psuData, tppInfo, 34, INTERNAL_REQUEST_ID);

        assertNull(createAisConsentRequest);
    }
}
