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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAspspAisExportServiceInternalTest {
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "anton.brueckner";
    private static final String WRONG_PSU_ID = "max.musterman";
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_ASPSP_ACCOUNT_ID = "00000000aa-n2131-13nw";
    private static final OffsetDateTime CREATION_DATE_TIME = OffsetDateTime.now();
    private static final OffsetDateTime STATUS_CHANGE_DATE_TIME = OffsetDateTime.now();

    private PsuIdData psuIdData;
    private PsuIdData wrongPsuIdData;
    private JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private CmsAspspAisExportServiceInternal cmsAspspAisExportServiceInternal;

    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AisConsentLazyMigrationService aisConsentLazyMigrationService;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);
    }

    @Test
    void exportConsentsByTpp_success() {
        // Given
        ConsentEntity consentEntity = buildConsentEntity();

        when(aisConsentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(
            TPP_AUTHORISATION_NUMBER,
            CREATION_DATE_FROM,
            CREATION_DATE_TO,
            psuIdData,
            DEFAULT_SERVICE_INSTANCE_ID, null
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(consentEntity));
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());

        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertFalse(aisConsents.isEmpty());
        assertTrue(aisConsents.contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null);
    }

    @Test
    void exportConsentsByTpp_failure_wrongTppAuthorisationNumber() {
        // Given
        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null);
    }

    @Test
    void exportConsentsByTpp_failure_nullTppAuthorisationNumber() {
        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(null, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByPsu_success() {
        // Given
        ConsentEntity consentEntity = buildConsentEntity();

        when(aisConsentSpecification.byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData,
                                                                                                   CREATION_DATE_FROM,
                                                                                                   CREATION_DATE_TO,
                                                                                                   DEFAULT_SERVICE_INSTANCE_ID, null
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll(any())).thenReturn(Collections.singletonList(consentEntity));
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(psuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertFalse(aisConsents.isEmpty());
        assertTrue(aisConsents.contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);
    }

    @Test
    void exportConsentsByPsu_failure_wrongPsuIdData() {
        // Given
        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(wrongPsuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(wrongPsuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);
    }

    @Test
    void exportConsentsByPsu_failure_nullPsuIdData() {
        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(null, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByPsu_failure_emptyPsuIdData() {
        // Given
        PsuIdData emptyPsuIdData = new PsuIdData(null, null, null, null, null);

        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(emptyPsuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, never()).byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByAccountId_success() {
        // Given
        when(aisConsentSpecification.byAspspAccountIdAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                                        CREATION_DATE_TO,
                                                                                                        DEFAULT_SERVICE_INSTANCE_ID, null
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildConsentEntity();
        when(consentJpaRepository.findAll(any()))
            .thenReturn(Collections.singletonList(consentEntity));
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndAuthorisationType(EXTERNAL_CONSENT_ID, AuthorisationType.AIS))
            .thenReturn(authorisations);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountIdAndAdditionalTppInfo(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertFalse(aisConsents.isEmpty());
        assertTrue(aisConsents.contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);
    }

    @Test
    void exportConsentsByAccountId_success_withNoInstanceId() {
        Collection<CmsAisAccountConsent> cmsAisAccountConsents = cmsAspspAisExportServiceInternal.exportConsentsByAccountIdAndAdditionalTppInfo(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO, "", null);

        assertEquals(Collections.emptyList(), cmsAisAccountConsents);
    }

    @Test
    void exportConsentsByAccountId_failure_wrongAspspAccountId() {
        // When
        Collection<CmsAisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountIdAndAdditionalTppInfo(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null);
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private CmsAisAccountConsent buildAisAccountConsent() {
        return new CmsAisAccountConsent(EXTERNAL_CONSENT_ID,
                                        null, false,
                                        null, null, 0,
                                        null, null,
                                        false, false, null,
                                        null, null, null,
                                        false, Collections.emptyList(), Collections.emptyMap(),
                                        CREATION_DATE_TIME, STATUS_CHANGE_DATE_TIME, null, null);
    }

    private ConsentEntity buildConsentEntity() {
        return jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);
    }
}
