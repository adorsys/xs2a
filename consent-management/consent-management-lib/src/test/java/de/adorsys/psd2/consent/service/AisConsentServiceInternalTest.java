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
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
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
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceInternalTest {
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String PSU_ID = "psu-id-1";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final PsuData PSU_DATA = new PsuData(PSU_ID, null, null, null);
    private static final byte[] ENCRYPTED_CONSENT_DATA = "test data".getBytes();
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_NUMBER = "Test Authorisation Number";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorisation id";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    private static final String AUTHENTICATION_METHOD_ID = "Method id";
    private static final String WRONG_AUTHENTICATION_METHOD_ID = "Wrong method id";

    private AisConsent aisConsent;
    private AisConsentAuthorization aisConsentAuthorisation;
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
    SecurityDataService securityDataService;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AisConsentAuthorisationRepository aisConsentAuthorisationRepository;

    @Mock
    private AisConsent aisConsentMocked;
    @Mock
    private TppInfoEntity tppInfoMocked;
    @Mock
    private PsuData psuDataMocked;
    @Mock
    private PsuData anotherPsuDataMocked;
    @Mock
    private CmsPsuService cmsPsuService;

    @Mock
    private ScaMethodMapper scaMethodMapper;

    @Before
    public void setUp() {
        aisConsentAuthorisation = buildAisConsentAuthorisation(AUTHORISATION_ID, ScaStatus.STARTED);
        aisConsentAuthorisationList.add(aisConsentAuthorisation);
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
    public void getAisAccountConsentById_checkAndUpdateOnExpirationInvoked() {
        // When
        ArgumentCaptor<AisConsent> argumentCaptor = ArgumentCaptor.forClass(AisConsent.class);
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, Collections.singletonList(psuDataMocked), LocalDate.now().minusDays(1));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent)).thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());

        // Then
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);
        verify(aisConsentRepository).save(argumentCaptor.capture());

        // Assert
        assertTrue(retrievedConsent.isPresent());
        assertEquals(ConsentStatus.EXPIRED, argumentCaptor.getValue().getConsentStatus());
    }

    @Test
    public void getAisAccountConsentById_checkAndUpdateOnExpirationNotInvoked() {
        // When
        AisConsent aisConsent = buildConsent(EXTERNAL_CONSENT_ID, Collections.singletonList(psuDataMocked), LocalDate.now());
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(aisConsent)).thenReturn(aisConsent);
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());

        // Then
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Assert
        assertTrue(retrievedConsent.isPresent());
        verify(aisConsentRepository, never()).save(any(AisConsent.class));
    }

    @Test
    public void shouldReturnExternalId_WhenCreateConsentIsCalled() {
        // When
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());

        // Then
        Optional<String> externalId = aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Assert
        assertTrue(externalId.isPresent());
        assertThat(externalId.get(), is(equalTo(aisConsent.getExternalId())));
    }

    @Test
    public void createConsent_AdjustValidUntil_ZeroLifeTime() {
        // When
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int consentLifeTime = 0;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(consentLifeTime));
        int validDays = 5;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // Then
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Assert
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(argument.getValue().getExpireDate(), validUntil);
    }

    @Test
    public void createConsent_AdjustValidUntil_NoAdjustment() {
        // When
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int consentLifeTime = 10;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(consentLifeTime));
        int validDays = 5;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // Then
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Assert
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(argument.getValue().getExpireDate(), validUntil);
    }

    @Test
    public void createConsent_AdjustValidUntil_AdjustmentToLifeTime() {
        // When
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);

        int consentLifeTime = 5;
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings(consentLifeTime));
        int validDays = 10;
        LocalDate validUntil = LocalDate.now().plusDays(validDays - 1);

        // Then
        aisConsentService.createConsent(buildCorrectCreateAisConsentRequest(validUntil));

        // Assert
        verify(aisConsentRepository).save(argument.capture());
        assertEquals(argument.getValue().getExpireDate(), LocalDate.now().plusDays(consentLifeTime - 1));
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
        Optional<String> consentId = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID, info);
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
        consentId = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID, info);
        // Assert
        assertTrue(consentId.isPresent());

        // Then
        Optional<String> consentId_notExist = aisConsentService.updateAspspAccountAccess(EXTERNAL_CONSENT_ID_NOT_EXIST, buildAccess());
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
        List<AisConsentAuthorization> authorisations = Collections.singletonList(buildAisConsentAuthorisation(AUTHORISATION_ID, SCA_STATUS));
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
        List<AisConsentAuthorization> authorisations = Collections.singletonList(buildAisConsentAuthorisation(WRONG_AUTHORISATION_ID, SCA_STATUS));
        AisConsent consent = buildConsentWithAuthorisations(EXTERNAL_CONSENT_ID, authorisations);
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));

        // When
        Optional<ScaStatus> actual = aisConsentService.getAuthorisationScaStatus(EXTERNAL_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void createAuthorizationWithClosingPreviousAuthorisations_success() {
        //Given
        ArgumentCaptor<AisConsentAuthorization> argument = ArgumentCaptor.forClass(AisConsentAuthorization.class);
        //noinspection unchecked
        ArgumentCaptor<List<AisConsentAuthorization>> failedAuthorisationsArgument = ArgumentCaptor.forClass((Class) List.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(aisConsentAuthorisationRepository.save(any(AisConsentAuthorization.class))).thenReturn(aisConsentAuthorisation);
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(any(), any())).thenReturn(Collections.singletonList(PSU_DATA));

        AisConsentAuthorizationRequest aisConsentAuthorisationRequest = new AisConsentAuthorizationRequest();
        aisConsentAuthorisationRequest.setPsuData(PSU_ID_DATA);
        aisConsentAuthorisationRequest.setScaStatus(aisConsentAuthorisation.getScaStatus());

        // When
        Optional<String> actual = aisConsentService.createAuthorization(EXTERNAL_CONSENT_ID, aisConsentAuthorisationRequest);

        // Then
        assertTrue(actual.isPresent());
        verify(aisConsentAuthorisationRepository).save(argument.capture());
        assertSame(argument.getValue().getScaStatus(), ScaStatus.STARTED);

        verify(aisConsentAuthorisationRepository).save(failedAuthorisationsArgument.capture());
        List<AisConsentAuthorization> failedAuthorisations = failedAuthorisationsArgument.getValue();
        Set<ScaStatus> scaStatuses = failedAuthorisations.stream()
                                         .map(AisConsentAuthorization::getScaStatus)
                                         .collect(Collectors.toSet());
        assertEquals(scaStatuses.size(), 1);
        assertTrue(scaStatuses.contains(ScaStatus.FAILED));

    }

    @Test(expected = IllegalArgumentException.class)
    public void findAndTerminateOldConsentsByNewConsentId_failure_consentNotFound() {
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());

        aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID_NOT_EXIST);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_newConsentRecurringIndicatorIsFalse() {
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.isOneAccessType())
            .thenReturn(true);

        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        assertFalse(result);
        verify(aisConsentRepository, never()).findOldConsentsByNewConsentParams(any(), any(), any(), any(), any(), any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAndTerminateOldConsentsByNewConsentId_failure_wrongConsentData() {
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.isWrongConsentData())
            .thenReturn(true);

        aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success_oldConsentsEmpty() {
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(aisConsentMocked));

        when(aisConsentMocked.getTppInfo())
            .thenReturn(tppInfoMocked);

        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);

        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);

        when(tppInfoMocked.getAuthorityId())
            .thenReturn(AUTHORISATION_ID);

        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);

        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        when(aisConsentRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, AUTHORISATION_ID, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.VALID)))
            .thenReturn(Collections.emptyList());

        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        assertFalse(result);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_success() {
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

        when(tppInfoMocked.getAuthorityId())
            .thenReturn(AUTHORISATION_ID);

        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);

        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        when(cmsPsuService.isPsuDataListEqual(psuDataList, psuDataList))
            .thenReturn(true);

        AisConsent oldConsent = buildConsent(EXTERNAL_CONSENT_ID_NOT_EXIST);
        List<AisConsent> oldConsents = Collections.singletonList(oldConsent);
        when(aisConsentRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, AUTHORISATION_ID, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        when(aisConsentRepository.save(oldConsents))
            .thenReturn(oldConsents);

        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        assertTrue(result);
        assertEquals(ConsentStatus.TERMINATED_BY_TPP, oldConsent.getConsentStatus());
        verify(aisConsentRepository).save(oldConsents);
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId_shouldFail_unequalPsuDataLists() {
        // Given
        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);
        List<PsuData> anotherPsuDataList = Collections.singletonList(psuDataMocked);

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
        when(tppInfoMocked.getAuthorityId())
            .thenReturn(AUTHORISATION_ID);
        when(aisConsentMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);
        when(aisConsentMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);
        when(cmsPsuService.isPsuDataListEqual(psuDataList, anotherPsuDataList))
            .thenReturn(false);

        AisConsent oldConsent = buildConsent(EXTERNAL_CONSENT_ID_NOT_EXIST, Collections.singletonList(anotherPsuDataMocked));
        List<AisConsent> oldConsents = Collections.singletonList(oldConsent);
        when(aisConsentRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, AUTHORISATION_ID, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        when(aisConsentRepository.save(oldConsents))
            .thenReturn(oldConsents);

        // When
        boolean result = aisConsentService.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(result);
        verify(aisConsentRepository, never()).save(any(AisConsent.class));
    }

    @Test
    public void isAuthenticationMethodDecoupled_success_decoupled() {
        // Given
        List<ScaMethod> methods = Collections.singletonList(buildScaMethod(true));
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(methods)));

        // When
        boolean actualResult = aisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResult);
    }

    @Test
    public void isAuthenticationMethodDecoupled_success_notDecoupled() {
        // Given
        List<ScaMethod> methods = Collections.singletonList(buildScaMethod(false));
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(methods)));

        // When
        boolean actualResult = aisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void isAuthenticationMethodDecoupled_failure_wrongMethodId() {
        // Given
        List<ScaMethod> methods = Collections.singletonList(buildScaMethod(true));
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(methods)));

        // When
        boolean actualResult = aisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, WRONG_AUTHENTICATION_METHOD_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void isAuthenticationMethodDecoupled_failure_wrongAuthorisationId() {
        // Given
        when(aisConsentAuthorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        boolean actualResult = aisConsentService.isAuthenticationMethodDecoupled(WRONG_AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void isAuthenticationMethodDecoupled_failure_noMethodsPresent() {
        // Given
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisationWithMethods(Collections.emptyList())));

        // When
        boolean actualResult = aisConsentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertFalse(actualResult);
    }

    @Test
    public void saveAuthenticationMethods_success() {
        // Given
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod(true));
        List<ScaMethod> scaMethods = Collections.singletonList(buildScaMethod(true));
        when(scaMethodMapper.mapToScaMethods(cmsScaMethods)).thenReturn(scaMethods);

        ArgumentCaptor<AisConsentAuthorization> authorisationArgumentCaptor = ArgumentCaptor.forClass(AisConsentAuthorization.class);
        when(aisConsentAuthorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorisation(AUTHORISATION_ID, SCA_STATUS)));

        // When
        boolean actualResult = aisConsentService.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actualResult);
        verify(aisConsentAuthorisationRepository, times(1)).save(authorisationArgumentCaptor.capture());
        assertEquals(authorisationArgumentCaptor.getValue().getAvailableScaMethods(), scaMethods);
    }

    @Test
    public void saveAuthenticationMethods_failure_wrongAuthorisationId() {
        // Given
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod(true));

        when(aisConsentAuthorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        boolean actualResult = aisConsentService.saveAuthenticationMethods(WRONG_AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertFalse(actualResult);
        verify(aisConsentAuthorisationRepository, never()).save(any(AisConsentAuthorization.class));
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
                                 false, false, false, false, false, 1,
                                 null, 1, 1, null, 1, false, false, false, false, null);
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

    private AisConsent buildConsentWithAuthorisations(String externalId, List<AisConsentAuthorization> authorisations) {
        AisConsent aisConsent = buildConsent(externalId);
        aisConsent.setAuthorizations(authorisations);
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
                                             .currency("EUR")
                                             .build());
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
                                     null, false,
                                     null, 0,
                                     null, null,
                                     false, false, null, null, null, false, Collections.emptyList(), 0, OffsetDateTime.now(),
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

    private AisConsentAuthorization buildAisConsentAuthorisationWithMethods(List<ScaMethod> scaMethods) {
        AisConsentAuthorization authorisation = buildAisConsentAuthorisation(AUTHORISATION_ID, SCA_STATUS);
        authorisation.setAvailableScaMethods(scaMethods);
        return authorisation;
    }

    private ScaMethod buildScaMethod(boolean decoupled) {
        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        scaMethod.setDecoupled(decoupled);
        return scaMethod;
    }

    private CmsScaMethod buildCmsScaMethod(boolean decoupled) {
        return new CmsScaMethod(AUTHENTICATION_METHOD_ID, decoupled);
    }
}
