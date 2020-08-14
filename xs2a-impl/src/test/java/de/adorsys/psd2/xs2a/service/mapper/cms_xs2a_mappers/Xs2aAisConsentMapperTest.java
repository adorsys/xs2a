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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aAisConsentMapper.class, Xs2aToSpiPsuDataMapper.class, Xs2aToSpiAccountAccessMapper.class,
    Xs2aToSpiAccountReferenceMapper.class, ConsentDataMapper.class, Xs2aAccountConsentAuthorizationMapper.class})
class Xs2aAisConsentMapperTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tppBrandLoggingInformation";

    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psuId", null, null, null, null);
    private static final TppInfo TPP_INFO = new TppInfo();
    private static final int ALLOWED_FREQUENCY_PER_DAY = 10;
    private static final byte[] AIS_CONSENT_DATA_BYTES = UUID.randomUUID().toString().getBytes();

    @MockBean
    private RequestProviderService requestProviderService;

    @MockBean
    private ConsentDataMapper consentDataMapper;

    @Autowired
    private Xs2aAisConsentMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiScaConfirmation() {
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setConsentId(CONSENT_ID);
        request.setScaAuthenticationData("123456");

        SpiScaConfirmation spiScaConfirmation = mapper.mapToSpiScaConfirmation(request, PSU_ID_DATA);

        assertEquals(CONSENT_ID, spiScaConfirmation.getConsentId());
        assertEquals("psuId", spiScaConfirmation.getPsuId());
        assertEquals("123456", spiScaConfirmation.getTanNumber());
    }

    @Test
    void mapToSpiScaConfirmation_psuIdDataIsNull() {
        SpiScaConfirmation spiScaConfirmation = mapper.mapToSpiScaConfirmation(new UpdateConsentPsuDataReq(), null);
        assertNotNull(spiScaConfirmation);
        assertNull(spiScaConfirmation.getPsuId());
    }

    @Test
    void mapToSpiAccountConsent() {
        //Given
        AisConsent ais = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-consent.json", AisConsent.class);
        //When
        SpiAccountConsent spiAccountConsent = mapper.mapToSpiAccountConsent(ais);
        SpiAccountConsent spiAccountConsentExpected = jsonReader.getObjectFromFile("json/service/mapper/consent/spi-account-consent.json", SpiAccountConsent.class);
        //Then
        assertEquals(spiAccountConsentExpected, spiAccountConsent);
    }

    @Test
    void mapToSpiUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq updateAuthorisationRequest = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req.json", UpdateConsentPsuDataReq.class);
        AuthorisationProcessorResponse authorisationProcessorResponse = jsonReader.getObjectFromFile("json/service/mapper/consent/authorisation-processor-response2.json", AuthorisationProcessorResponse.class);

        UpdateConsentPsuDataReq actual = mapper.mapToUpdateConsentPsuDataReq(updateAuthorisationRequest, authorisationProcessorResponse);

        UpdateConsentPsuDataReq expected = jsonReader.getObjectFromFile("json/service/mapper/consent/update-consent-psu-data-req-mapped.json", UpdateConsentPsuDataReq.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToSpiUpdateConsentPsuDataReq_nullValue() {
        assertNull(mapper.mapToUpdateConsentPsuDataReq(null, null));
    }

    @Test
    void mapToCmsConsent() {
        //Given
        CreateConsentReq createConsentReq = jsonReader.getObjectFromFile("json/service/mapper/consent/create-consent-req.json", CreateConsentReq.class);
        AisConsentData aisConsentData = new AisConsentData(createConsentReq.getAvailableAccounts(), createConsentReq.getAllPsd2(), createConsentReq.getAvailableAccountsWithBalance(), createConsentReq.isCombinedServiceIndicator());
        when(consentDataMapper.getBytesFromConsentData(aisConsentData)).thenReturn(AIS_CONSENT_DATA_BYTES);
        when(requestProviderService.resolveTppRedirectPreferred()).thenReturn(Optional.of(true));
        when(requestProviderService.getInternalRequestIdString()).thenReturn(INTERNAL_REQUEST_ID);

        //When
        CmsConsent cmsConsent = mapper.mapToCmsConsent(createConsentReq, PSU_ID_DATA, TPP_INFO, ALLOWED_FREQUENCY_PER_DAY);

        //Then
        assertEquals(AIS_CONSENT_DATA_BYTES, cmsConsent.getConsentData());
        assertEquals(TPP_INFO, cmsConsent.getTppInformation().getTppInfo());
        assertTrue(cmsConsent.getTppInformation().isTppRedirectPreferred());
        assertEquals(createConsentReq.getFrequencyPerDay(), cmsConsent.getTppInformation().getTppFrequencyPerDay());
        assertEquals(createConsentReq.getTppNotificationData().getTppNotificationUri(), cmsConsent.getTppInformation().getTppNotificationUri());
        assertEquals(createConsentReq.getTppNotificationData().getNotificationModes(), cmsConsent.getTppInformation().getTppNotificationSupportedModes());
        assertEquals(createConsentReq.getTppRedirectUri(), cmsConsent.getAuthorisationTemplate().getTppRedirectUri());
        assertEquals(INTERNAL_REQUEST_ID, cmsConsent.getInternalRequestId());
        assertEquals(ALLOWED_FREQUENCY_PER_DAY, cmsConsent.getFrequencyPerDay());
        assertEquals(createConsentReq.getValidUntil(), cmsConsent.getValidUntil());
        assertEquals(createConsentReq.isRecurringIndicator(), cmsConsent.isRecurringIndicator());
        assertEquals(Collections.singletonList(PSU_ID_DATA), cmsConsent.getPsuIdDataList());
        assertEquals(ConsentType.AIS, cmsConsent.getConsentType());
        assertEquals(ConsentStatus.RECEIVED, cmsConsent.getConsentStatus());
        assertEquals(TPP_BRAND_LOGGING_INFORMATION, cmsConsent.getTppInformation().getTppBrandLoggingInformation());
    }

    @Test
    void mapToAisConsent_withEmptyOrNullAuthorisations() {
        //Given
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/cms-consent.json", CmsConsent.class);
        cmsConsent.setConsentData(AIS_CONSENT_DATA_BYTES);
        AisConsentData aisConsentData = new AisConsentData(null, null, null, false);
        when(consentDataMapper.mapToAisConsentData(AIS_CONSENT_DATA_BYTES)).thenReturn(aisConsentData);

        //When
        AisConsent aisConsent = mapper.mapToAisConsent(cmsConsent);

        //Then
        assertEquals(aisConsent.getId(), cmsConsent.getId());
        assertEquals(aisConsent.getConsentData(), aisConsentData);
        assertEquals(aisConsent.isRecurringIndicator(), cmsConsent.isRecurringIndicator());
        assertEquals(aisConsent.getValidUntil(), cmsConsent.getValidUntil());
        assertEquals(aisConsent.getExpireDate(), cmsConsent.getExpireDate());
        assertEquals(aisConsent.getFrequencyPerDay(), cmsConsent.getFrequencyPerDay());
        assertEquals(aisConsent.getLastActionDate(), cmsConsent.getLastActionDate());
        assertEquals(aisConsent.getConsentStatus(), cmsConsent.getConsentStatus());
        assertEquals(aisConsent.getAuthorisationTemplate(), cmsConsent.getAuthorisationTemplate());
        assertEquals(aisConsent.getPsuIdDataList(), cmsConsent.getPsuIdDataList());
        assertEquals(aisConsent.getConsentTppInformation(), cmsConsent.getTppInformation());
        assertEquals(aisConsent.isMultilevelScaRequired(), cmsConsent.isMultilevelScaRequired());
        assertTrue(aisConsent.getAuthorisations().isEmpty());
        assertEquals(aisConsent.getStatusChangeTimestamp(), cmsConsent.getStatusChangeTimestamp());
        assertEquals(aisConsent.getUsages(), cmsConsent.getUsages());
        assertEquals(aisConsent.getCreationTimestamp(), cmsConsent.getCreationTimestamp());
    }

    @Test
    void mapToAisConsent_withAuthorisations() {
        //Given
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/cms-consent-with-authorisations.json", CmsConsent.class);
        cmsConsent.setConsentData(AIS_CONSENT_DATA_BYTES);
        AisConsentData aisConsentData = new AisConsentData(null, null, null, false);
        when(consentDataMapper.mapToAisConsentData(AIS_CONSENT_DATA_BYTES)).thenReturn(aisConsentData);

        //When
        AisConsent aisConsent = mapper.mapToAisConsent(cmsConsent);

        //Then
        assertEquals(aisConsent.getId(), cmsConsent.getId());
        assertEquals(aisConsent.getConsentData(), aisConsentData);
        assertEquals(aisConsent.isRecurringIndicator(), cmsConsent.isRecurringIndicator());
        assertEquals(aisConsent.getValidUntil(), cmsConsent.getValidUntil());
        assertEquals(aisConsent.getExpireDate(), cmsConsent.getExpireDate());
        assertEquals(aisConsent.getFrequencyPerDay(), cmsConsent.getFrequencyPerDay());
        assertEquals(aisConsent.getLastActionDate(), cmsConsent.getLastActionDate());
        assertEquals(aisConsent.getConsentStatus(), cmsConsent.getConsentStatus());
        assertEquals(aisConsent.getAuthorisationTemplate(), cmsConsent.getAuthorisationTemplate());
        assertEquals(aisConsent.getPsuIdDataList(), cmsConsent.getPsuIdDataList());
        assertEquals(aisConsent.getConsentTppInformation(), cmsConsent.getTppInformation());
        assertEquals(aisConsent.isMultilevelScaRequired(), cmsConsent.isMultilevelScaRequired());
        for (int i = 0; i < aisConsent.getAuthorisations().size(); i++) {
            ConsentAuthorization consentAuthorization = aisConsent.getAuthorisations().get(i);
            Authorisation authorisation = cmsConsent.getAuthorisations().get(i);

            assertEquals(consentAuthorization.getId(), authorisation.getAuthorisationId());
            assertEquals(consentAuthorization.getPsuIdData(), authorisation.getPsuIdData());
            assertEquals(consentAuthorization.getScaStatus(), authorisation.getScaStatus());
        }
        assertEquals(aisConsent.getStatusChangeTimestamp(), cmsConsent.getStatusChangeTimestamp());
        assertEquals(aisConsent.getUsages(), cmsConsent.getUsages());
        assertEquals(aisConsent.getCreationTimestamp(), cmsConsent.getCreationTimestamp());
    }
}
