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
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
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
import static org.junit.Assert.*;
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
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    private AisConsent aisConsent;
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu-id-1", null, null, null);
    private static final byte[] ENCRYPTED_CONSENT_DATA = "test data".getBytes();
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";

    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    @Before
    public void setUp() {
        aisConsent = buildConsent(EXTERNAL_CONSENT_ID);
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
        when(securityDataService.encryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.encryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA))
            .thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));
        when(tppInfoMapper.mapToTppInfoEntity(buildTppInfo())).thenReturn(buildTppInfoEntity());
    }

    @Test
    public void shouldReturnAisConsent_whenGetConsentByIdIsCalled() {
        // When
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent)).thenReturn(aisConsent);
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
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency("EUR").build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency("USD").build())
        );
        Optional<String> consentId = aisConsentService.updateAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Assert
        assertTrue(consentId.isPresent());

        // Then
        info = new AisAccountAccessInfo();
        info.setAccounts(Arrays.asList(
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency("EUR").build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-2").currency("USD").build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-3").currency("EUR").build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-3").currency("USD").build())
        );
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

    @Test
    public void getAuthorisationScaStatus_success() {
        List<AisConsentAuthorization> authorisations = Collections.singletonList(buildConsentAuthorisation(AUTHORISATION_ID));
        AisConsent consent = buildConsentWithAuthorisations(EXTERNAL_CONSENT_ID, authorisations);
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));

        // When
        Optional<ScaStatus> actual = aisConsentService.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongConsentId() {
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = aisConsentService.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID_NOT_EXIST, AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongAuthorisationId() {
        List<AisConsentAuthorization> authorisations = Collections.singletonList(buildConsentAuthorisation(WRONG_AUTHORISATION_ID));
        AisConsent consent = buildConsentWithAuthorisations(EXTERNAL_CONSENT_ID, authorisations);
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));

        // When
        Optional<ScaStatus> actual = aisConsentService.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }


    private AisConsent buildConsent(String externalId) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(externalId);
        aisConsent.setExpireDate(LocalDate.now());
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return aisConsent;
    }

    private AisConsent buildConsentWithAuthorisations(String externalId, List<AisConsentAuthorization> authorisations) {
        AisConsent aisConsent = buildConsent(externalId);
        aisConsent.setAuthorizations(authorisations);
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
        request.setTppInfo(buildTppInfo());
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
        return Collections.singletonList(AccountInfo.builder()
                                             .resourceId(UUID.randomUUID().toString())
                                             .accountIdentifier("iban-1")
                                             .currency("EUR")
                                             .build());
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
            null, false,
            null, 0,
            null, null,
            false, false, null, null, null);
    }

    private AisConsent buildFinalisedConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now());
        aisConsent.setConsentStatus(ConsentStatus.REJECTED);
        return aisConsent;
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("tpp-id-1");
        return tppInfo;
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber("tpp-id-1");
        return tppInfoEntity;
    }

    private AisConsentAuthorization buildConsentAuthorisation(String externalId) {
        AisConsentAuthorization authorisation = new AisConsentAuthorization();
        authorisation.setExternalId(externalId);
        authorisation.setScaStatus(SCA_STATUS);
        return authorisation;
    }
}
