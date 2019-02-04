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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuAisServiceInternal;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    @Spy
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
        psuData = buildPsuData();
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
            .byPsuIdIdAndInstanceId(psuIdData.getPsuId(), DEFAULT_SERVICE_INSTANCE_ID);
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
            .byPsuIdIdAndInstanceId(psuIdDataWrong.getPsuId(), DEFAULT_SERVICE_INSTANCE_ID);
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
        aisConsent.setPsuData(psuData);
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

    private PsuData buildPsuData() {
        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData);
        psuData.setId(CONSENT_ID);
        return psuData;

    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, "", "", "");
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
                                     null, false,
                                     null, 0,
                                     null, null,
                                     false, false, null, null, null);
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
}
