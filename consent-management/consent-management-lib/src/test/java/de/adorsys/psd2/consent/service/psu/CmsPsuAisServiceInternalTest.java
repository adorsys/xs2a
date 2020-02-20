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

package de.adorsys.psd2.consent.service.psu;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.impl.AisConsentRepositoryImpl;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.consent.service.mapper.AccessMapper;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentMigrationService;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuAisServiceInternalTest {
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String AUTHORISATION_ID_NOT_EXIST = "248eae68-e4fa-4d43-8b3f-2ae2b584cdd9";
    private static final String FINALISED_CONSENT_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_AUTHORISATION_ID = "6b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String TPP_OK_REDIRECT_URI = "Mock tppOkRedirectUri";
    private static final String TPP_NOK_REDIRECT_URI = "Mock tppNokRedirectUri";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String CORRECT_PSU_ID = "anton.brueckner";
    private static final String WRONG_PSU_ID = "max.musterman";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";

    @InjectMocks
    private CmsPsuAisServiceInternal cmsPsuAisService;

    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private AisConsentRepositoryImpl aisConsentRepositoryImpl;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private AuthorisationEntity mockAisConsentAuthorization;
    @Mock
    private CmsAisAccountConsent mockCmsAisAccountConsent;
    @Mock
    private AuthorisationSpecification authorisationSpecification;
    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private ConsentService aisConsentService;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private ConsentDataMapper consentDataMapper;
    @Mock
    private AisConsentMigrationService aisConsentMigrationService;
    @Mock
    private CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper;
    @Mock
    private AccessMapper accessMapper;

    private ConsentEntity consentEntity;
    private List<ConsentEntity> consentEntityList;
    private CmsAisAccountConsent aisAccountConsent;
    private AuthorisationEntity authorisationEntity;
    private PsuIdData psuIdData;
    private PsuIdData psuIdDataWrong;
    private PsuData psuData;
    private JsonReader jsonReader;
    private AuthenticationDataHolder authenticationDataHolder;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData(CORRECT_PSU_ID);
        psuData = new PsuData(CORRECT_PSU_ID, "", "", "", "");
        jsonReader = new JsonReader();
        consentEntity = buildConsent();

        psuIdDataWrong = buildPsuIdData(WRONG_PSU_ID);
        aisAccountConsent = buildCmsAisAccountConsent();
        authorisationEntity = buildAisConsentAuthorisation();
        consentEntityList = buildAisConsents();
        authenticationDataHolder = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);
    }

    @Test
    void updatePsuDataInConsentSuccess() throws AuthorisationIsExpiredException {
        // Given
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        when(psuDataMapper.mapToPsuData(psuIdData))
            .thenReturn(psuData);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), anyList()))
            .thenReturn(Optional.of(psuData));
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updatePsuDataInConsent);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsentFail() throws AuthorisationIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID_NOT_EXIST), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updatePsuDataInConsent);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentSuccess() {
        // Given
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(aisAccountConsent);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<CmsAisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), aisAccountConsent);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentFail() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID_NOT_EXIST), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<CmsAisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consent.isPresent());
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentSuccessStatusNotChanged() {
        //Given
        ConsentStatus consentStatus = ConsentStatus.TERMINATED_BY_TPP;
        ConsentEntity aisConsentTerminatedByTpp = buildConsentByStatusAndExpireDate(consentStatus, LocalDate.now().minusDays(1));
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(aisConsentTerminatedByTpp));

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(aisConsentTerminatedByTpp, authorisations))
            .thenReturn(mockCmsAisAccountConsent);
        when(aisConsentMigrationService.migrateIfNeeded(aisConsentTerminatedByTpp))
            .thenReturn(aisConsentTerminatedByTpp);

        ArgumentCaptor<ConsentEntity> argument = ArgumentCaptor.forClass(ConsentEntity.class);

        // When
        Optional<CmsAisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        verify(aisConsentMapper).mapToCmsAisAccountConsent(argument.capture(), anyList());
        assertEquals(consentStatus, argument.getValue().getConsentStatus());
    }

    @Test
    void getAuthorisationByAuthorisationId_success() {
        // Given
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(cmsPsuAuthorisationMapper.mapToCmsPsuAuthorisation(authorisationEntity))
            .thenReturn(new CmsPsuAuthorisation());

        // When
        Optional<CmsPsuAuthorisation> cmsPsuAuthorisation = cmsPsuAisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(cmsPsuAuthorisation.isPresent());
    }

    @Test
    void getAuthorisationByAuthorisationId_no_authorisation() {
        // Given
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        Optional<CmsPsuAuthorisation> cmsPsuAuthorisation = cmsPsuAisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(cmsPsuAuthorisation.isPresent());
    }

    @Test
    void updateAuthorisationStatusSuccess() throws AuthorisationIsExpiredException {
        // When
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));
        when(authorisationRepository.save(authorisationEntity))
            .thenReturn(authorisationEntity);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(ScaStatus.RECEIVED, authorisationEntity.getScaStatus());
    }

    @Test
    void updateAuthorisationStatusFail() throws AuthorisationIsExpiredException {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID_NOT_EXIST, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_noConsent_Fail() throws AuthorisationIsExpiredException {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID_NOT_EXIST, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(updateAuthorisationStatus);
    }

    @Test
    void getConsentsForPsuSuccess() {
        // Given
        when(aisConsentSpecification.byPsuDataInListAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findAll(any(Specification.class)))
            .thenReturn(consentEntityList);
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(aisAccountConsent);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(consentsForPsu.size(), consentEntityList.size());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsuFail() {
        // When
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consentsForPsu.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceId(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu_emptyPsuData() {
        // When
        PsuIdData emptyPsuIdData = new PsuIdData();
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsu(emptyPsuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consentsForPsu.isEmpty());
    }

    @Test
    void confirmConsentSuccess() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentService.findAndTerminateOldConsentsByNewConsentId(anyString()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        ConsentEntity aisConsentValid = buildConsentWithStatus(ConsentStatus.VALID);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsentValid))
            .thenReturn(aisConsentValid);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void confirmConsentFail() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsentSuccess() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity aisConsentRejected = buildConsentWithStatus(ConsentStatus.REJECTED);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsentRejected))
            .thenReturn(aisConsentRejected);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsentFail() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsentSuccess() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        ConsentEntity aisConsentRevoked = buildConsentWithStatus(ConsentStatus.REVOKED_BY_PSU);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsentRevoked))
            .thenReturn(aisConsentRevoked);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsentFail() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void authorisePartiallyConsentSuccess() throws WrongChecksumException {
        //Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        ConsentEntity aisConsent = buildConsentWithStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        aisConsent.setMultilevelScaRequired(true);
        when(aisConsentRepositoryImpl.verifyAndSave(aisConsent))
            .thenReturn(aisConsent);
        ArgumentCaptor<ConsentEntity> argumentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.authorisePartiallyConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentRepositoryImpl).verifyAndSave(argumentCaptor.capture());
        ConsentEntity aisConsentActual = argumentCaptor.getValue();
        assertEquals(ConsentStatus.PARTIALLY_AUTHORISED, aisConsentActual.getConsentStatus());
        assertTrue(aisConsentActual.isMultilevelScaRequired());
    }

    @Test
    void confirmConsent_FinalisedStatus_Fail() throws WrongChecksumException {
        // When
        boolean result = cmsPsuAisService.confirmConsent(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsent_FinalisedStatus_Fail() throws WrongChecksumException {
        // When
        boolean result = cmsPsuAisService.rejectConsent(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsent_FinalisedStatus_Fail() throws WrongChecksumException {
        // When
        boolean result = cmsPsuAisService.revokeConsent(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(FINALISED_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAuthorisationStatus_FinalisedStatus_Fail() throws AuthorisationIsExpiredException {
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity), Optional.empty());
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(FINALISED_AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean result = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(result);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(FINALISED_AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentByRedirectId_Fail_AuthorisationNotFound() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consentResponseOptional.isPresent());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentByRedirectId_Fail_RedirectExpire() {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(mockAisConsentAuthorization));
        when(mockAisConsentAuthorization.isRedirectUrlNotExpired())
            .thenReturn(false);
        when(authorisationRepository.save(mockAisConsentAuthorization))
            .thenReturn(mockAisConsentAuthorization);

        // When
        assertThrows(
            RedirectUrlIsExpiredException.class,
            () -> cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID)
        );

        verify(mockAisConsentAuthorization).setScaStatus(ScaStatus.FAILED);
    }

    @Test
    void getConsentByRedirectId_Fail_NullAisConsent() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(mockAisConsentAuthorization));
        when(mockAisConsentAuthorization.isRedirectUrlNotExpired())
            .thenReturn(true);

        // When
        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consentResponseOptional.isPresent());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsentByRedirectId_Success() throws RedirectUrlIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(eq(AUTHORISATION_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(mockAisConsentAuthorization));
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        when(mockAisConsentAuthorization.isRedirectUrlNotExpired())
            .thenReturn(true);
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(mockCmsAisAccountConsent);
        when(mockAisConsentAuthorization.getTppOkRedirectUri())
            .thenReturn(TPP_OK_REDIRECT_URI);
        when(mockAisConsentAuthorization.getTppNokRedirectUri())
            .thenReturn(TPP_NOK_REDIRECT_URI);
        when(mockAisConsentAuthorization.getParentExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<CmsAisConsentResponse> consentResponseOptional = cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consentResponseOptional.isPresent());
        verifyCmsAisConsentResponse(consentResponseOptional.get());
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updateAccountAccessInConsent_Success() throws WrongChecksumException {
        // Given
        int frequencyPerDay = 777;
        LocalDate validUntil = LocalDate.now();
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/ais-account-access.json", AisAccountAccess.class);
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, validUntil, frequencyPerDay, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<ConsentEntity> argument = ArgumentCaptor.forClass(ConsentEntity.class);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AccountAccess mappedAccountAccess = jsonReader.getObjectFromFile("json/account-access-mapped.json", AccountAccess.class);
        when(aisConsentMapper.mapToAccountAccess(aisAccountAccess))
            .thenReturn(mappedAccountAccess);
        List<AspspAccountAccess> aspspAccountAccesses = jsonReader.getObjectFromFile("json/service/psu/aspsp-account-access-list.json", new TypeReference<List<AspspAccountAccess>>() {
        });
        when(accessMapper.mapToAspspAccountAccess(mappedAccountAccess))
            .thenReturn(aspspAccountAccesses);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(saved);
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        ConsentEntity capturedConsentEntity = argument.getValue();
        assertSame(capturedConsentEntity.getValidUntil(), validUntil);
        assertEquals(capturedConsentEntity.getFrequencyPerDay(), frequencyPerDay);
        assertEquals(getUsageCounter(capturedConsentEntity), frequencyPerDay);
        assertFalse(capturedConsentEntity.isRecurringIndicator());
        assertEquals(aspspAccountAccesses, capturedConsentEntity.getAspspAccountAccesses());
    }

    @Test
    void updateAccountAccessInConsent_additionalAccountInformation_Success() throws WrongChecksumException {
        // Given
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/ais-account-access-with-additional-information.json", AisAccountAccess.class);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, LocalDate.now(), 777, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<ConsentEntity> argument = ArgumentCaptor.forClass(ConsentEntity.class);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AccountAccess mappedAccountAccess = jsonReader.getObjectFromFile("json/account-access-with-additional-information.json", AccountAccess.class);
        when(aisConsentMapper.mapToAccountAccess(aisAccountAccess))
            .thenReturn(mappedAccountAccess);
        List<AspspAccountAccess> aspspAccountAccesses = jsonReader.getObjectFromFile("json/service/psu/aspsp-account-access-list-owner-name.json", new TypeReference<List<AspspAccountAccess>>() {
        });
        when(accessMapper.mapToAspspAccountAccess(mappedAccountAccess))
            .thenReturn(aspspAccountAccesses);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(saved);
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        ConsentEntity capturedConsentEntity = argument.getValue();
        assertEquals(aspspAccountAccesses, capturedConsentEntity.getAspspAccountAccesses());
    }

    @Test
    void updateAccountAccessInConsent_AdditionalAccountInformation_AllAvailableAccounts_Success() throws WrongChecksumException {
        // Given
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/account-access.json", AisAccountAccess.class);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, LocalDate.now(), 777, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<ConsentEntity> argument = ArgumentCaptor.forClass(ConsentEntity.class);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentMapper.mapToAccountAccess(aisAccountAccess))
            .thenReturn(jsonReader.getObjectFromFile("json/account-access-mapped.json", AccountAccess.class));
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        verify(aisConsentRepositoryImpl).verifyAndUpdate(argument.capture());
        assertTrue(saved);
    }

    @Test
    void getPsuDataAuthorisations_Success() {
        // Given
        ConsentEntity consent = buildConsent();
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consent));
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(Collections.singletonList(buildFinalisedAuthorisation()));
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(1, actualResult.get().size());
        assertEquals(ScaStatus.FINALISED, actualResult.get().get(0).getScaStatus());
    }

    @Test
    void getPsuDataAuthorisations_noConsent_Fail() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getPsuDataAuthorisationsEmptyPsuData_Success() {
        // Given
        ConsentEntity consent = buildConsent();
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID))).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consent));
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().isEmpty());
    }

    @Test
    void saveAccountAccessInConsent_Consent_Finalised_Failed() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq("")))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, "");

        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_Consent_Unknown_Failed() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID_NOT_EXIST), eq("")))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID_NOT_EXIST, accountAccessRequest, "");
        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_AccessIsNull() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_InvalidValidUntil() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(eq(EXTERNAL_CONSENT_ID), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, LocalDate.now().minusDays(1), 1, null, null);
        when(aisConsentMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(saved);
    }

    private void verifyCmsAisConsentResponse(CmsAisConsentResponse cmsAisConsentResponse) {
        assertEquals(mockCmsAisAccountConsent, cmsAisConsentResponse.getAccountConsent());
        assertEquals(AUTHORISATION_ID, cmsAisConsentResponse.getAuthorisationId());
        assertEquals(TPP_NOK_REDIRECT_URI, cmsAisConsentResponse.getTppNokRedirectUri());
        assertEquals(TPP_OK_REDIRECT_URI, cmsAisConsentResponse.getTppOkRedirectUri());
    }

    private int getUsageCounter(ConsentEntity aisConsent) {
        Integer usage = aisConsent.getUsages().stream()
                            .filter(consent -> LocalDate.now().isEqual(consent.getUsageDate()))
                            .findFirst()
                            .map(AisConsentUsage::getUsage)
                            .orElse(0);

        return Math.max(aisConsent.getFrequencyPerDay() - usage, 0);
    }

    private List<ConsentEntity> buildAisConsents() {
        return Arrays.asList(consentEntity, consentEntity, consentEntity);
    }

    private AuthorisationEntity buildAisConsentAuthorisation() {
        AuthorisationEntity aisConsentAuthorization = new AuthorisationEntity();
        aisConsentAuthorization.setAuthorisationType(AuthorisationType.AIS);
        aisConsentAuthorization.setExternalId(AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        aisConsentAuthorization.setParentExternalId(EXTERNAL_CONSENT_ID);
        aisConsentAuthorization.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));
        aisConsentAuthorization.setScaAuthenticationData(AUTHENTICATION_DATA);
        aisConsentAuthorization.setAuthenticationMethodId(METHOD_ID);
        return aisConsentAuthorization;
    }

    private AuthorisationEntity buildFinalisedAuthorisation() {
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setAuthorisationType(AuthorisationType.AIS);
        authorisationEntity.setExternalId(FINALISED_AUTHORISATION_ID);
        authorisationEntity.setScaStatus(ScaStatus.FINALISED);
        authorisationEntity.setPsuData(psuData);
        authorisationEntity.setAuthorisationType(AuthorisationType.AIS);

        return authorisationEntity;
    }

    private ConsentEntity buildConsent() {
        ConsentEntity aisConsent = jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);

        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setValidUntil(LocalDate.now().plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuDataList(Collections.singletonList(psuData));
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));

        AisConsentData data = new AisConsentData(null, null, null, false);
        ConsentDataMapper mapper = new ConsentDataMapper();
        byte[] bytes = mapper.getBytesFromAisConsentData(data);

        aisConsent.setData(bytes);

        return aisConsent;
    }

    private ConsentEntity buildConsentWithStatus(ConsentStatus status) {
        ConsentEntity aisConsent = buildConsent();
        aisConsent.setConsentStatus(status);
        return aisConsent;
    }

    private ConsentEntity buildConsentByStatusAndExpireDate(ConsentStatus status, LocalDate validUntil) {
        ConsentEntity aisConsent = buildConsent();
        aisConsent.setConsentStatus(status);
        aisConsent.setValidUntil(validUntil);
        return aisConsent;
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, "", "", "", "");
    }

    private CmsAisAccountConsent buildCmsAisAccountConsent() {
        AisAccountConsentAuthorisation aisAuthorisation = new AisAccountConsentAuthorisation(AUTHORISATION_ID, buildPsuIdData(CORRECT_PSU_ID), ScaStatus.RECEIVED);
        return new CmsAisAccountConsent(consentEntity.getId().toString(),
                                        null, false,
                                        null, null, 0,
                                        null, null,
                                        false, false, null, null, null, null, false, Collections.singletonList(aisAuthorisation), Collections.emptyMap(), OffsetDateTime.now(),
                                        OffsetDateTime.now());
    }
}

