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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.repository.AisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuAisServiceTest {

    @InjectMocks
    CmsPsuAisServiceInternal cmsPsuAisService;
    @Mock
    private AisConsentRepository aisConsentRepository;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AisConsentAuthorizationRepository aisConsentAuthorizationRepository;
    @Mock
    private PsuDataRepository psuDataRepository;
    @Spy
    private PsuDataMapper psuDataMapper;
    @Mock
    private SecurityDataService securityDataService;

    @Mock
    private AisConsentAuthorization mockAisConsentAuthorization;

    private AisConsent aisConsent;
    private List<AisConsent> aisConsents;
    private AisAccountConsent aisAccountConsent;
    private AisConsentAuthorization aisConsentAuthorization;
    private PsuIdData psuIdData;
    private PsuIdData psuIdDataWrong;
    private PsuData psuData;
    private CmsAisConsentResponse cmsAisConsentResponse;
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

    @Before
    public void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        psuData = buildPsuData();
        aisConsent = buildConsent();
        psuIdDataWrong = buildPsuIdData("wrong");
        aisAccountConsent = buildSpiAccountConsent();
        aisConsentAuthorization = buildAisConsentAuthorisation();
        aisConsents = buildAisConsents();
        cmsAisConsentResponse = buildCmsAisConsentResponse(aisAccountConsent, AUTHORISATION_ID, TPP_OK_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(aisConsentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(aisAccountConsent);
        when(aisConsentAuthorizationRepository.save(aisConsentAuthorization)).thenReturn(aisConsentAuthorization);
        when(aisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(aisConsentAuthorization));
        when(aisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aisConsent));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(aisConsentRepository.findByPsuDataPsuId(PSU_ID)).thenReturn(aisConsents);
        when(psuDataRepository.save(psuData)).thenReturn(psuData);
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
    }

    @Test
    public void updatePsuDataInConsentSuccess() {
        // When
        // Then
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertTrue(updatePsuDataInConsent);
    }

    @Test
    public void updatePsuDataInConsentFail() {
        // When
        // Then
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST);
        // Assert
        assertFalse(updatePsuDataInConsent);
    }

    @Test
    public void getConsentSuccess() {
        // When
        // Then
        Optional<AisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), aisAccountConsent);
    }

    @Test
    public void getConsentFail() {
        // When
        // Then
        Optional<AisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST);
        // Assert
        assertTrue(!consent.isPresent());
    }

    @Test
    public void updateAuthorisationStatusSuccess() {
        // When
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID, ScaStatus.STARTED);
        // Assert
        assertTrue(updateAuthorisationStatus);
    }

    @Test
    public void updateAuthorisationStatusFail() {
        // When
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID_NOT_EXIST, ScaStatus.STARTED);
        // Assert
        assertFalse(updateAuthorisationStatus);
    }

    @Test
    public void getConsentsForPsuSuccess() {
        // When
        // Then
        List<AisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdData);
        // Assert
        assertEquals(consentsForPsu.size(), aisConsents.size());
    }

    @Test
    public void getConsentsForPsuFail() {
        // When
        // Then
        List<AisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdDataWrong);
        // Assert
        assertTrue(consentsForPsu.isEmpty());
    }

    @Test
    public void confirmConsentSuccess() {
        // When
        AisConsent aisConsentValid = buildConsentByStatus(ConsentStatus.VALID);
        when(aisConsentRepository.save(aisConsentValid)).thenReturn(aisConsentValid);
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertTrue(updateAuthorisationStatus);
    }

    @Test
    public void confirmConsentFail() {
        // When
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertFalse(updateAuthorisationStatus);
    }

    @Test
    public void rejectConsentSuccess() {
        // When
        AisConsent aisConsentRejected = buildConsentByStatus(ConsentStatus.REJECTED);
        when(aisConsentRepository.save(aisConsentRejected)).thenReturn(aisConsentRejected);
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertTrue(updateAuthorisationStatus);
    }

    @Test
    public void rejectConsentFail() {
        // When
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertFalse(updateAuthorisationStatus);
    }

    @Test
    public void revokeConsentSuccess() {
        // When
        AisConsent aisConsentRevoked = buildConsentByStatus(ConsentStatus.REVOKED_BY_PSU);
        when(aisConsentRepository.save(aisConsentRevoked)).thenReturn(aisConsentRevoked);
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertTrue(updateAuthorisationStatus);
    }

    @Test
    public void revokeConsentFail() {
        // When
        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Assert
        assertFalse(updateAuthorisationStatus);
    }

    @Test
    public void confirmConsent_FinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(securityDataService.decryptId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(FINALISED_CONSENT_ID));
        when(aisConsentRepository.findByExternalId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(finalisedConsent));

        //When
        boolean result = cmsPsuAisService.confirmConsent(psuIdData, FINALISED_CONSENT_ID);

        //Then
        assertFalse(result);
    }

    @Test
    public void rejectConsent_FinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(securityDataService.decryptId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(FINALISED_CONSENT_ID));
        when(aisConsentRepository.findByExternalId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(finalisedConsent));

        //When
        boolean result = cmsPsuAisService.rejectConsent(psuIdData, FINALISED_CONSENT_ID);

        //Then
        assertFalse(result);
    }

    @Test
    public void revokeConsent_FinalisedStatus_Fail() {
        //Given
        AisConsent finalisedConsent = buildFinalisedConsent();
        when(securityDataService.decryptId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(FINALISED_CONSENT_ID));
        when(aisConsentRepository.findByExternalId(FINALISED_CONSENT_ID)).thenReturn(Optional.of(finalisedConsent));

        //When
        boolean result = cmsPsuAisService.revokeConsent(psuIdData, FINALISED_CONSENT_ID);

        //Then
        assertFalse(result);
    }

    @Test
    public void updateAuthorisationStatus_FinalisedStatus_Fail() {
        //Given
        AisConsent consent = buildConsent();
        AisConsentAuthorization finalisedAuthorisation = buildFinalisedAuthorisation();
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(consent));
        when(aisConsentAuthorizationRepository.findByExternalId(FINALISED_AUTHORISATION_ID)).thenReturn(Optional.of(finalisedAuthorisation));

        //When
        boolean result = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED);

        //Then
        assertFalse(result);
    }

    @Test
    public void getConsentByRedirectId_Fail_AuthorisationNotFound() {
        when(aisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.empty());

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.getConsentByRedirectId(psuIdData, AUTHORISATION_ID);

        assertFalse(consentResponseOptional.isPresent());
    }

    @Test
    public void getConsentByRedirectId_Fail_AuthorisationExpire() {
        when(aisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(mockAisConsentAuthorization));
        when(mockAisConsentAuthorization.isExpired()).thenReturn(true);
        doReturn(mockAisConsentAuthorization).when(aisConsentAuthorizationRepository).save(mockAisConsentAuthorization);

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.getConsentByRedirectId(psuIdData, AUTHORISATION_ID);

        assertFalse(consentResponseOptional.isPresent());
        verify(aisConsentAuthorizationRepository).save(mockAisConsentAuthorization);
        verify(mockAisConsentAuthorization).setScaStatus(ScaStatus.FAILED);
    }

    @Test
    public void getConsentByRedirectId_Fail_NullAisConsent() {
        when(aisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(mockAisConsentAuthorization));
        when(mockAisConsentAuthorization.isExpired()).thenReturn(false);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(null);

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.getConsentByRedirectId(psuIdData, AUTHORISATION_ID);

        assertFalse(consentResponseOptional.isPresent());
    }

    @Test
    public void getConsentByRedirectId_Success() {
        when(aisConsentAuthorizationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(mockAisConsentAuthorization));
        when(mockAisConsentAuthorization.isExpired()).thenReturn(false);
        when(mockAisConsentAuthorization.getConsent()).thenReturn(aisConsent);
        when(aisConsentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(aisAccountConsent);
        when(aisConsentMapper.mapToCmsAisConsentResponse(aisAccountConsent, AUTHORISATION_ID, TPP_OK_REDIRECT_URI, TPP_NOK_REDIRECT_URI)).thenReturn(Optional.of(cmsAisConsentResponse));

        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.getConsentByRedirectId(psuIdData, AUTHORISATION_ID);

        assertTrue(consentResponseOptional.isPresent());
        CmsAisConsentResponse cmsAisConsentResponse = consentResponseOptional.get();
        assertEquals(aisAccountConsent, cmsAisConsentResponse.getAccountConsent());
        assertEquals(AUTHORISATION_ID, cmsAisConsentResponse.getAuthorisationId());
        assertEquals(TPP_NOK_REDIRECT_URI, cmsAisConsentResponse.getTppNokRedirectUri());
        assertEquals(TPP_OK_REDIRECT_URI, cmsAisConsentResponse.getTppOkRedirectUri());
    }

    private CmsAisConsentResponse buildCmsAisConsentResponse(AisAccountConsent aisAccountConsent, String redirectId, String tppOkRedirectUri, String tppNokRedirectUri) {
        return new CmsAisConsentResponse(aisAccountConsent, redirectId, tppOkRedirectUri, tppNokRedirectUri);
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
}
