/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.ais.ConsentTypeSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceInternalTest {
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b11213-6a96-4941-a220-2da8a4af2c63";
    private static final String PSU_ID = "psu-id-1";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final PsuData PSU_DATA = new PsuData(PSU_ID, null, null, null, null);
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_NUMBER = "Test Authorisation Number";
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String REDIRECT_URI = "http://bank.com/redirect-ok";
    private static final String NOK_REDIRECT_URI = "http://bank.com/redirect-not-ok";
    private static final LocalDate VALID_UNTIL = LocalDate.of(2030, 12, 31);

    private ConsentEntity consentEntity;
    private final List<AuthorisationEntity> authorisationEntities = new ArrayList<>();
    private final JsonReader jsonReader = new JsonReader();
    private final AspspSettings aspspSettings = buildMockAspspSettings();

    @InjectMocks
    private ConsentServiceInternal consentServiceInternal;

    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private ConsentEntity consentEntityMocked;
    @Mock
    private TppInfoEntity tppInfoMocked;
    @Mock
    private PsuData psuDataMocked;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private TppInfoRepository tppInfoRepository;
    @Mock
    private AisConsentLazyMigrationService aisConsentLazyMigrationService;
    @Mock
    private CmsConsentMapper cmsConsentMapper;
    @Mock
    private AisConsentVerifyingRepository aisConsentVerifyingRepository;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AspspProfileService aspspProfileService;

    @BeforeEach
    void setUp() {
        AuthorisationEntity authorisationEntity = buildConsentAuthorisation();
        authorisationEntities.add(authorisationEntity);
        consentEntity = buildConsentEntity(EXTERNAL_CONSENT_ID);
    }

    @Test
    void getConsentById_success() {
        // Given
        when(consentJpaRepository.findByExternalId(any()))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentLazyMigrationService.migrateIfNeeded(any(ConsentEntity.class)))
            .thenReturn(consentEntity);
        when(cmsConsentMapper.mapToCmsConsent(consentEntity, authorisationEntities, Collections.emptyMap()))
            .thenReturn(buildCmsConsent());
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consentEntity))
            .thenReturn(consentEntity);
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisationEntities);

        // When
        CmsResponse<CmsConsent> retrievedConsent = consentServiceInternal.getConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isSuccessful());
        assertEquals(retrievedConsent.getPayload().getId(), consentEntity.getExternalId());
    }

    @Test
    void getConsentById_checkAndUpdateOnExpirationInvoked() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentLazyMigrationService.migrateIfNeeded(any(ConsentEntity.class)))
            .thenReturn(consentEntity);
        when(cmsConsentMapper.mapToCmsConsent(consentEntity, authorisationEntities, Collections.emptyMap()))
            .thenReturn(buildCmsConsent());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisationEntities);

        // When
        CmsResponse<CmsConsent> retrievedConsent = consentServiceInternal.getConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isSuccessful());
    }

    @Test
    void getConsentById_checkAndUpdateOnExpirationNotInvoked() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentLazyMigrationService.migrateIfNeeded(any(ConsentEntity.class)))
            .thenReturn(consentEntity);
        when(cmsConsentMapper.mapToCmsConsent(consentEntity, authorisationEntities, Collections.emptyMap()))
            .thenReturn(buildCmsConsent());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisationEntities);

        // When
        CmsResponse<CmsConsent> retrievedConsent = consentServiceInternal.getConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(retrievedConsent.isSuccessful());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void getConsentById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        ConsentEntity consent = buildUsedNonRecurringConsent();

        when(aisConsentLazyMigrationService.migrateIfNeeded(any(ConsentEntity.class)))
            .thenReturn(consent);

        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.expireConsent(consent)).thenReturn(consent);

        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisationEntities);

        // When
        consentServiceInternal.getConsentById(EXTERNAL_CONSENT_ID);

        // Then
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    void getConsentById_noConsent() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<CmsConsent> response = consentServiceInternal.getConsentById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(response.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, response.getError());
    }

    @Test
    void createConsent_shouldReturnCmsCreateConsentResponse() throws WrongChecksumException {
        // Given
        when(aisConsentVerifyingRepository.verifyAndSave(any(ConsentEntity.class)))
            .thenReturn(consentEntity);
        when(cmsConsentMapper.mapToNewConsentEntity(any()))
            .thenReturn(buildFinalisedConsent());
        when(cmsConsentMapper.mapToCmsConsent(consentEntity, Collections.emptyList(), Collections.emptyMap()))
            .thenReturn(buildCmsConsent());
        when(aspspProfileService.getAspspSettings(consentEntity.getInstanceId())).thenReturn(aspspSettings);

        CmsCreateConsentResponse expected = new CmsCreateConsentResponse(EXTERNAL_CONSENT_ID, buildCmsConsent());

        // When
        CmsResponse<CmsCreateConsentResponse> actual = consentServiceInternal.createConsent(buildCmsConsent());

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(expected, actual.getPayload());
    }

    @Test
    void createConsent_shouldReturnCmsCreateConsentResponse_withTppRedirectUri() throws WrongChecksumException {
        // Given
        when(cmsConsentMapper.mapToNewConsentEntity(any()))
            .thenReturn(buildFinalisedConsent());
        when(aisConsentVerifyingRepository.verifyAndSave(any()))
            .thenReturn(consentEntity);
        when(cmsConsentMapper.mapToCmsConsent(consentEntity, Collections.emptyList(), Collections.emptyMap()))
            .thenReturn(buildCmsConsent());
        when(aspspProfileService.getAspspSettings(consentEntity.getInstanceId())).thenReturn(aspspSettings);

        CmsCreateConsentResponse expected = new CmsCreateConsentResponse(EXTERNAL_CONSENT_ID, buildCmsConsent());
        CmsConsent cmsConsent = buildCmsConsent();

        // When
        CmsResponse<CmsCreateConsentResponse> actual = consentServiceInternal.createConsent(cmsConsent);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(expected, actual.getPayload());
    }

    @Test
    void createConsent_frequencyPerDay_isNull_shouldReturnLogicalError() throws WrongChecksumException {
        // Given
        CmsConsent cmsConsent = buildCmsConsent();
        cmsConsent.setFrequencyPerDay(null);

        // When
        CmsResponse<CmsCreateConsentResponse> actual = consentServiceInternal.createConsent(cmsConsent);

        // Then
        assertLogicalError(actual);
    }

    @Test
    void createConsent_id_isNull_shouldReturnTechnicalError() throws WrongChecksumException {
        // Given
        consentEntity.setId(null);

        when(aisConsentVerifyingRepository.verifyAndSave(any()))
            .thenReturn(consentEntity);
        when(cmsConsentMapper.mapToNewConsentEntity(any()))
            .thenReturn(consentEntity);
        when(aspspProfileService.getAspspSettings(consentEntity.getInstanceId())).thenReturn(aspspSettings);

        // When
        CmsResponse<CmsCreateConsentResponse> actual = consentServiceInternal.createConsent(buildCmsConsent());

        // Then
        assertFalse(actual.isSuccessful());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void createConsent_shouldAdjustValidUntilDate() throws WrongChecksumException {
        // Given
        CmsConsent cmsConsent = buildCmsConsent();
        when(cmsConsentMapper.mapToNewConsentEntity(any()))
            .thenReturn(buildConsentEntity(EXTERNAL_CONSENT_ID));
        int maxConsentValidity = 2;
        when(aspspProfileService.getAspspSettings(cmsConsent.getInstanceId())).thenReturn(buildMockAspspSettings(maxConsentValidity));
        LocalDate adjustedValidUntil = LocalDate.now().plusDays(maxConsentValidity - 1);
        ConsentEntity adjustedConsentEntity = buildAdjustedConsentEntity(EXTERNAL_CONSENT_ID, adjustedValidUntil);
        when(aisConsentVerifyingRepository.verifyAndSave(any(ConsentEntity.class)))
            .thenReturn(adjustedConsentEntity);
        CmsConsent adjustedConsent = buildAdjustedCmsConsent(adjustedValidUntil);
        when(cmsConsentMapper.mapToCmsConsent(adjustedConsentEntity, Collections.emptyList(), Collections.emptyMap()))
            .thenReturn(adjustedConsent);
        CmsCreateConsentResponse expected = new CmsCreateConsentResponse(EXTERNAL_CONSENT_ID, adjustedConsent);
        ArgumentCaptor<ConsentEntity> consentEntityCaptor = ArgumentCaptor.forClass(ConsentEntity.class);

        // When
        CmsResponse<CmsCreateConsentResponse> actual = consentServiceInternal.createConsent(cmsConsent);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(expected, actual.getPayload());
        verify(aisConsentVerifyingRepository).verifyAndSave(consentEntityCaptor.capture());
        ConsentEntity capturedConsentEntity = consentEntityCaptor.getValue();
        assertEquals(adjustedValidUntil, capturedConsentEntity.getValidUntil());
    }

    @Test
    void getPsuDataByConsentId_success() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(psuDataMapper.mapToPsuIdDataList(anyList()))
            .thenReturn(Collections.singletonList(PSU_ID_DATA));

        // When
        CmsResponse<List<PsuIdData>> psuDataList = consentServiceInternal.getPsuDataByConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertEquals(PSU_ID_DATA, psuDataList.getPayload().get(0));
    }

    @Test
    void getPsuDataByConsentId_noPsuData_shouldReturnLogicalError() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<List<PsuIdData>> actual = consentServiceInternal.getPsuDataByConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertLogicalError(actual);
    }

    @Test
    void updateMultilevelScaRequired_success() throws WrongChecksumException {
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.ofNullable(consentEntity));
        // When
        CmsResponse<Boolean> actual = consentServiceInternal.updateMultilevelScaRequired(EXTERNAL_CONSENT_ID, true);

        // Then
        assertTrue(actual.getPayload());
    }

    @Test
    void updateMultilevelScaRequired_checksumBad_shouldThrowChecksumException() throws WrongChecksumException {
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.ofNullable(consentEntity));

        // Given
        when(aisConsentVerifyingRepository.verifyAndSave(any(ConsentEntity.class)))
            .thenThrow(WrongChecksumException.class);

        // When
        assertThrows(WrongChecksumException.class, () -> consentServiceInternal.updateMultilevelScaRequired(EXTERNAL_CONSENT_ID, true));
    }

    @Test
    void updateMultilevelScaRequired_noEntity_shouldReturnFalse() throws WrongChecksumException {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actual = consentServiceInternal.updateMultilevelScaRequired(EXTERNAL_CONSENT_ID, true);

        // Then
        assertFalse(actual.getPayload());
    }

    @Test
    void updateConsentStatusById_UpdateFinalisedStatus_Fail() throws WrongChecksumException {
        // Given
        ConsentEntity finalisedConsent = buildFinalisedConsent();
        when(consentJpaRepository.findByExternalId(FINALISED_CONSENT_ID))
            .thenReturn(Optional.of(finalisedConsent));

        // When
        CmsResponse<Boolean> actual = consentServiceInternal.updateConsentStatusById(FINALISED_CONSENT_ID, ConsentStatus.EXPIRED);

        // Then
        assertLogicalError(actual);
    }

    @Test
    void updateConsentStatusById_noActualConsent_shouldReturnFalse() throws WrongChecksumException {
        // Given
        ConsentEntity nonFinalisedConsent = buildFinalisedConsent();
        nonFinalisedConsent.setConsentStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        when(consentJpaRepository.findByExternalId(FINALISED_CONSENT_ID))
            .thenReturn(Optional.of(nonFinalisedConsent));

        // When
        CmsResponse<Boolean> result = consentServiceInternal.updateConsentStatusById(FINALISED_CONSENT_ID, ConsentStatus.EXPIRED);

        // Then
        assertTrue(result.isSuccessful());
        assertEquals(Boolean.FALSE, result.getPayload());
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_failure_consentNotFound() {
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID_NOT_EXIST)
        );
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success_newConsentRecurringIndicatorIsFalse() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntityMocked));

        when(consentEntityMocked.isOneAccessType())
            .thenReturn(true);

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertFalse(result.getPayload());
        verify(consentJpaRepository, never()).findOldConsentsByNewConsentParams(any(), any(), any(), any(), any());
    }

    @Test
    void findAndTerminateOldConsents_success_newConsentRecurringIndicatorIsFalse() {
        // Given
        TerminateOldConsentsRequest request = getTerminateOldConsentsRequestOneOff();

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsents(EXTERNAL_CONSENT_ID, request);

        // Then
        assertTrue(result.isSuccessful());
        assertFalse(result.getPayload());
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_failure_wrongConsentData() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntityMocked));

        when(consentEntityMocked.isWrongConsentData())
            .thenReturn(true);

        // When
        assertThrows(
            IllegalArgumentException.class,
            () -> consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID)
        );
    }


    @Test
    void findAndTerminateOldConsents_failure_wrongConsentData() {
        // Given
        TerminateOldConsentsRequest request = getTerminateOldConsentsRequestWrongConsentData();

        // When
        assertThrows(
            IllegalArgumentException.class,
            () -> consentServiceInternal.findAndTerminateOldConsents(EXTERNAL_CONSENT_ID, request)
        );
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success_oldConsentsEmpty() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());
        assertFalse(result.getPayload());
    }

    @Test
    void findAndTerminateOldConsents_success_oldConsentsEmpty() {
        // Given
        TerminateOldConsentsRequest request = getTerminateOldConsentsRequest();

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsents(EXTERNAL_CONSENT_ID, request);

        // Then
        assertTrue(result.isSuccessful());
        assertFalse(result.getPayload());
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntityMocked));

        ConsentTppInformationEntity tppInformation = new ConsentTppInformationEntity();
        tppInformation.setTppInfo(tppInfoMocked);

        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);
        when(consentEntityMocked.getPsuDataList())
            .thenReturn(psuDataList);
        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);
        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);
        when(consentEntityMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);
        when(consentEntityMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);
        when(consentEntityMocked.getTppInformation())
            .thenReturn(tppInformation);
        when(cmsPsuService.isPsuDataListEqual(psuDataList, psuDataList))
            .thenReturn(true);

        ConsentEntity oldConsent = buildConsentEntity(EXTERNAL_CONSENT_ID_NOT_EXIST);
        List<ConsentEntity> oldConsents = Collections.singletonList(oldConsent);
        when(consentJpaRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());
        assertTrue(result.getPayload());
        assertEquals(ConsentStatus.TERMINATED_BY_TPP, oldConsent.getConsentStatus());
        verify(consentJpaRepository).saveAll(oldConsents);
    }

    @Test
    void findAndTerminateOldConsents_success() {
        // Given
        TerminateOldConsentsRequest request = getTerminateOldConsentsRequest();

        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);
        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);
        when(cmsPsuService.isPsuDataListEqual(psuDataList, psuDataList))
            .thenReturn(true);

        when(psuDataMapper.mapToPsuDataList(request.getPsuIdDataList(), request.getInstanceId())).thenReturn(psuDataList);
        ConsentEntity oldConsent = buildConsentEntity(EXTERNAL_CONSENT_ID_NOT_EXIST);
        List<ConsentEntity> oldConsents = Collections.singletonList(oldConsent);
        when(consentJpaRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID),
                                                                    AUTHORISATION_NUMBER,
                                                                    INSTANCE_ID,
                                                                    EXTERNAL_CONSENT_ID,
                                                                    EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsents(EXTERNAL_CONSENT_ID, request);

        // Then
        assertTrue(result.isSuccessful());
        assertTrue(result.getPayload());
        assertEquals(ConsentStatus.TERMINATED_BY_TPP, oldConsent.getConsentStatus());
        verify(consentJpaRepository).saveAll(oldConsents);
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_success_multilevel_SCA() {
        // Given
        ConsentEntity consent = buildUsedNonRecurringConsent();
        consent.setRecurringIndicator(true);

        ConsentTppInformationEntity tppInformation = new ConsentTppInformationEntity();
        tppInformation.setTppInfo(tppInfoMocked);

        consent.setTppInformation(tppInformation);

        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);
        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);

        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);

        when(cmsPsuService.isPsuDataListEqual(psuDataList, psuDataList))
            .thenReturn(true);

        ConsentEntity oldConsent = buildConsentEntity(EXTERNAL_CONSENT_ID_NOT_EXIST);
        oldConsent.setConsentStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        List<ConsentEntity> oldConsents = Collections.singletonList(oldConsent);
        when(consentJpaRepository.findOldConsentsByNewConsentParams(Collections.singleton(PSU_ID), AUTHORISATION_NUMBER, INSTANCE_ID, EXTERNAL_CONSENT_ID, EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED, ConsentStatus.VALID)))
            .thenReturn(oldConsents);

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertTrue(result.getPayload());
        assertEquals(ConsentStatus.REJECTED, oldConsent.getConsentStatus());
        verify(consentJpaRepository).saveAll(oldConsents);
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_shouldFail_unequalPsuDataLists() {
        // Given
        List<PsuData> psuDataList = Collections.singletonList(psuDataMocked);

        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntityMocked));
        ConsentTppInformationEntity tppInformation = new ConsentTppInformationEntity();
        tppInformation.setTppInfo(tppInfoMocked);
        when(consentEntityMocked.getTppInformation())
            .thenReturn(tppInformation);
        when(consentEntityMocked.getPsuDataList())
            .thenReturn(psuDataList);
        when(psuDataMocked.getPsuId())
            .thenReturn(PSU_ID);
        when(tppInfoMocked.getAuthorisationNumber())
            .thenReturn(AUTHORISATION_NUMBER);
        when(consentEntityMocked.getInstanceId())
            .thenReturn(INSTANCE_ID);
        when(consentEntityMocked.getExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);

        // When
        CmsResponse<Boolean> result = consentServiceInternal.findAndTerminateOldConsentsByNewConsentId(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(result.isSuccessful());

        assertFalse(result.getPayload());
        verify(consentJpaRepository, never()).save(any(ConsentEntity.class));
    }

    @Test
    void getConsentStatusById_withValidUsedNonRecurringConsent_shouldExpireConsent() {
        // Given
        ConsentEntity consent = buildUsedNonRecurringConsent();

        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consent));
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent))
            .thenReturn(consent);
        when(aisConsentConfirmationExpirationService.expireConsent(consent)).thenReturn(consent);

        // When
        CmsResponse<ConsentStatus> consentStatusById = consentServiceInternal.getConsentStatusById(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(consentStatusById.isSuccessful());
        verify(aisConsentConfirmationExpirationService, atLeastOnce()).expireConsent(consent);
    }

    @Test
    void getConsentStatusById_noEntity_shouldReturnLogicalError() {
        // Given
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<ConsentStatus> actual = consentServiceInternal.getConsentStatusById(EXTERNAL_CONSENT_ID);

        // Then
        assertLogicalError(actual);
    }

    private void assertLogicalError(CmsResponse<?> actual) {
        assertFalse(actual.isSuccessful());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    private ConsentEntity buildUsedNonRecurringConsent() {
        ConsentEntity consent = buildConsentEntity(EXTERNAL_CONSENT_ID);

        AisConsentUsage usage = new AisConsentUsage();
        usage.setUsageDate(LocalDate.of(2019, 6, 3));

        consent.setUsages(Collections.singletonList(usage));
        consent.setConsentStatus(ConsentStatus.VALID);
        return consent;
    }

    private AuthorisationEntity buildConsentAuthorisation() {
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setExternalId(AUTHORISATION_ID);
        authorisationEntity.setPsuData(PSU_DATA);
        authorisationEntity.setScaStatus(ScaStatus.RECEIVED);
        return authorisationEntity;
    }

    private ConsentEntity buildAdjustedConsentEntity(String externalId, LocalDate adjustedValidUntil) {
        ConsentEntity consentEntity = buildConsentEntity(externalId);
        consentEntity.setValidUntil(adjustedValidUntil);
        return consentEntity;
    }


    private ConsentEntity buildConsentEntity(String externalId) {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setId(CONSENT_ID);
        consentEntity.setExternalId(externalId);
        consentEntity.setValidUntil(VALID_UNTIL);
        consentEntity.setConsentStatus(ConsentStatus.VALID);
        consentEntity.setPsuDataList(Collections.singletonList(psuDataMocked));
        AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();
        authorisationTemplate.setRedirectUri(REDIRECT_URI);
        authorisationTemplate.setNokRedirectUri(NOK_REDIRECT_URI);
        consentEntity.setAuthorisationTemplate(authorisationTemplate);
        return consentEntity;
    }

    private CmsConsent buildAdjustedCmsConsent(LocalDate adjustedValidUntil) {
        CmsConsent cmsConsent = buildCmsConsent();
        cmsConsent.setValidUntil(adjustedValidUntil);
        return cmsConsent;
    }

    private CmsConsent buildCmsConsent() {
        return jsonReader.getObjectFromFile("json/cms-consent.json", CmsConsent.class);
    }

    private ConsentEntity buildFinalisedConsent() {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setId(CONSENT_ID);
        consentEntity.setExternalId(EXTERNAL_CONSENT_ID);
        consentEntity.setValidUntil(LocalDate.now());
        consentEntity.setConsentStatus(ConsentStatus.REJECTED);
        consentEntity.setInstanceId(INSTANCE_ID);
        return consentEntity;
    }

    private AspspSettings buildMockAspspSettings() {
        return buildMockAspspSettings(0);
    }

    private AspspSettings buildMockAspspSettings(int maxValidity) {
        ConsentTypeSetting consentTypeSettings = new ConsentTypeSetting(false, false, false, 0, 1L, maxValidity, false, false);
        AisAspspProfileSetting aisAspspProfileSettings = new AisAspspProfileSetting(consentTypeSettings, null, null, null, null);
        return new AspspSettings(aisAspspProfileSettings, null, null, null, null);
    }

    private TerminateOldConsentsRequest getTerminateOldConsentsRequestOneOff() {
        return new TerminateOldConsentsRequest(true, false, null, null, null);
    }

    private TerminateOldConsentsRequest getTerminateOldConsentsRequestWrongConsentData() {
        return new TerminateOldConsentsRequest(false, true, null, null, null);
    }

    private TerminateOldConsentsRequest getTerminateOldConsentsRequest() {
        return jsonReader.getObjectFromFile("json/service/terminate-consent-req.json", TerminateOldConsentsRequest.class);
    }
}

