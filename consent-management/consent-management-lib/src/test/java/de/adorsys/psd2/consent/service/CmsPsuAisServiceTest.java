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

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuAisServiceInternal;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuAisServiceTest {

    @InjectMocks
    CmsPsuAisServiceInternal cmsPsuAisService;
    @Mock
    private AisConsentRepository aisConsentRepository;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    @Mock
    private PsuDataRepository psuDataRepository;
    @Mock
    private PsuDataMapper psuDataMapper;

    @Mock
    private AisConsentAuthorization mockAisConsentAuthorization;
    @Mock
    private AisAccountConsent mockAisAccountConsent;
    @Mock
    private TppInfo tppInfo;
    @Mock
    private AisConsentAuthorizationSpecification aisConsentAuthorizationSpecification;
    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private AisConsentService aisConsentService;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private CmsPsuService cmsPsuService;

    private AisConsent aisConsent;
    private List<AisConsent> aisConsents;
    private AisAccountConsent aisAccountConsent;
    private AisConsentAuthorization aisConsentAuthorization;
    private PsuIdData psuIdData;
    private PsuIdData psuIdDataWrong;
    private PsuData psuData;
    private final long CONSENT_ID = 1;
    private final String PSU_ID = "987654321";
    private final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private final String AUTHORISATION_ID_NOT_EXIST = "248eae68-e4fa-4d43-8b3f-2ae2b584cdd9";
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_AUTHORISATION_ID = "6b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String TPP_OK_REDIRECT_URI = "Mock tppOkRedirectUri";
    private static final String TPP_NOK_REDIRECT_URI = "Mock tppNokRedirectUri";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    @Before
    public void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        psuData = buildPsuData(PSU_ID);
        aisConsent = buildConsent();
        psuIdDataWrong = buildPsuIdData("wrong");
        aisAccountConsent = buildSpiAccountConsent();
        aisConsentAuthorization = buildAisConsentAuthorisation();
        aisConsents = buildAisConsents();

        when(aisConsentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(aisAccountConsent);
        when(aisConsentAuthorisationRepository.save(aisConsentAuthorization)).thenReturn(aisConsentAuthorization);
        when(psuDataRepository.save(psuData)).thenReturn(psuData);
    }

    @Test
    public void updatePsuDataInConsentSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(aisConsentAuthorization);
        when(psuDataMapper.mapToPsuData(psuIdData))
            .thenReturn(psuData);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), anyList()))
            .thenReturn(Optional.of(psuData));

        // Then
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(updatePsuDataInConsent);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updatePsuDataInConsentFail() {
        // When
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        // Then
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertFalse(updatePsuDataInConsent);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);

        // Then
        Optional<AisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), aisAccountConsent);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentFail() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(null);

        // Then
        Optional<AisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(!consent.isPresent());
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatusSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(aisConsentAuthorization);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID, ScaStatus.STARTED, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatusFail() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID_NOT_EXIST, ScaStatus.STARTED, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentsForPsuSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class))).thenReturn(aisConsents);

        // Then
        List<AisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertEquals(consentsForPsu.size(), aisConsents.size());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentsForPsuFail() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // Then
        List<AisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(consentsForPsu.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceId(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void confirmConsentSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);
        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(anyString())).thenReturn(true);

        AisConsent aisConsentValid = buildConsentByStatus(ConsentStatus.VALID);
        when(aisConsentRepository.save(aisConsentValid)).thenReturn(aisConsentValid);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void confirmConsentFail() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void rejectConsentSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);

        AisConsent aisConsentRejected = buildConsentByStatus(ConsentStatus.REJECTED);
        when(aisConsentRepository.save(aisConsentRejected)).thenReturn(aisConsentRejected);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void rejectConsentFail() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void revokeConsentSuccess() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);

        AisConsent aisConsentRevoked = buildConsentByStatus(ConsentStatus.REVOKED_BY_PSU);
        when(aisConsentRepository.save(aisConsentRevoked)).thenReturn(aisConsentRevoked);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void revokeConsentFail() {
        // When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Assert
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void confirmConsent_FinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();

        //When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(finalisedConsent);

        boolean result = cmsPsuAisService.confirmConsent(psuIdData, FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        //Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void rejectConsent_FinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();

        //When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(finalisedConsent);

        boolean result = cmsPsuAisService.rejectConsent(psuIdData, FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        //Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void revokeConsent_FinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();

        //When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(finalisedConsent);

        boolean result = cmsPsuAisService.revokeConsent(psuIdData, FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        //Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAuthorisationStatus_FinalisedStatus_Fail() {
        //Given
        AisConsent consent = buildConsent();
        AisConsentAuthorization finalisedAuthorisation = buildFinalisedAuthorisation();

        //When
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(consent);
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(finalisedAuthorisation);

        boolean result = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED, DEFAULT_SERVICE_INSTANCE_ID);

        //Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(FINALISED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentByRedirectId_Fail_AuthorisationNotFound() {
        //When
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(null);

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(consentResponseOptional.isPresent());
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentByRedirectId_Fail_AuthorisationExpire() {
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(mockAisConsentAuthorization);
        when(mockAisConsentAuthorization.isNotExpired()).thenReturn(false);
        when(mockAisConsentAuthorization.getScaStatus()).thenReturn(ScaStatus.RECEIVED);
        doReturn(mockAisConsentAuthorization).when(aisConsentAuthorisationRepository).save(mockAisConsentAuthorization);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(aisConsent);

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertTrue(consentResponseOptional.isPresent());
        assertEquals(consentResponseOptional.get(), new CmsAisConsentResponse(TPP_NOK_REDIRECT_URI));
    }

    @Test
    public void getConsentByRedirectId_Fail_NullAisConsent() {
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(mockAisConsentAuthorization);
        when(mockAisConsentAuthorization.isNotExpired()).thenReturn(true);
        when(mockAisConsentAuthorization.getScaStatus()).thenReturn(ScaStatus.RECEIVED);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(null);

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(consentResponseOptional.isPresent());
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void getConsentByRedirectId_Success() {
        //noinspection unchecked
        when(aisConsentAuthorisationRepository.findOne(any(Specification.class))).thenReturn(mockAisConsentAuthorization);
        when(mockAisConsentAuthorization.isNotExpired()).thenReturn(true);
        when(mockAisConsentAuthorization.getScaStatus()).thenReturn(ScaStatus.RECEIVED);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(aisConsent);
        when(mockAisConsentAuthorization.getPsuData()).thenReturn(psuData);
        when(aisConsentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(mockAisAccountConsent);
        when(mockAisAccountConsent.getTppInfo()).thenReturn(tppInfo);
        when(tppInfo.getTppRedirectUri()).thenReturn(buildTppRedirectUri());
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertTrue(consentResponseOptional.isPresent());
        CmsAisConsentResponse cmsAisConsentResponse = consentResponseOptional.get();
        assertEquals(mockAisAccountConsent, cmsAisConsentResponse.getAccountConsent());
        assertEquals(AUTHORISATION_ID, cmsAisConsentResponse.getAuthorisationId());
        assertEquals(TPP_NOK_REDIRECT_URI, cmsAisConsentResponse.getTppNokRedirectUri());
        assertEquals(TPP_OK_REDIRECT_URI, cmsAisConsentResponse.getTppOkRedirectUri());
        verify(aisConsentAuthorizationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void updateAccountAccessInConsent_Success() {
        //Given
        int frequencyPerDay = 777;
        String iban = "DE67597874259856475273";
        Currency currency = Currency.getInstance("EUR");
        LocalDate validUntil = LocalDate.now();
        AccountReference accountReference = getAccountReference(iban, currency);
        AisAccountAccess aisAccountAccess = getAisAccountAccess(accountReference);
        Set<AspspAccountAccess> aspspAccountAccesses = getAspspAccountAccesses(aisAccountAccess);
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, validUntil, frequencyPerDay);
        ArgumentCaptor<AisConsent> argument = ArgumentCaptor.forClass(AisConsent.class);
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(aisConsent);
        when(aisConsentMapper.mapAspspAccountAccesses(aisAccountAccess)).thenReturn(aspspAccountAccesses);
        //When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, "");
        //Then
        verify(aisConsentRepository).save(argument.capture());
        List<AspspAccountAccess> aspspAccountAccessesChecked = argument.getValue().getAspspAccountAccesses();
        assertSame(aspspAccountAccessesChecked.size(), aspspAccountAccesses.size());
        assertSame(aspspAccountAccessesChecked.get(0).getAccountIdentifier(), iban);
        assertSame(aspspAccountAccessesChecked.get(0).getCurrency(), currency);
        assertSame(argument.getValue().getExpireDate(), validUntil);
        assertEquals(argument.getValue().getAllowedFrequencyPerDay(), frequencyPerDay);
        assertEquals(getUsageCounter(argument.getValue()), frequencyPerDay);
        assertTrue(saved);
    }

    private int getUsageCounter(AisConsent aisConsent) {
        Integer usage = aisConsent.getUsages().stream()
                            .filter(consent -> LocalDate.now().isEqual(consent.getUsageDate()))
                            .findFirst()
                            .map(AisConsentUsage::getUsage)
                            .orElse(0);

        return Math.max(aisConsent.getAllowedFrequencyPerDay() - usage, 0);
    }

    @Test
    public void getPsuDataAuthorisations_Success() {
        // Given
        AisConsent consent = buildAisConsentWithFinalisedAuthorisation();
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(consent);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertThat(actualResult.get().size()).isEqualTo(1);
        assertThat(actualResult.get().get(0).getScaStatus()).isEqualTo(ScaStatus.FINALISED);
    }

    @Test
    public void getPsuDataAuthorisationsEmptyPsuData_Success() {
        // Given
        AisConsent consent = buildAisConsentWithFinalisedAuthorisationNoPsuData();
        //noinspection unchecked
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(consent);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().isEmpty());
    }

    @Test
    public void saveAccountAccessInConsent_Consent_Finalised_Failed() {
        //Given
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1);
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(buildFinalisedConsent());
        //When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, "");
        //Then
        assertFalse(saved);
    }

    @Test
    public void saveAccountAccessInConsent_Consent_Unknown_Failed() {
        //Given
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1);
        when(aisConsentRepository.findOne(any(Specification.class))).thenReturn(buildFinalisedConsent());
        //When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID_NOT_EXIST, accountAccessRequest, "");
        //Then
        assertFalse(saved);
    }

    private Set<AspspAccountAccess> getAspspAccountAccesses(AisAccountAccess aisAccountAccess) {
        Set<AspspAccountAccess> aspspAccountAccesses = new HashSet<>();
        aspspAccountAccesses.add(mapToAccountInfo(aisAccountAccess.getAccounts().get(0), TypeAccess.ACCOUNT));
        aspspAccountAccesses.add(mapToAccountInfo(aisAccountAccess.getBalances().get(0), TypeAccess.BALANCE));
        aspspAccountAccesses.add(mapToAccountInfo(aisAccountAccess.getTransactions().get(0), TypeAccess.TRANSACTION));
        return aspspAccountAccesses;
    }

    private AspspAccountAccess mapToAccountInfo(AccountReference accountReference, TypeAccess typeAccess) {
        AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();

        return new AspspAccountAccess(selector.getAccountValue(),
                                      typeAccess,
                                      selector.getAccountReferenceType(),
                                      accountReference.getCurrency(),
                                      accountReference.getResourceId(),
                                      accountReference.getAspspAccountId());
    }

    private AisConsent buildAisConsentWithFinalisedAuthorisation() {
        AisConsent consent = buildConsent();
        AisConsentAuthorization finalisedAuthorisation = buildFinalisedAuthorisation();
        finalisedAuthorisation.setPsuData(psuData);
        consent.setAuthorizations(Collections.singletonList(finalisedAuthorisation));
        return consent;
    }

    private AisConsent buildAisConsentWithFinalisedAuthorisationNoPsuData() {
        AisConsent consent = buildConsent();
        consent.setAuthorizations(Collections.singletonList(buildFinalisedAuthorisation()));
        return consent;
    }

    private AisConsent buildFinalisedConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now());
        aisConsent.setConsentStatus(ConsentStatus.REJECTED);
        return aisConsent;
    }

    private List<AisConsent> buildAisConsents() {
        return Arrays.asList(aisConsent, aisConsent, aisConsent);
    }

    private AisConsentAuthorization buildAisConsentAuthorisation() {
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setExternalId(AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        aisConsentAuthorization.setConsent(buildConsent());
        return aisConsentAuthorization;
    }

    private AisConsentAuthorization buildFinalisedAuthorisation() {
        AisConsentAuthorization aisConsentAuthorization = new AisConsentAuthorization();
        aisConsentAuthorization.setExternalId(FINALISED_AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.FINALISED);
        return aisConsentAuthorization;
    }

    private AisConsent buildConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now().plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuDataList(Collections.singletonList(psuData));
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setTppInfo(buildTppInfoEntity());
        return aisConsent;
    }

    private AisConsent buildConsentByStatus(ConsentStatus status) {
        AisConsent aisConsent = buildConsent();
        aisConsent.setConsentStatus(status);
        return aisConsent;
    }

    private PsuData buildPsuData(String psuId) {
        return new PsuData(psuId, "", "", "");
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, "", "", "");
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
                                     null, false,
                                     null, 0,
                                     null, null,
                                     false, false, null, null, null, false, Collections.emptyList(), 0, OffsetDateTime.now(),
                                     OffsetDateTime.now());
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setRedirectUri(TPP_OK_REDIRECT_URI);
        tppInfoEntity.setNokRedirectUri(TPP_NOK_REDIRECT_URI);
        return tppInfoEntity;
    }

    private TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri(TPP_OK_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    }

    private AisAccountAccess getAisAccountAccess(AccountReference accountReference) {
        return new AisAccountAccess(
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference),
            Collections.singletonList(accountReference), null, null);
    }

    private AccountReference getAccountReference(String iban, Currency currency) {
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(iban);
        accountReference.setCurrency(currency);
        return accountReference;
    }
}
