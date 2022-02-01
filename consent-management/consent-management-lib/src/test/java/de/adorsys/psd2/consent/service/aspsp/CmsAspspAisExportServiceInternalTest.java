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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.api.ais.AdditionalTppInfo;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.pagination.data.PageRequestParameters;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAspspAisExportServiceInternalTest {
    public static final Integer PAGE_INDEX = 0;
    public static final Integer ITEMS_PER_PAGE = 20;
    public static final PageRequestParameters PAGE_PARAMETERS = new PageRequestParameters(0,20);
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
    @Spy
    private PageRequestBuilder pageRequestBuilder = new PageRequestBuilder();

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
            DEFAULT_SERVICE_INSTANCE_ID,
            AdditionalTppInfo.NONE
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.singletonList(consentEntity), PageRequest.of(PAGE_PARAMETERS.getPageIndex(), PAGE_PARAMETERS.getItemsPerPage()), 1));
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisations);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null,
                                                                 AdditionalTppInfo.NONE);

        // Then
        assertFalse(aisConsents.getData().isEmpty());
        assertTrue(aisConsents.getData().contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID,
                                                               AdditionalTppInfo.NONE);
    }

    @Test
    void exportConsentsByTpp_successPagination() {
        // Given
        ConsentEntity consentEntity = buildConsentEntity();

        when(aisConsentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(
            TPP_AUTHORISATION_NUMBER,
            CREATION_DATE_FROM,
            CREATION_DATE_TO,
            psuIdData,
            DEFAULT_SERVICE_INSTANCE_ID,
            AdditionalTppInfo.NONE
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(consentJpaRepository.findAll(any(Specification.class), eq(pageRequest)))
            .thenReturn(new PageImpl<>(Collections.singletonList(consentEntity), pageRequest, 1));
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisations);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, PAGE_PARAMETERS,
                                                                 AdditionalTppInfo.NONE);

        // Then
        assertFalse(aisConsents.getData().isEmpty());
        assertTrue(aisConsents.getData().contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID,
                                                               AdditionalTppInfo.NONE);
    }

    @Test
    void exportConsentsByTpp_failure_wrongTppAuthorisationNumber() {
        // Given
        when(aisConsentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(
            WRONG_TPP_AUTHORISATION_NUMBER,
            CREATION_DATE_FROM,
            CREATION_DATE_TO,
            psuIdData,
            DEFAULT_SERVICE_INSTANCE_ID
            ,
            AdditionalTppInfo.NONE)).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        //noinspection unchecked
        when(consentJpaRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 1));

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null,
                                                                 AdditionalTppInfo.NONE);

        // Then
        assertTrue(aisConsents.getData().isEmpty());
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID,
                                                               AdditionalTppInfo.NONE);
    }

    @Test
    void exportConsentsByTpp_failure_nullTppAuthorisationNumber() {
        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(null, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID, PAGE_PARAMETERS,
                                                                 AdditionalTppInfo.NONE);

        // Then
        assertTrue(aisConsents.getData().isEmpty());
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
                                                                                                   DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                   AdditionalTppInfo.NONE
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll(any(), eq(Pageable.unpaged()))).thenReturn(new PageImpl(Collections.singletonList(consentEntity),
                                                                                                  PageRequest.of(0, 20), 1));
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(psuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null,
                                                                                     AdditionalTppInfo.NONE);

        // Then
        assertFalse(aisConsents.getData().isEmpty());
        assertTrue(aisConsents.getData().contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID,
                                                                           AdditionalTppInfo.NONE);
    }


    @Test
    void exportConsentsByPsu_successPagination() {
        // Given
        ConsentEntity consentEntity = buildConsentEntity();

        when(aisConsentSpecification.byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData,
                                                                                                   CREATION_DATE_FROM,
                                                                                                   CREATION_DATE_TO,
                                                                                                   DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                   AdditionalTppInfo.NONE
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);

        when(consentJpaRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(new PageImpl<>(Collections.singletonList(consentEntity), pageRequest, 1));
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisations);
        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(psuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE,
                                                                                     AdditionalTppInfo.NONE);

        // Then
        assertFalse(aisConsents.getData().isEmpty());
        assertTrue(aisConsents.getData().contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(psuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID,
                                                                           AdditionalTppInfo.NONE);
    }

    @Test
    void exportConsentsByPsu_failure_wrongPsuIdData() {
        // Given
        when(aisConsentSpecification.byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(wrongPsuIdData,
                                                                                                   CREATION_DATE_FROM,
                                                                                                   CREATION_DATE_TO,
                                                                                                   DEFAULT_SERVICE_INSTANCE_ID,
                                                                                                   AdditionalTppInfo.NONE
        )).thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);

        when(consentJpaRepository.findAll(any(Specification.class), eq(Pageable.unpaged())))
            .thenReturn(new PageImpl<>(Collections.emptyList(), pageRequest, 0));

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(wrongPsuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null,
                                                                                     AdditionalTppInfo.NONE);

        // Then
        assertTrue(aisConsents.getData().isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(wrongPsuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID,
                                                                           AdditionalTppInfo.NONE);
    }

    @Test
    void exportConsentsByPsu_failure_nullPsuIdData() {
        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(null, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE,
                                                                                     AdditionalTppInfo.NONE);

        // Then
        assertTrue(aisConsents.getData().isEmpty());
        verify(aisConsentSpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByPsu_failure_emptyPsuIdData() {
        // Given
        PsuIdData emptyPsuIdData = new PsuIdData(null, null, null, null, null);

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsuAndAdditionalTppInfo(emptyPsuIdData, CREATION_DATE_FROM,
                                                                                     CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE,
                                                                                     AdditionalTppInfo.NONE);

        // Then
        assertTrue(aisConsents.getData().isEmpty());
        verify(aisConsentSpecification, never()).byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(any(), any(), any(), any(), any());
    }

    @Test
    void exportConsentsByAccountId_success() {
        // Given
        ZoneOffset currentOffset = OffsetDateTime.now().getOffset();
        ConsentEntity consentEntity = buildConsentEntity();
        when(consentJpaRepository.findAllWithPaginationAndTppInfo(Collections.singleton(ConsentType.AIS.getName()), ASPSP_ACCOUNT_ID,
                                                                  OffsetDateTime.of(CREATION_DATE_FROM, LocalTime.MIN, currentOffset),
                                                                  OffsetDateTime.of(CREATION_DATE_TO, LocalTime.MAX, currentOffset),
                                                                  DEFAULT_SERVICE_INSTANCE_ID, Pageable.unpaged(), AdditionalTppInfo.NONE))
            .thenReturn(new PageImpl<>(Collections.singletonList(consentEntity), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 1));
        List<AuthorisationEntity> authorisations = Collections.singletonList(new AuthorisationEntity());
        when(authorisationRepository.findAllByParentExternalIdAndType(EXTERNAL_CONSENT_ID, AuthorisationType.CONSENT))
            .thenReturn(authorisations);
        when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
            .thenReturn(consentEntity);

        when(aisConsentMapper.mapToCmsAisAccountConsent(consentEntity, authorisations))
            .thenReturn(buildAisAccountConsent());
        CmsAisAccountConsent expectedConsent = buildAisAccountConsent();

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountIdAndAdditionalTppInfo(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID, null, null,
                                                                                           AdditionalTppInfo.NONE);

        // Then
        assertFalse(aisConsents.getData().isEmpty());
        assertTrue(aisConsents.getData().contains(expectedConsent));
    }

    @Test
    void exportConsentsByAccountId_success_withNoInstanceId() {
        PageData<Collection<CmsAisAccountConsent>> cmsAisAccountConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountIdAndAdditionalTppInfo(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO, "",
                                                                                           PAGE_INDEX, ITEMS_PER_PAGE,
                                                                                           AdditionalTppInfo.NONE);

        assertEquals(Collections.emptyList(), cmsAisAccountConsents.getData());
    }

    @Test
    void exportConsentsByAccountId_failure_wrongAspspAccountId() {
        ZoneOffset currentOffset = OffsetDateTime.now().getOffset();
        when(consentJpaRepository.findAllWithPaginationAndTppInfo(Collections.singleton(ConsentType.AIS.getName()), WRONG_ASPSP_ACCOUNT_ID,
                                                                  OffsetDateTime.of(CREATION_DATE_FROM, LocalTime.MIN, currentOffset),
                                                                  OffsetDateTime.of(CREATION_DATE_TO, LocalTime.MAX, currentOffset),
                                                                  DEFAULT_SERVICE_INSTANCE_ID, Pageable.unpaged(), AdditionalTppInfo.NONE))
            .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE), 0));

        // When
        PageData<Collection<CmsAisAccountConsent>> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountIdAndAdditionalTppInfo(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                                           CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID,
                                                                                           null, null,
                                                                                           AdditionalTppInfo.NONE);

        // Then
        assertTrue(aisConsents.getData().isEmpty());
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private CmsAisAccountConsent buildAisAccountConsent() {
        return new CmsAisAccountConsent(EXTERNAL_CONSENT_ID,
                                        null, false,
                                        null, null, 0,
                                        null, null,
                                        false, false, null, null, null, null, false, Collections.emptyList(), Collections.emptyMap(), CREATION_DATE_TIME, STATUS_CHANGE_DATE_TIME, null,
                                        AdditionalTppInfo.NONE);
    }

    private ConsentEntity buildConsentEntity() {
        return jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);
    }
}
