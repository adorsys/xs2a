/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceInternalTest {
    @InjectMocks
    private AisConsentServiceInternal aisConsentService;
    @Mock
    private AisConsentMapper consentMapper;
    @Mock
    private AisConsentRepository aisConsentRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private PsuData psuData;
    @Mock
    SecurityDataService securityDataService;

    private AisConsent aisConsent;
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu-id-1", null, null, null);
    private static final byte[] ENCRYPTED_CONSENT_DATA = "test data".getBytes();
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";


    @Before
    public void setUp() {
        aisConsent = buildConsent();
        CmsAspspConsentDataBase64 cmsAspspConsentDataBase64 = buildUpdateBlobRequest();
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
        when(securityDataService.encryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.encryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, cmsAspspConsentDataBase64.getAspspConsentDataBase64()))
            .thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));
    }

    @Test
    public void shouldReturnAisConsent_whenGetConsentByIdIsCalled() {
        // When
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());

        // Then
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Assert
        assertTrue(retrievedConsent.isPresent());
        assertThat(retrievedConsent.get().getId(), is(equalTo(aisConsent.getId().toString())));
    }

    @Test
    public void shouldReturnExternalId_WhenCreateConsentIsCalled() {
        // When
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(psuData);

        // Then
        Optional<String> externalId = aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Assert
        assertTrue(externalId.isPresent());
        assertThat(externalId.get(), is(equalTo(aisConsent.getExternalId())));
    }

    @Test
    public void updateAccountAccessById() {
        // When
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);

        // Then
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(Arrays.asList(
            new AccountInfo(UUID.randomUUID().toString(), "iban-1", "EUR"),
            new AccountInfo(UUID.randomUUID().toString(), "iban-1", "USD")
        ));
        Optional<String> consentId = aisConsentService.updateAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Assert
        assertTrue(consentId.isPresent());

        // Then
        info = new AisAccountAccessInfo();
        info.setAccounts(Arrays.asList(
            new AccountInfo(UUID.randomUUID().toString(), "iban-1", "EUR"),
            new AccountInfo(UUID.randomUUID().toString(), "iban-2", "USD"),
            new AccountInfo(UUID.randomUUID().toString(), "iban-2", "EUR"),
            new AccountInfo(UUID.randomUUID().toString(), "iban-3", "USD")
        ));
        consentId = aisConsentService.updateAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Assert
        assertTrue(consentId.isPresent());

        // Then
        Optional<String> consentId_notExist = aisConsentService.updateAccountAccess(EXTERNAL_CONSENT_ID_NOT_EXIST, buildAccess());
        // Assert
        assertFalse(consentId_notExist.isPresent());
    }

    @Test
    public void updateConsentStatusById_UpdateFinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(securityDataService.decryptId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(FINALISED_CONSENT_ID));
        when(aisConsentRepository.findByExternalId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(finalisedConsent));

        //When
        boolean result = aisConsentService.updateConsentStatusById(FINALISED_CONSENT_ID, ConsentStatus.EXPIRED);

        //Then
        assertFalse(result);
    }

    private AisConsent buildConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now());
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return aisConsent;
    }

    private CreateAisConsentRequest buildCorrectCreateAisConsentRequest() {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setAccess(buildAccess());
        request.setCombinedServiceIndicator(true);
        request.setAllowedFrequencyPerDay(2);
        request.setRequestedFrequencyPerDay(5);
        request.setPsuData(PSU_ID_DATA);
        request.setRecurringIndicator(true);
        request.setTppId("tpp-id-1");
        request.setValidUntil(LocalDate.now());
        request.setTppRedirectPreferred(true);
        return request;
    }

    private AisAccountAccessInfo buildAccess() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(buildAccountsInfo());
        return info;
    }

    private List<AccountInfo> buildAccountsInfo() {
        return Collections.singletonList(new AccountInfo(UUID.randomUUID().toString(), "iban-1", "EUR"));
    }

    private CmsAspspConsentDataBase64 buildUpdateBlobRequest() {
        return new CmsAspspConsentDataBase64("encryptedId", Base64.getEncoder().encodeToString("decrypted consent data".getBytes()));
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
            null, false,
            null, 0,
            null, null,
            false, false, null, null, null);
    }

    private AspspConsentDataEntity getAspspConsentData() {
        AspspConsentDataEntity consentData = new AspspConsentDataEntity();
        consentData.setConsentId(EXTERNAL_CONSENT_ID);
        consentData.setData(ENCRYPTED_CONSENT_DATA);
        return consentData;
    }

    private AisConsent buildFinalisedConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now());
        aisConsent.setConsentStatus(ConsentStatus.REJECTED);
        return aisConsent;
    }
}
