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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAction;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceInternalTest {
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String PSU_ID = "psu-id-1";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final PsuData PSU_DATA = new PsuData(PSU_ID, null, null, null);
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_NUMBER = "Test Authorisation Number";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String REQUEST_URI = "request/uri";
    private static final String TPP_ID = "tppId";
    private static final String EUR = "EUR";
    private static final String USD = "USD";

    private AisConsent aisConsent;
    private List<AisConsentAuthorization> aisConsentAuthorisationList = new ArrayList<>();

    @InjectMocks
    private AisConsentServiceInternal aisConsentService;

    @Mock
    private AisConsentMapper consentMapper;
    @Mock
    private AisConsentRepository aisConsentRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AisConsent aisConsentMocked;
    @Mock
    private TppInfoEntity tppInfoMocked;
    @Mock
    private PsuData psuDataMocked;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private AisConsentActionRepository aisConsentActionRepository;
    @Mock
    private TppInfoRepository tppInfoRepository;
    @Mock
    private AisConsentRequestTypeService aisConsentRequestTypeService;

    @Before
    public void setUp() {
        AisConsentAuthorization aisConsentAuthorisation = buildAisConsentAuthorisation(AUTHORISATION_ID, ScaStatus.RECEIVED);
        aisConsentAuthorisationList.add(aisConsentAuthorisation);
        aisConsent = buildConsent(EXTERNAL_CONSENT_ID);
        when(tppInfoMapper.mapToTppInfoEntity(buildTppInfo())).thenReturn(buildTppInfoEntity());
        AisConsentAction action = buildAisConsentAction();
        when(aisConsentActionRepository.save(action)).thenReturn(action);
        when(tppInfoRepository.findByAuthorisationNumber(any())).thenReturn(Optional.of(buildTppInfoEntity()));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
    }

    @Test
    public void shouldReturnAisConsent_whenGetConsentByIdIsCalled() {
        // Given
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent)).thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());

        // When
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isPresent());
        assertThat(retrievedConsent.get().getId(), is(equalTo(aisConsent.getId().toString())));
    }

    @Test
    public void getAisAccountConsentById_checkAndUpdateOnExpirationInvoked() {
        // Given
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, Collections.singletonList(psuDataMocked), LocalDate.now().minusDays(1));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent)).thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());
        when(aisConsentConfirmationExpirationService.isConsentExpiredOrFinalised(aisConsent))
            .thenReturn(true);
        doNothing().when(aisConsentConfirmationExpirationService).expireConsent(aisConsent);

        // When
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isPresent());
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(aisConsent);
    }

    @Test
    public void getAisAccountConsentById_checkAndUpdateOnExpirationNotInvoked() {
        // Given
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, Collections.singletonList(psuDataMocked), LocalDate.now());
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent)).thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());

        // When
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isPresent());
        verify(aisConsentRepository, never()).save(any(AisConsent.class));
    }

    @Test
    public void getAisAccountConsentById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        AisConsent consent = buildUsedNonRecurringConsent();

        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.isConsentExpiredOrFinalised(consent))
            .thenReturn(true);
        doNothing().when(aisConsentConfirmationExpirationService).expireConsent(consent);

        // When
        aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    public void shouldReturnExternalId_WhenCreateConsentIsCalled() {
        // Given
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());

        // When
        Optional<String> externalId = aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Then
        assertTrue(externalId.isPresent());
        assertThat(externalId.get(), is(equalTo(aisConsent.getExternalId())));
    }

    @Test
    public void createConsent_AdjustValidUntil_ZeroLifeTime() {
        // Given
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int consentLifeTime = 0;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(consentLifeTime));
        int validDays = 5;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Then
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(argument.getValue().getExpireDate(), validUntil);
    }

    @Test
    public void createConsent_AdjustValidUntil_NoAdjustment() {
        // Given
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int consentLifeTime = 10;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(consentLifeTime));
        int validDays = 5;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Then
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(argument.getValue().getExpireDate(), validUntil);
    }

    @Test
    public void createConsent_AdjustValidUntil_AdjustmentToLifeTime() {
        // Given
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int consentLifeTime = 5;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(consentLifeTime));
        int validDays = 10;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Then
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(argument.getValue().getExpireDate(), LocalDate.now().plusDays(consentLifeTime - 1));
    }

    @Test
    public void createConsent_checkLastActionDate() {
        // Given
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Then
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(LocalDate.now(), argument.getValue().getLastActionDate());
    }

    @Test
    public void updateAccountAccessById() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);

        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(Arrays.asList(
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency(EUR).build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency(USD).build())
        );
        // When
        Optional<String> consentId = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Then
        assertTrue(consentId.isPresent());

        // Given
        info = new AisAccountAccessInfo();
        info.setAccounts(Arrays.asList(
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency(EUR).build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-2").currency(USD).build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-3").currency(EUR).build(),
            AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-3").currency(USD).build())
        );

        // When
        consentId = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Then
        assertTrue(consentId.isPresent());

        // When
        Optional<String> consentId_notExist = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID_NOT_EXIST, buildAccess());
        // Then
        assertFalse(consentId_notExist.isPresent());
    }

    @Test
    public void updateConsentStatusById_UpdateFinalisedStatus_Fail() {
        // Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(aisConsentRepository.findByExternalId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(finalisedConsent));

        // When
        boolean result = aisConsentService.updateConsentStatusById(FINALISED_CONSENT_ID, ConsentStatus.EXPIRED);

        // Then
        assertFalse(result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void findAndTerminateOldConsentsByNewConsentId_failure_consentNotFound() {
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());

        aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID_NOT_EXIST);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_newConsentRecurringIndicatorIsFalse() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.isOneAccessType())
            .thenReturn(true);

        // When
        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(result);
        verify(aisConsentRepository, never()).findOldConsentsByNewConsentParams(any(), any(), any(), any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAndTerminateOldConsentsByNewConsentId_failure_wrongConsentData() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.isWrongConsentData())
            .thenReturn(true);

        // When
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_oldConsentsEmpty() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.getTppInfo())
            .thenReturn(tppInfoMocked);

        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);

        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);

        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        // When
        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(result);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.getTppInfo())
            .thenReturn(tppInfoMocked);

        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);
        when(aisConsentMocked.getPsuDataList())
            .thenReturn(psuDataList);

        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);

        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);

        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);

        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        when(cmsPsuService.isPsuDataListEqual(psuDataList, psuDataList))
            .thenReturn(true);

        AisConsent oldConsent = buildConsent(EXTERNAL_CONSENT_ID_NOT_EXIST);
        List<AisConsent> oldConsents = Collections.singletonList(oldConsent);
        when(aisConsentRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        when(aisConsentRepository.saveAll(oldConsents)).thenReturn(oldConsents);

        // When
        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result);
        assertEquals(ConsentStatus.REJECTED, oldConsent.getConsentStatus());
        verify(aisConsentRepository).saveAll(oldConsents);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_multilevel_SCA() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.getTppInfo())
            .thenReturn(tppInfoMocked);

        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);
        when(aisConsentMocked.getPsuDataList())
            .thenReturn(psuDataList);

        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);

        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);

        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);

        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        when(cmsPsuService.isPsuDataListEqual(psuDataList, psuDataList))
            .thenReturn(true);

        AisConsent oldConsent = buildConsent(EXTERNAL_CONSENT_ID_NOT_EXIST);
        oldConsent.setConsentStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        List<AisConsent> oldConsents = Collections.singletonList(oldConsent);
        when(aisConsentRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        when(aisConsentRepository.saveAll(oldConsents)).thenReturn(oldConsents);

        // When
        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result);
        assertEquals(ConsentStatus.REJECTED, oldConsent.getConsentStatus());
        verify(aisConsentRepository).saveAll(oldConsents);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_shouldFail_unequalPsuDataLists() {
        // Given
        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);

        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));
        when(aisConsentMocked.getTppInfo())
            .thenReturn(tppInfoMocked);
        when(aisConsentMocked.getPsuDataList())
            .thenReturn(psuDataList);
        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);
        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);
        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);
        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        // When
        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(result);
        verify(aisConsentRepository, never()).save(any(AisConsent.class));
    }

    @Test
    public void checkConsentAndSaveActionLog() {
        // Given
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.empty());

        try {
            aisConsentService.checkConsentAndSaveActionLog(new AisConsentActionRequest(TPP_ID, EXTERNAL_CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, true, null, null));
            assertTrue("Method works without exceptions", true);
        } catch (Exception ex) {
            fail("Exception should not be appeared.");
        }
    }

    @Test
    public void checkConsentAndSaveActionLog_updateUsageCounter() {
        // When
        AisConsentActionRequest request = new AisConsentActionRequest(TPP_ID, EXTERNAL_CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, true, null, null);
        aisConsentService.checkConsentAndSaveActionLog(request);
        // Then
        verify(aisConsentUsageService, atLeastOnce()).incrementUsage(aisConsent, request);
    }

    @Test
    public void checkConsentAndSaveActionLog_NotUpdateUsageCounter() {
        // When
        AisConsentActionRequest request = new AisConsentActionRequest(TPP_ID, EXTERNAL_CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, false, null, null);
        aisConsentService.checkConsentAndSaveActionLog(request);
        // Then
        verify(aisConsentUsageService, never()).incrementUsage(aisConsent, request);
    }

    @Test
    public void checkConsentAndSaveActionLog_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        AisConsent consent = buildUsedNonRecurringConsent();

        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.isConsentExpiredOrFinalised(consent))
            .thenReturn(true);
        doNothing().when(aisConsentConfirmationExpirationService).expireConsent(consent);

        // When
        aisConsentService.checkConsentAndSaveActionLog(new AisConsentActionRequest(TPP_ID, EXTERNAL_CONSENT_ID, ActionStatus.SUCCESS, "/uri", false, null, null));

        // Then
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    public void getConsentStatusById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        AisConsent consent = buildUsedNonRecurringConsent();

        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.isConsentExpiredOrFinalised(consent))
            .thenReturn(true);
        doNothing().when(aisConsentConfirmationExpirationService).expireConsent(consent);

        // When
        Optional<ConsentStatus> consentStatusById = aisConsentService.getConsentStatusById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(consentStatusById.isPresent());
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    public void getInitialAisAccountConsentById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        AisConsent consent = buildUsedNonRecurringConsent();

        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.isConsentExpiredOrFinalised(consent))
            .thenReturn(true);
        doNothing().when(aisConsentConfirmationExpirationService).expireConsent(consent);

        // When
        aisConsentService.getInitialAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @NotNull
    private AisConsent buildUsedNonRecurringConsent() {
        AisConsent consent = buildConsent(EXTERNAL_CONSENT_ID);

        AisConsentUsage usage = new AisConsentUsage();
        usage.setUsageDate(LocalDate.of(2019, 6, 3));

        consent.setUsages(Collections.singletonList(usage));
        consent.setConsentStatus(ConsentStatus.VALID);
        return consent;
    }

    private AisConsentAction buildAisConsentAction() {
        AisConsentAction action = new AisConsentAction();
        action.setActionStatus(ActionStatus.SUCCESS);
        action.setRequestedConsentId(EXTERNAL_CONSENT_ID);
        action.setTppId(TPP_ID);
        action.setRequestDate(LocalDate.now());
        return action;
    }

    private AisConsentAuthorization buildAisConsentAuthorisation(String externalId, ScaStatus scaStatus) {
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setConsent(aisConsent);
        aisConsentAuthorization.setExternalId(externalId);
        aisConsentAuthorization.setPsuData(PSU_DATA);
        aisConsentAuthorization.setScaStatus(scaStatus);
        return aisConsentAuthorization;
    }

    @NotNull
    private AspspSettings getAspspSettings() {
        return getAspspSettings(1);
    }

    @NotNull
    private AspspSettings getAspspSettings(int consentLifeTime) {
        return new AspspSettings(1, false, false, null, null,
                                 null, false, null, null, consentLifeTime, 1, false,
                                 false, false, false, false, 1, 1,
                                 null, 1, 1, null, 1, false, false, false, false, null, ScaRedirectFlow.REDIRECT, false, false, null, StartAuthorisationMode.AUTO, false);
    }

    private AisConsent buildConsent(String externalId) {
        return buildConsent(externalId, Collections.singletonList(psuDataMocked));
    }

    private AisConsent buildConsent(String externalId, List<PsuData> psuDataList) {
        return buildConsent(externalId, psuDataList, LocalDate.now());
    }

    private AisConsent buildConsent(String externalId, List<PsuData> psuDataList, LocalDate validUntil) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(externalId);
        aisConsent.setExpireDate(validUntil);
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setAuthorizations(aisConsentAuthorisationList);
        aisConsent.setPsuDataList(psuDataList);
        return aisConsent;
    }

    private CreateAisConsentRequest buildCorrectCreateAisConsentRequest() {
        return buildCorrectCreateAisConsentRequest(LocalDate.now());
    }

    private CreateAisConsentRequest buildCorrectCreateAisConsentRequest(LocalDate validUntil) {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setAccess(buildAccess());
        request.setCombinedServiceIndicator(true);
        request.setAllowedFrequencyPerDay(2);
        request.setRequestedFrequencyPerDay(5);
        request.setPsuData(PSU_ID_DATA);
        request.setRecurringIndicator(true);
        request.setTppInfo(buildTppInfo());
        request.setValidUntil(validUntil);
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
                                             .currency(EUR)
                                             .build());
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
                                     null, null, false,
                                     null, 0,
                                     null, null,
                                     false, false, null, null, null, null, false, Collections.emptyList(), Collections.emptyMap(), OffsetDateTime.now(),
                                     OffsetDateTime.now());

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
}
