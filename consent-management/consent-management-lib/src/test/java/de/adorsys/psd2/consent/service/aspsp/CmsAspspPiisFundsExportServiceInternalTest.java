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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAspspPiisFundsExportServiceInternalTest {
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String SERVICE_INSTANCE_ID = "instance id";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "psu id";
    private static final String WRONG_PSU_ID = "wrong psu id";

    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final String WRONG_ASPSP_ACCOUNT_ID = "wrong aspsp account id";
    private static final int PAGE_INDEX = 0;
    private static final int ITEMS_PER_PAGE = 60;
    private static final int TOTAL = 300;
    private static final Pageable PAGE_REQUEST = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);

    private static final OffsetDateTime CREATION_TIMESTAMP =
        OffsetDateTime.of(2019, 2, 4, 12, 0, 0, 0, ZoneOffset.UTC);

    @InjectMocks
    private CmsAspspPiisFundsExportServiceInternal cmsAspspPiisFundsExportServiceInternal;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @Mock
    private PiisConsentLazyMigrationService piisConsentLazyMigrationService;
    @Mock
    private PageRequestBuilder pageRequestBuilder;

    private PsuIdData psuIdData;
    private PsuIdData wrongPsuIdData;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);
    }

    @Test
    void exportConsentsByTpp_success() {
        // Given
        when(piisConsentEntitySpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER,
                                                                                              CREATION_DATE_FROM,
                                                                                              CREATION_DATE_TO,
                                                                                              psuIdData,
                                                                                              SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        Page<ConsentEntity> page = new PageImpl<>(Collections.singletonList(consentEntity), PAGE_REQUEST, TOTAL);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        CmsPiisConsent cmsPiisConsent = buildCmsPiisConsent();
        when(piisConsentMapper.mapToCmsPiisConsent(consentEntity)).thenReturn(cmsPiisConsent);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);
        when(piisConsentLazyMigrationService.migrateIfNeeded(consentEntity)).thenReturn(consentEntity);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(cmsPiisConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByTpp_success_nullInstanceId() {
        // Given
        when(piisConsentEntitySpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER,
                                                                                              CREATION_DATE_FROM,
                                                                                              CREATION_DATE_TO,
                                                                                              psuIdData,
                                                                                              DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        Page<ConsentEntity> page = new PageImpl<>(Collections.singletonList(consentEntity), PAGE_REQUEST, TOTAL);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        CmsPiisConsent cmsPiisConsent = buildCmsPiisConsent();
        when(piisConsentMapper.mapToCmsPiisConsent(consentEntity)).thenReturn(cmsPiisConsent);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);
        when(piisConsentLazyMigrationService.migrateIfNeeded(consentEntity)).thenReturn(consentEntity);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, null, PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(cmsPiisConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByTpp_failure_wrongTppAuthorisationNumber() {
        // Given
        when(piisConsentEntitySpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER,
                                                                                              CREATION_DATE_FROM,
                                                                                              CREATION_DATE_TO,
                                                                                              psuIdData,
                                                                                              SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        Page<ConsentEntity> page = new PageImpl<>(Collections.emptyList(), PAGE_REQUEST, 0);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByTpp_failure_nullTppAuthorisationNumber() {
        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(null, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByTpp_failure_blankTppAuthorisationNumber() {
        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp("", CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByPsu_success() {
        // Given
        when(piisConsentEntitySpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData,
                                                                                      CREATION_DATE_FROM,
                                                                                      CREATION_DATE_TO,
                                                                                      SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        Page<ConsentEntity> page = new PageImpl<>(Collections.singletonList(consentEntity), PAGE_REQUEST, TOTAL);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        CmsPiisConsent cmsPiisConsent = buildCmsPiisConsent();
        when(piisConsentMapper.mapToCmsPiisConsent(consentEntity)).thenReturn(cmsPiisConsent);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);
        when(piisConsentLazyMigrationService.migrateIfNeeded(consentEntity)).thenReturn(consentEntity);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(cmsPiisConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByPsu_success_nullInstanceId() {
        // Given
        when(piisConsentEntitySpecification.byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData,
                                                                                      CREATION_DATE_FROM,
                                                                                      CREATION_DATE_TO,
                                                                                      DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        Page<ConsentEntity> page = new PageImpl<>(Collections.singletonList(consentEntity), PAGE_REQUEST, TOTAL);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        CmsPiisConsent cmsPiisConsent = buildCmsPiisConsent();
        when(piisConsentMapper.mapToCmsPiisConsent(consentEntity)).thenReturn(cmsPiisConsent);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);
        when(piisConsentLazyMigrationService.migrateIfNeeded(consentEntity)).thenReturn(consentEntity);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, null,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(cmsPiisConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByPsu_failure_wrongPsuIdData() {
        // Given
        when(piisConsentEntitySpecification.byPsuIdDataAndCreationPeriodAndInstanceId(wrongPsuIdData, CREATION_DATE_FROM,
                                                                                      CREATION_DATE_TO, SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        Page<ConsentEntity> page = new PageImpl<>(Collections.emptyList(), PAGE_REQUEST, 0);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(wrongPsuIdData, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(wrongPsuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByPsu_failure_nullPsuIdData() {
        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(null, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    void exportConsentsByPsu_failure_emptyPsuIdData() {
        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(buildEmptyPsuIdData(), CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                       PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    void exportConsentsByAccountId_success() {
        // Given
        when(piisConsentEntitySpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID,
                                                                                           CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO,
                                                                                           SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        Page<ConsentEntity> page = new PageImpl<>(Collections.singletonList(consentEntity), PAGE_REQUEST, TOTAL);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        CmsPiisConsent cmsPiisConsent = buildCmsPiisConsent();
        when(piisConsentMapper.mapToCmsPiisConsent(consentEntity)).thenReturn(cmsPiisConsent);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);
        when(piisConsentLazyMigrationService.migrateIfNeeded(consentEntity)).thenReturn(consentEntity);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                             PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(cmsPiisConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByAccountId__success_nullInstanceId() {
        // Given
        when(piisConsentEntitySpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID,
                                                                                           CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO,
                                                                                           DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        Page<ConsentEntity> page = new PageImpl<>(Collections.singletonList(consentEntity), PAGE_REQUEST, TOTAL);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        CmsPiisConsent cmsPiisConsent = buildCmsPiisConsent();
        when(piisConsentMapper.mapToCmsPiisConsent(consentEntity)).thenReturn(cmsPiisConsent);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);
        when(piisConsentLazyMigrationService.migrateIfNeeded(consentEntity)).thenReturn(consentEntity);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, null,
                                                                             PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(cmsPiisConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByAccountId_wrongAspspAccountId() {
        // Given
        when(piisConsentEntitySpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO, SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        Page<ConsentEntity> page = new PageImpl<>(Collections.emptyList(), PAGE_REQUEST, 0);
        when(consentJpaRepository.findAll(any(Specification.class), eq(PAGE_REQUEST))).thenReturn(page);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(PAGE_REQUEST);

        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                             PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    void exportConsentsByAccountId__failure_blankAspspAccountId() {
        // When
        PageData<Collection<CmsPiisConsent>> piisConsentsPage =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId("", CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, SERVICE_INSTANCE_ID,
                                                                             PAGE_INDEX, ITEMS_PER_PAGE);
        Collection<CmsPiisConsent> piisConsents = piisConsentsPage.getData();

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byAspspAccountIdAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    private ConsentEntity buildPiisConsentEntity() {
        ConsentEntity piisConsentEntity = new ConsentEntity();
        piisConsentEntity.setPsuDataList(Collections.singletonList(buildPsuData()));
        piisConsentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        return piisConsentEntity;
    }

    private CmsPiisConsent buildCmsPiisConsent() {
        CmsPiisConsent piisConsent = new CmsPiisConsent();
        piisConsent.setPsuData(buildPsuIdData(PSU_ID));
        piisConsent.setCreationTimestamp(CREATION_TIMESTAMP);
        return piisConsent;
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private PsuIdData buildEmptyPsuIdData() {
        return new PsuIdData(null, null, null, null, null);
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null, null);
    }
}
