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
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisPsuDataAuthorisation;
import de.adorsys.psd2.consent.repository.AisConsentVerifyingRepository;
import de.adorsys.psd2.consent.repository.AspspAccountAccessRepository;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.AuthorisationSpecification;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.consent.service.authorisation.CmsConsentAuthorisationServiceInternal;
import de.adorsys.psd2.consent.service.mapper.AccessMapper;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsPsuAuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.consent.service.psu.util.PsuDataUpdater;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final Integer PAGE_INDEX = 0;
    private static final Integer ITEMS_PER_PAGE = 20;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String AUTHORISATION_ID_NOT_EXIST = "248eae68-e4fa-4d43-8b3f-2ae2b584cdd9";
    private static final String FINALISED_AUTHORISATION_ID = "6b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String TPP_OK_REDIRECT_URI = "Mock tppOkRedirectUri";
    private static final String TPP_NOK_REDIRECT_URI = "Mock tppNokRedirectUri";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String CORRECT_PSU_ID = "anton.brueckner";
    private static final String WRONG_PSU_ID = "max.musterman";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";
    private static final int FREQUENCY_PER_DAY = 777;
    private static final LocalDate VALID_UNTIL = LocalDate.now();

    @InjectMocks
    private CmsPsuAisServiceInternal cmsPsuAisService;

    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private AspspAccountAccessRepository aspspAccountAccessRepository;
    @Mock
    private AisConsentVerifyingRepository aisConsentVerifyingRepository;
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
    private AisConsentLazyMigrationService aisConsentLazyMigrationService;
    @Mock
    private CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper;
    @Mock
    private AccessMapper accessMapper;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private CmsConsentAuthorisationServiceInternal consentAuthorisationService;
    @Mock
    private CmsPsuConsentServiceInternal cmsPsuConsentServiceInternal;
    @Mock
    private PsuDataUpdater psuDataUpdater;
    @Mock
    private PageRequestBuilder pageRequestBuilder;

    private ConsentEntity consentEntity;
    private List<ConsentEntity> consentEntityList;
    private Page<ConsentEntity> consentEntityPage;
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
        consentEntityPage = buildAisConsentsPage();
        authenticationDataHolder = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);
    }

    @Test
    void updatePsuDataInConsent() throws AuthorisationIsExpiredException {
        // Given
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(cmsPsuConsentServiceInternal.updatePsuData(authorisationEntity, psuIdData, ConsentType.AIS))
            .thenReturn(true);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updatePsuDataInConsent);
        verify(authorisationSpecification, times(1))
            .byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsent_authorisationNotFound() throws AuthorisationIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updatePsuDataInConsent);
        verify(authorisationSpecification).byExternalIdAndInstanceId(AUTHORISATION_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void updatePsuDataInConsent_expiredAuthorisation_shouldThrowException() {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        AuthorisationEntity expiredAuthorisationEntity = buildExpiredAuthorisationEntity();
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(expiredAuthorisationEntity));

        // When
        assertThrows(AuthorisationIsExpiredException.class,
                     () -> cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID));

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void updatePsuDataInConsent_emptyPsuData_shouldNotUpdate() throws AuthorisationIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));
        PsuIdData emptyPsuIdData = new PsuIdData();
        PsuData emptyPsuData = new PsuData();
        when(cmsPsuConsentServiceInternal.updatePsuData(authorisationEntity, emptyPsuIdData, ConsentType.AIS))
            .thenReturn(false);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(emptyPsuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updatePsuDataInConsent);
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void updatePsuDataInConsent_authorisationContainsPsuData_shouldUpdateExistingPsuData() throws AuthorisationIsExpiredException {
        // Given
        Long psuDataId = 1L;
        AuthorisationEntity authorisationEntityWithPsuData = buildAuthorisationEntityWithEmptyPsu(psuDataId);
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(authorisationEntityWithPsuData));
        PsuData psuDataFromAuth = new PsuData();
        psuDataFromAuth.setId(psuDataId);
        when(cmsPsuConsentServiceInternal.updatePsuData(authorisationEntityWithPsuData, psuIdData, ConsentType.AIS))
            .thenReturn(true);

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updatePsuDataInConsent);
    }

    @Test
    void updatePsuDataInConsent_consentNotFound_shouldNotUpdate() throws AuthorisationIsExpiredException {
        // Given
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));

        // When
        boolean updatePsuDataInConsent = cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updatePsuDataInConsent);
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void getConsent() {
        // Given
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(aisAccountConsent);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
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
    void getConsent_noConsent() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<CmsAisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consent.isPresent());
        verify(aisConsentSpecification).byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void getConsent_finalisedExpiredConsent_shouldNotChangeStatus() {
        //Given
        ConsentStatus consentStatus = ConsentStatus.TERMINATED_BY_TPP;
        ConsentEntity aisConsentTerminatedByTpp = buildConsentByStatusAndExpireDate(consentStatus, VALID_UNTIL.minusDays(1));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(aisConsentTerminatedByTpp));

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(aisConsentTerminatedByTpp, authorisations))
            .thenReturn(mockCmsAisAccountConsent);
        when(aisConsentLazyMigrationService.migrateIfNeeded(aisConsentTerminatedByTpp))
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
    void getConsent_shouldExpireConsentIfNeeded() {
        //Given
        LocalDate validUntilInThePast = VALID_UNTIL.minusDays(1);
        ConsentEntity consentToBeExpired = buildConsentByStatusAndExpireDate(ConsentStatus.VALID, validUntilInThePast);
        ConsentEntity expiredConsent = buildConsentByStatusAndExpireDate(ConsentStatus.EXPIRED, validUntilInThePast);
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentToBeExpired));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentToBeExpired))
            .thenReturn(consentToBeExpired);
        when(aisConsentConfirmationExpirationService.expireConsent(consentToBeExpired)).thenReturn(expiredConsent);
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(expiredConsent, authorisations))
            .thenReturn(mockCmsAisAccountConsent);

        // When
        Optional<CmsAisAccountConsent> consent = cmsPsuAisService.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        verify(aisConsentConfirmationExpirationService).expireConsent(consentToBeExpired);
    }

    @Test
    void getAuthorisationByAuthorisationId_success() {
        // Given
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(authorisationEntity));
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
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
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        // When
        Optional<CmsPsuAuthorisation> cmsPsuAuthorisation = cmsPsuAisService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(cmsPsuAuthorisation.isPresent());
    }

    @Test
    void updateAuthorisationStatus() throws AuthorisationIsExpiredException {
        // When
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(authorisationEntity));
        when(consentAuthorisationService.updateScaStatusAndAuthenticationData(ScaStatus.RECEIVED, authorisationEntity, authenticationDataHolder))
            .thenReturn(true);

        // Then
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertTrue(updateAuthorisationStatus);
    }

    @Test
    void updateAuthorisationStatus_authorisationNotFound() throws AuthorisationIsExpiredException {
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID_NOT_EXIST, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(updateAuthorisationStatus);
    }

    @Test
    void updateAuthorisationStatus_consentNotFound() throws AuthorisationIsExpiredException {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
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
    void updateAuthorisationStatus_expiredAuthorisation_shouldThrowException() throws AuthorisationIsExpiredException {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        AuthorisationEntity expiredAuthorisationEntity = buildExpiredAuthorisationEntity();
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn(Optional.of(expiredAuthorisationEntity));
        when(consentAuthorisationService.updateScaStatusAndAuthenticationData(ScaStatus.RECEIVED, expiredAuthorisationEntity, authenticationDataHolder))
            .thenReturn(false);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(updateAuthorisationStatus);

        verify(consentAuthorisationService).getAuthorisationByAuthorisationId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID);
        verify(consentAuthorisationService).updateScaStatusAndAuthenticationData(ScaStatus.RECEIVED, expiredAuthorisationEntity, authenticationDataHolder);
    }

    @Test
    void getConsentsForPsuSuccess() {
        // Given
        when(aisConsentSpecification.byPsuDataInListAndInstanceIdAndAdditionalTppInfo(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, AdditionalTppInfo.NONE, Collections.emptyList(), null))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        Pageable pageRequest = Pageable.unpaged();
        when(pageRequestBuilder.getPageable(null, null)).thenReturn(pageRequest);
        when(consentJpaRepository.findAll(any(Specification.class), eq(pageRequest)))
            .thenReturn(consentEntityPage);
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(aisAccountConsent);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(psuIdData, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                           AdditionalTppInfo.NONE, null, null,
                                                                                                           null, null);

        // Then
        assertEquals(consentsForPsu.size(), consentEntityList.size());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceIdAndAdditionalTppInfo(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, AdditionalTppInfo.NONE, Collections.emptyList(), null);
    }

    @Test
    void getConsentsForPsuSuccessPagination() {
        // Given
        when(aisConsentSpecification.byPsuDataInListAndInstanceIdAndAdditionalTppInfo(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, AdditionalTppInfo.NONE, Collections.emptyList(), null))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(pageRequest);
        when(consentJpaRepository.findAll(any(Specification.class), eq(pageRequest)))
            .thenReturn(consentEntityPage);
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(aisAccountConsent);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(psuIdData, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                           AdditionalTppInfo.NONE, null, null, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertEquals(consentsForPsu.size(), consentEntityList.size());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceIdAndAdditionalTppInfo(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, AdditionalTppInfo.NONE, Collections.emptyList(), null);
    }

    @Test
    void getConsentsForPsuFail() {
        // Given
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(pageRequest);
        when(consentJpaRepository.findAll(null, pageRequest)).thenReturn(Page.empty());

        // When
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(psuIdDataWrong,
                                                                                                           DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                           AdditionalTppInfo.NONE, Collections.emptyList(), null, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(consentsForPsu.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuDataInListAndInstanceIdAndAdditionalTppInfo(psuIdDataWrong, DEFAULT_SERVICE_INSTANCE_ID, AdditionalTppInfo.NONE, Collections.emptyList(), null);
    }

    @Test
    void getConsentsForPsu_emptyPsuData() {
        // When
        PsuIdData emptyPsuIdData = new PsuIdData();
        List<CmsAisAccountConsent> consentsForPsu = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(emptyPsuIdData,
                                                                                                           DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                           AdditionalTppInfo.NONE, null, null, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(consentsForPsu.isEmpty());
    }

    @Test
    void confirmConsent() throws WrongChecksumException {
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
        when(aisConsentVerifyingRepository.verifyAndSave(aisConsentValid))
            .thenReturn(aisConsentValid);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean actualResult = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResult);
        verify(aisConsentSpecification).byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void confirmConsent_consentNotFound() throws WrongChecksumException {
        // When
        boolean actualResult = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(aisConsentSpecification).byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void confirmConsent_withAlreadyFinalisedConsent_shouldNotUpdate() throws WrongChecksumException {
        // Given
        ConsentEntity finalisedConsentEntity = buildConsentWithStatus(ConsentStatus.REJECTED);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(finalisedConsentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(finalisedConsentEntity))
            .thenReturn(finalisedConsentEntity);

        // When
        boolean actualResult = cmsPsuAisService.confirmConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(aisConsentVerifyingRepository, never()).verifyAndSave(any());
    }

    @Test
    void rejectConsent() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity aisConsentRejected = buildConsentWithStatus(ConsentStatus.REJECTED);
        when(aisConsentVerifyingRepository.verifyAndSave(aisConsentRejected))
            .thenReturn(aisConsentRejected);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsent_consentNotFound() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification).byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void rejectConsent_withAlreadyFinalisedConsent_shouldNotUpdate() throws WrongChecksumException {
        // Given
        ConsentEntity finalisedConsentEntity = buildConsentWithStatus(ConsentStatus.REJECTED);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(finalisedConsentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(finalisedConsentEntity))
            .thenReturn(finalisedConsentEntity);

        // When
        boolean actualResult = cmsPsuAisService.rejectConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(aisConsentVerifyingRepository, never()).verifyAndSave(any());
    }

    @Test
    void revokeConsent() throws WrongChecksumException {
        // Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        ConsentEntity aisConsentRevoked = buildConsentWithStatus(ConsentStatus.REVOKED_BY_PSU);
        when(aisConsentVerifyingRepository.verifyAndSave(aisConsentRevoked))
            .thenReturn(aisConsentRevoked);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsent_consentNotFound() throws WrongChecksumException {
        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(updateAuthorisationStatus);
        verify(aisConsentSpecification, times(1))
            .byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsent_withAlreadyFinalisedConsent_shouldNotUpdate() throws WrongChecksumException {
        // Given
        ConsentEntity finalisedConsentEntity = buildConsentWithStatus(ConsentStatus.REJECTED);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(finalisedConsentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(finalisedConsentEntity))
            .thenReturn(finalisedConsentEntity);

        // When
        boolean actualResult = cmsPsuAisService.revokeConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(aisConsentVerifyingRepository, never()).verifyAndSave(any());
    }

    @Test
    void authorisePartiallyConsent() throws WrongChecksumException {
        //Given
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));

        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);

        ConsentEntity aisConsent = buildConsentWithStatus(ConsentStatus.PARTIALLY_AUTHORISED);
        aisConsent.setMultilevelScaRequired(true);
        when(aisConsentVerifyingRepository.verifyAndSave(aisConsent))
            .thenReturn(aisConsent);
        ArgumentCaptor<ConsentEntity> argumentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean updateAuthorisationStatus = cmsPsuAisService.authorisePartiallyConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(updateAuthorisationStatus);
        verify(aisConsentVerifyingRepository).verifyAndSave(argumentCaptor.capture());
        ConsentEntity aisConsentActual = argumentCaptor.getValue();
        assertEquals(ConsentStatus.PARTIALLY_AUTHORISED, aisConsentActual.getConsentStatus());
        assertTrue(aisConsentActual.isMultilevelScaRequired());
    }

    @Test
    void authorisePartiallyConsent_withAlreadyFinalisedConsent_shouldNotUpdate() throws WrongChecksumException {
        // Given
        ConsentEntity finalisedConsentEntity = buildConsentWithStatus(ConsentStatus.REJECTED);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(finalisedConsentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(finalisedConsentEntity))
            .thenReturn(finalisedConsentEntity);

        // When
        boolean actualResult = cmsPsuAisService.authorisePartiallyConsent(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResult);
        verify(aisConsentVerifyingRepository, never()).verifyAndSave(any());
    }

    @Test
    void updateAuthorisationStatus_FinalisedStatus_Fail() throws AuthorisationIsExpiredException {
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity), Optional.empty());
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean result = cmsPsuAisService.updateAuthorisationStatus(psuIdData, EXTERNAL_CONSENT_ID, FINALISED_AUTHORISATION_ID, ScaStatus.SCAMETHODSELECTED, DEFAULT_SERVICE_INSTANCE_ID, authenticationDataHolder);

        // Then
        assertFalse(result);
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
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(mockAisConsentAuthorization));
        when(mockAisConsentAuthorization.isRedirectUrlNotExpired())
            .thenReturn(false);

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
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
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
        when(authorisationSpecification.byExternalIdAndInstanceId(AUTHORISATION_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(authorisationRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(mockAisConsentAuthorization));
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        when(mockAisConsentAuthorization.isRedirectUrlNotExpired())
            .thenReturn(true);
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(mockCmsAisAccountConsent);
        when(mockAisConsentAuthorization.getTppOkRedirectUri())
            .thenReturn(TPP_OK_REDIRECT_URI);
        when(mockAisConsentAuthorization.getTppNokRedirectUri())
            .thenReturn(TPP_NOK_REDIRECT_URI);
        when(mockAisConsentAuthorization.getParentExternalId())
            .thenReturn(EXTERNAL_CONSENT_ID);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
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
        int frequencyPerDay = FREQUENCY_PER_DAY;
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/ais-account-access.json", AisAccountAccess.class);
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, VALID_UNTIL, frequencyPerDay, Boolean.TRUE, Boolean.TRUE);
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
        when(accessMapper.mapToAspspAccountAccess(consentEntity, mappedAccountAccess))
            .thenReturn(aspspAccountAccesses);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(saved);
        verify(consentJpaRepository).save(argument.capture());
        verify(aspspAccountAccessRepository).deleteByConsentId(consentEntity.getId());
        ConsentEntity capturedConsentEntity = argument.getValue();
        assertSame(VALID_UNTIL, capturedConsentEntity.getValidUntil());
        assertEquals(frequencyPerDay, capturedConsentEntity.getFrequencyPerDay());
        assertEquals(frequencyPerDay, getUsageCounter(capturedConsentEntity));
        assertFalse(capturedConsentEntity.isRecurringIndicator());
        assertEquals(aspspAccountAccesses, capturedConsentEntity.getAspspAccountAccesses());
    }

    @Test
    void updateAccountAccessInConsent_withConsentInValidStatus_shouldNotUpdate() throws WrongChecksumException {
        // Given
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/ais-account-access.json", AisAccountAccess.class);
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, VALID_UNTIL, FREQUENCY_PER_DAY, Boolean.TRUE, Boolean.TRUE);
        ConsentEntity validConsent = buildConsentWithStatus(ConsentStatus.VALID);
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(validConsent));
        when(aisConsentLazyMigrationService.migrateIfNeeded(validConsent))
            .thenReturn(validConsent);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(saved);
        verify(aisConsentVerifyingRepository, never()).verifyAndUpdate(any());
    }

    @Test
    void updateAccountAccessInConsent_additionalAccountInformation_Success() throws WrongChecksumException {
        // Given
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/ais-account-access-with-additional-information.json", AisAccountAccess.class);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, VALID_UNTIL, FREQUENCY_PER_DAY, Boolean.TRUE, Boolean.TRUE);
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
        when(accessMapper.mapToAspspAccountAccess(consentEntity, mappedAccountAccess))
            .thenReturn(aspspAccountAccesses);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(saved);
        verify(consentJpaRepository).save(argument.capture());
        verify(aspspAccountAccessRepository).deleteByConsentId(consentEntity.getId());
        ConsentEntity capturedConsentEntity = argument.getValue();
        assertEquals(aspspAccountAccesses, capturedConsentEntity.getAspspAccountAccesses());
    }

    @Test
    void updateAccountAccessInConsent_AdditionalAccountInformation_AllAvailableAccounts_Success() throws WrongChecksumException {
        // Given
        AisAccountAccess aisAccountAccess = jsonReader.getObjectFromFile("json/account-access.json", AisAccountAccess.class);

        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(aisAccountAccess, VALID_UNTIL, FREQUENCY_PER_DAY, Boolean.TRUE, Boolean.TRUE);
        ArgumentCaptor<ConsentEntity> argument = ArgumentCaptor.forClass(ConsentEntity.class);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentMapper.mapToAccountAccess(aisAccountAccess))
            .thenReturn(jsonReader.getObjectFromFile("json/account-access-mapped.json", AccountAccess.class));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        verify(consentJpaRepository).save(argument.capture());
        verify(aspspAccountAccessRepository).deleteByConsentId(consentEntity.getId());
        assertTrue(saved);
    }

    @Test
    void getPsuDataAuthorisations_Success() {
        // Given
        ConsentEntity consent = buildConsent();
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consent));
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(Collections.singletonList(buildFinalisedAuthorisation()));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(1, actualResult.get().size());
        assertEquals(ScaStatus.FINALISED, actualResult.get().get(0).getScaStatus());
    }

    @Test
    void getPsuDataAuthorisations_SuccessPagination() {
        // Given
        ConsentEntity consent = buildConsent();
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consent));
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(pageRequest);
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT, pageRequest))
            .thenReturn(Collections.singletonList(buildFinalisedAuthorisation()));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(actualResult.isPresent());
        assertEquals(1, actualResult.get().size());
        assertEquals(ScaStatus.FINALISED, actualResult.get().get(0).getScaStatus());
    }

    @Test
    void getPsuDataAuthorisations_noConsent_Fail() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertFalse(actualResult.isPresent());
    }

    @Test
    void getPsuDataAuthorisationsEmptyPsuData_Success() {
        // Given
        ConsentEntity consent = buildConsent();
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consent));
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Optional<List<CmsAisPsuDataAuthorisation>> actualResult = cmsPsuAisService.getPsuDataAuthorisations(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertTrue(actualResult.isPresent());
        assertTrue(actualResult.get().isEmpty());
    }

    @Test
    void saveAccountAccessInConsent_Consent_Finalised_Failed() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, ""))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.ofNullable(consentEntity));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, "");

        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_Consent_Unknown_Failed() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_NOT_EXIST, ""))
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
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, null, 1, null, null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        boolean saved = cmsPsuAisService.updateAccountAccessInConsent(EXTERNAL_CONSENT_ID, accountAccessRequest, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(saved);
    }

    @Test
    void saveAccountAccessInConsent_InvalidValidUntil() {
        // Given
        when(aisConsentSpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        CmsAisConsentAccessRequest accountAccessRequest = new CmsAisConsentAccessRequest(null, VALID_UNTIL.minusDays(1), 1, null, null);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
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

    private Page<ConsentEntity> buildAisConsentsPage() {
        return new PageImpl(consentEntityList);
    }

    private AuthorisationEntity buildAuthorisationEntityWithEmptyPsu(Long psuDataDatabaseId) {
        AuthorisationEntity authorisationEntity = buildAisConsentAuthorisation();
        PsuData existingPsuData = new PsuData();
        existingPsuData.setId(psuDataDatabaseId);
        authorisationEntity.setPsuData(existingPsuData);
        return authorisationEntity;
    }

    private AuthorisationEntity buildAisConsentAuthorisation() {
        AuthorisationEntity aisConsentAuthorization = new AuthorisationEntity();
        aisConsentAuthorization.setType(AuthorisationType.CONSENT);
        aisConsentAuthorization.setExternalId(AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        aisConsentAuthorization.setParentExternalId(EXTERNAL_CONSENT_ID);
        aisConsentAuthorization.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));
        aisConsentAuthorization.setScaAuthenticationData(AUTHENTICATION_DATA);
        aisConsentAuthorization.setAuthenticationMethodId(METHOD_ID);
        return aisConsentAuthorization;
    }

    private AuthorisationEntity buildExpiredAuthorisationEntity() {
        AuthorisationEntity authorisationEntity = buildAisConsentAuthorisation();
        authorisationEntity.setAuthorisationExpirationTimestamp(OffsetDateTime.now().minusDays(1));
        return authorisationEntity;
    }

    @NotNull
    private PsuData buildPsuDataWithId(Long psuDataId, PsuData savedPsuData) {
        PsuData expectedPsuData = new PsuData(savedPsuData.getPsuId(), savedPsuData.getPsuIdType(),
                                              savedPsuData.getPsuCorporateId(), savedPsuData.getPsuCorporateIdType(),
                                              savedPsuData.getPsuIpAddress(), savedPsuData.getAdditionalPsuData());
        expectedPsuData.setId(psuDataId);
        return expectedPsuData;
    }

    private AuthorisationEntity buildFinalisedAuthorisation() {
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setType(AuthorisationType.CONSENT);
        authorisationEntity.setExternalId(FINALISED_AUTHORISATION_ID);
        authorisationEntity.setScaStatus(ScaStatus.FINALISED);
        authorisationEntity.setPsuData(psuData);
        authorisationEntity.setType(AuthorisationType.CONSENT);
        authorisationEntity.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));

        return authorisationEntity;
    }

    private ConsentEntity buildConsent() {
        ConsentEntity aisConsent = jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);

        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setValidUntil(VALID_UNTIL.plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuDataList(Collections.singletonList(psuData));
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));

        AisConsentData data = new AisConsentData(null, null, null, false);
        ConsentDataMapper mapper = new ConsentDataMapper();
        byte[] bytes = mapper.getBytesFromConsentData(data);

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
                                        false, false, null,
                                        null, null, null, false,
                                        Collections.singletonList(aisAuthorisation), Collections.emptyMap(), OffsetDateTime.now(),
                                        OffsetDateTime.now(), null,
                                        AdditionalTppInfo.NONE);
    }
}

