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
import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.ais.ConsentTypeSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAction;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.impl.AisConsentRepositoryImpl;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceInternalTest {
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String PSU_ID = "psu-id-1";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final PsuData PSU_DATA = new PsuData(PSU_ID, null, null, null, null);
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_NUMBER = "Test Authorisation Number";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String REQUEST_URI = "request/uri";
    private static final String TPP_ID = "tppId";
    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final List<AccountInfo> ACCOUNTS_1 = Arrays.asList(
        AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency(EUR).build(),
        AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-2").currency(USD).build(),
        AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-3").currency(EUR).build(),
        AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-3").currency(USD).build());
    private static final List<AccountInfo> ACCOUNTS_2 = Arrays.asList(
        AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency(EUR).build(),
        AccountInfo.builder().resourceId(UUID.randomUUID().toString()).accountIdentifier("iban-1").currency(USD).build());
    private static final List<NotificationSupportedMode> MODES = Arrays.asList(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA);
    private static final String REDIRECT_URI = "ok";
    private static final String NOK_REDIRECT_URI = "not ok";

    private AisConsent aisConsent;
    private List<AisConsentAuthorization> aisConsentAuthorisationList = new ArrayList<>();

    @InjectMocks
    private AisConsentServiceInternal aisConsentService;

    @Mock
    private AisConsentMapper consentMapper;
    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;
    @Mock
    private AisConsentRepositoryImpl aisConsentRepositoryImpl;
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
        AisConsentAuthorization aisConsentAuthorisation = buildAisConsentAuthorisation();
        aisConsentAuthorisationList.add(aisConsentAuthorisation);
        aisConsent = buildConsent(EXTERNAL_CONSENT_ID);
        when(tppInfoMapper.mapToTppInfoEntity(buildTppInfo())).thenReturn(buildTppInfoEntity());
        AisConsentAction action = buildAisConsentAction();
        when(aisConsentActionRepository.save(action)).thenReturn(action);
        when(tppInfoRepository.findByAuthorisationNumber(any())).thenReturn(Optional.of(buildTppInfoEntity()));
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
    }

    @Test
    public void shouldReturnAisConsent_whenGetConsentByIdIsCalled() {
        // Given
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent))
            .thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent))
            .thenReturn(buildSpiAccountConsent());

        // When
        CmsResponse<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isSuccessful());
        assertEquals(retrievedConsent.getPayload().getId(), aisConsent.getId().toString());
    }

    @Test
    public void getAisAccountConsentById_checkAndUpdateOnExpirationInvoked() {
        // Given
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, Collections.singletonList(psuDataMocked), LocalDate.now().minusDays(1));
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent))
            .thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent))
            .thenReturn(buildSpiAccountConsent());
        when(aisConsentConfirmationExpirationService.expireConsent(aisConsent)).thenReturn(aisConsent);

        // When
        CmsResponse<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isSuccessful());
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(aisConsent);
    }

    @Test
    public void getAisAccountConsentById_checkAndUpdateOnExpirationNotInvoked() {
        // Given
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, Collections.singletonList(psuDataMocked), LocalDate.now());
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent))
            .thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent))
            .thenReturn(buildSpiAccountConsent());

        // When
        CmsResponse<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isSuccessful());
        verify(aisConsentJpaRepository, never()).save(any(AisConsent.class));
    }

    @Test
    public void getAisAccountConsentById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        AisConsent consent = buildUsedNonRecurringConsent();

        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.expireConsent(consent)).thenReturn(consent);

        // When
        aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Then
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    public void createConsent_shouldReturnCreateAisConsentResponse() {
        // Given
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        when(aspspProfileService.getAspspSettings())
            .thenReturn(getAspspSettings());
        AisAccountConsent aisAccountConsent = buildSpiAccountConsent();
        when(consentMapper.mapToAisAccountConsent(aisConsent))
            .thenReturn(aisAccountConsent);

        CreateAisConsentResponse expected = new CreateAisConsentResponse(EXTERNAL_CONSENT_ID, aisAccountConsent, MODES);

        // When
        CmsResponse<CreateAisConsentResponse> actual = aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(expected, actual.getPayload());
    }

    @Test
    public void createConsent_shouldReturnCreateAisConsentResponse_withTppRedirectUri() {
        // Given
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        when(aspspProfileService.getAspspSettings())
            .thenReturn(getAspspSettings());
        AisAccountConsent aisAccountConsent = buildSpiAccountConsent();
        when(consentMapper.mapToAisAccountConsent(aisConsent))
            .thenReturn(aisAccountConsent);

        CreateAisConsentResponse expected = new CreateAisConsentResponse(EXTERNAL_CONSENT_ID, aisAccountConsent, MODES);
        CreateAisConsentRequest request = buildCorrectCreateAisConsentRequest();
        request.setTppRedirectUri(new TppRedirectUri(REDIRECT_URI, NOK_REDIRECT_URI));

        // When
        CmsResponse<CreateAisConsentResponse> actual = aisConsentService.createConsent(request);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(expected, actual.getPayload());
    }

    @Test
    public void createConsent_frequencyPerDay_isNull_shouldReturnLogicalError() {
        // Given
        CreateAisConsentRequest request = buildCorrectCreateAisConsentRequest();
        request.setAllowedFrequencyPerDay(null);

        // When
        CmsResponse<CreateAisConsentResponse> actual = aisConsentService.createConsent(request);

        // Then
        assertLogicalError(actual);
    }

    @Test
    public void createConsent_id_isNull_shouldReturnTechnicalError() {
        // Given
        aisConsent.setId(null);
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        when(aspspProfileService.getAspspSettings())
            .thenReturn(getAspspSettings());

        // When
        CmsResponse<CreateAisConsentResponse> actual = aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Then
        assertFalse(actual.isSuccessful());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    public void createConsent_AdjustValidUntil_ZeroLifeTime() {
        // Given
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int maxConsentValidityDays = 0;
        when(aspspProfileService.getAspspSettings())
            .thenReturn(getAspspSettings(maxConsentValidityDays));
        int validDays = 5;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Then
        verify(aisConsentRepositoryImpl).verifyAndSave(argument.capture());
        assertEquals(argument.getValue().getValidUntil(), validUntil);
    }

    @Test
    public void createConsent_AdjustValidUntil_NoAdjustment() {
        // Given
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int maxConsentValidityDays = 10;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(maxConsentValidityDays));
        int validDays = 5;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Then
        verify(aisConsentRepositoryImpl).verifyAndSave(argument.capture());
        assertEquals(argument.getValue().getValidUntil(), validUntil);
    }

    @Test
    public void createConsent_AdjustValidUntil_AdjustmentToLifeTime() {
        // Given
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int maxConsentValidityDays = 5;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(maxConsentValidityDays));
        int validDays = 10;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Then
        verify(aisConsentRepositoryImpl).verifyAndSave(argument.capture());
        assertEquals(argument.getValue().getValidUntil(), LocalDate.now().plusDays(maxConsentValidityDays - 1));
    }

    @Test
    public void createConsent_checkLastActionDate() {
        // Given
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);
        when(aspspProfileService.getAspspSettings())
            .thenReturn(getAspspSettings());
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        // When
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Then
        verify(aisConsentRepositoryImpl).verifyAndSave(argument.capture());
        assertEquals(LocalDate.now(), argument.getValue().getLastActionDate());
    }

    @Test
    public void updateAccountAccessById() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());
        when(aisConsentRepositoryImpl.verifyAndSave(any(AisConsent.class)))
            .thenReturn(aisConsent);

        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(ACCOUNTS_2);

        // When
        CmsResponse<String> consentId = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Then
        assertTrue(consentId.isSuccessful());

        // Given
        info = new AisAccountAccessInfo();
        info.setAccounts(ACCOUNTS_1);

        // When
        consentId = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Then
        assertTrue(consentId.isSuccessful());

        // When
        CmsResponse<String> consentId_notExist = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID_NOT_EXIST, buildAccess());
        // Then
        assertFalse(consentId_notExist.isSuccessful());
    }

    @Test
    public void updateAccountAccessByIdWithResponse_noEntity_shouldReturnLogicalError() {
        // Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(aisConsentJpaRepository.findByExternalId(FINALISED_CONSENT_ID))
            .thenReturn(Optional.of(finalisedConsent));

        // When
        CmsResponse<AisAccountConsent> result = aisConsentService.updateAspspAccountAccessWithResponse(FINALISED_CONSENT_ID, buildAccess());

        // Then
        assertLogicalError(result);
    }

    @Test
    public void updateAccountAccessByIdWithResponse_success() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());
        when(aisConsentRepositoryImpl.verifyAndUpdate(any(AisConsent.class)))
            .thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent))
            .thenReturn(buildSpiAccountConsent());

        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(ACCOUNTS_2);

        // When
        CmsResponse<AisAccountConsent> consentId = aisConsentService.updateAspspAccountAccessWithResponse(EXTERNAL_CONSENT_ID, info);
        // Then
        assertTrue(consentId.isSuccessful());

        // Given
        info = new AisAccountAccessInfo();
        info.setAccounts(ACCOUNTS_1);

        // When
        consentId = aisConsentService.updateAspspAccountAccessWithResponse(EXTERNAL_CONSENT_ID, info);
        // Then
        assertTrue(consentId.isSuccessful());

        // When
        CmsResponse<String> consentId_notExist = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID_NOT_EXIST, buildAccess());
        // Then
        assertFalse(consentId_notExist.isSuccessful());
    }

    @Test
    public void getPsuDataByConsentId_success() {
        // Given
        when(psuDataMapper.mapToPsuIdDataList(anyList()))
            .thenReturn(Collections.singletonList(PSU_ID_DATA));

        // When
        CmsResponse<List<PsuIdData>> psuDataList = aisConsentService.getPsuDataByConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertEquals(PSU_ID_DATA, psuDataList.getPayload().get(0));
    }

    @Test
    public void getPsuDataByConsentId_noPsuData_shouldReturnLogicalError() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<List<PsuIdData>> actual = aisConsentService.getPsuDataByConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertLogicalError(actual);
    }

    @Test
    public void updateMultilevelScaRequired_success() {
        // When
        CmsResponse<Boolean> actual = aisConsentService.updateMultilevelScaRequired(EXTERNAL_CONSENT_ID, true);

        // Then
        assertTrue(actual.getPayload());
    }

    @Test
    public void updateMultilevelScaRequired_noEntity_shouldReturnFalse() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actual = aisConsentService.updateMultilevelScaRequired(EXTERNAL_CONSENT_ID, true);

        // Then
        assertFalse(actual.getPayload());
    }

    @Test
    public void updateConsentStatusById_UpdateFinalisedStatus_Fail() {
        // Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(aisConsentJpaRepository.findByExternalId(FINALISED_CONSENT_ID))
            .thenReturn(Optional.of(finalisedConsent));

        // When
        CmsResponse<Boolean> actual = aisConsentService.updateConsentStatusById(FINALISED_CONSENT_ID, ConsentStatus.EXPIRED);

        // Then
        assertLogicalError(actual);
    }

    @Test
    public void updateConsentStatusById_noActualConsent_shouldReturnFalse() {
        // Given
        AisConsent nonFinalisedConsent = buildFinalisedConsent();
        nonFinalisedConsent.setConsentStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        when(aisConsentJpaRepository.findByExternalId(FINALISED_CONSENT_ID))
            .thenReturn(Optional.of(nonFinalisedConsent));

        // When
        CmsResponse<Boolean> result = aisConsentService.updateConsentStatusById(FINALISED_CONSENT_ID, ConsentStatus.EXPIRED);

        // Then
        assertTrue(result.isSuccessful());
        assertEquals(Boolean.FALSE, result.getPayload());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAndTerminateOldConsentsByNewConsentId_failure_consentNotFound() {
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());

        aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID_NOT_EXIST);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_newConsentRecurringIndicatorIsFalse() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.isOneAccessType())
            .thenReturn(true);

        // When
        CmsResponse<Boolean> result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertFalse(result.getPayload());
        verify(aisConsentJpaRepository, never()).findOldConsentsByNewConsentParams(any(), any(), any(), any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAndTerminateOldConsentsByNewConsentId_failure_wrongConsentData() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.isWrongConsentData())
            .thenReturn(true);

        // When
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_oldConsentsEmpty() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
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
        CmsResponse<Boolean> result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertFalse(result.getPayload());
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
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
        when(aisConsentJpaRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        // When
        CmsResponse<Boolean> result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertTrue(result.getPayload());
        assertEquals(ConsentStatus.REJECTED, oldConsent.getConsentStatus());
        verify(aisConsentJpaRepository).saveAll(oldConsents);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_multilevel_SCA() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
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
        when(aisConsentJpaRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        // When
        CmsResponse<Boolean> result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertTrue(result.getPayload());
        assertEquals(ConsentStatus.REJECTED, oldConsent.getConsentStatus());
        verify(aisConsentJpaRepository).saveAll(oldConsents);

    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_shouldFail_unequalPsuDataLists() {
        // Given
        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);

        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
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
        CmsResponse<Boolean> result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertFalse(result.getPayload());
        verify(aisConsentJpaRepository, never()).save(any(AisConsent.class));
    }

    @Test
    public void checkConsentAndSaveActionLog() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.empty());

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

        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.expireConsent(consent)).thenReturn(consent);

        // When
        aisConsentService.checkConsentAndSaveActionLog(new AisConsentActionRequest(TPP_ID, EXTERNAL_CONSENT_ID, ActionStatus.SUCCESS, "/uri", false, null, null));

        // Then
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    public void getConsentStatusById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        AisConsent consent = buildUsedNonRecurringConsent();

        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.expireConsent(consent)).thenReturn(consent);

        // When
        CmsResponse<ConsentStatus> consentStatusById = aisConsentService.getConsentStatusById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(consentStatusById.isSuccessful());
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    public void getConsentStatusById_noEntity_shouldReturnLogicalError() {
        // Given
        when(aisConsentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<ConsentStatus> actual = aisConsentService.getConsentStatusById(EXTERNAL_CONSENT_ID);

        // Then
        assertLogicalError(actual);
    }

    private void assertLogicalError(CmsResponse actual) {
        assertFalse(actual.isSuccessful());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
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

    private AisConsentAuthorization buildAisConsentAuthorisation() {
        AisConsentAuthorization aisConsentAuthorisation = new AisConsentAuthorization();
        aisConsentAuthorisation.setConsent(aisConsent);
        aisConsentAuthorisation.setExternalId(AUTHORISATION_ID);
        aisConsentAuthorisation.setPsuData(PSU_DATA);
        aisConsentAuthorisation.setScaStatus(ScaStatus.RECEIVED);
        return aisConsentAuthorisation;
    }

    @NotNull
    private AspspSettings getAspspSettings() {
        return getAspspSettings(1);
    }

    @NotNull
    private AspspSettings getAspspSettings(int maxConsentValidityDays) {
        return new AspspSettings(new AisAspspProfileSetting(new ConsentTypeSetting(false, false, false, 0, 0, maxConsentValidityDays, false), null, null, null, null), null, null, null);
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
        aisConsent.setValidUntil(validUntil);
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setAuthorizations(aisConsentAuthorisationList);
        aisConsent.setPsuDataList(psuDataList);
        AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();
        authorisationTemplate.setRedirectUri(REDIRECT_URI);
        authorisationTemplate.setNokRedirectUri(NOK_REDIRECT_URI);
        aisConsent.setAuthorisationTemplate(authorisationTemplate);
        aisConsent.setOwnerNameType(AdditionalAccountInformationType.DEDICATED_ACCOUNTS);
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
        request.setNotificationSupportedModes(MODES);
        return request;
    }

    private AisAccountAccessInfo buildAccess() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(buildAccountsInfo());
        info.setAccountAdditionalInformationAccess(new AccountAdditionalInformationAccess(Collections.singletonList(AccountInfo.builder().build())));
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
                                     null, null, 0,
                                     null, null,
                                     false, false, null, null, null, null, false, Collections.emptyList(), Collections.emptyMap(), OffsetDateTime.now(),
                                     OffsetDateTime.now());

    }

    private AisConsent buildFinalisedConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setValidUntil(LocalDate.now());
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

