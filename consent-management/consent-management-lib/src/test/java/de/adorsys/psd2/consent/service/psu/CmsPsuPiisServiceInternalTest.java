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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PageRequestBuilder;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuPiisServiceInternalTest {
    public static final Integer PAGE_INDEX = 0;
    public static final Integer ITEMS_PER_PAGE = 20;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_ID_FINALISED = "4b112130-6a96-4941-a220-2da8a4af2c64";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    private CmsPiisConsent cmsPiisConsent;
    private ConsentEntity piisConsentEntity;
    private PsuIdData psuIdData;
    private PsuIdData psuIdDataNotExist;
    private PsuData psuData;

    @InjectMocks
    private CmsPsuPiisServiceInternal cmsPsuPiisServiceInternal;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private PiisConsentLazyMigrationService piisConsentLazyMigrationService;
    @Mock
    private PageRequestBuilder pageRequestBuilder;
    @Mock
    private Specification specification;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        psuIdDataNotExist = jsonReader.getObjectFromFile("json/service/psu/piis/psu-data-invalid.json", PsuIdData.class);
        psuData = jsonReader.getObjectFromFile("json/service/psu/piis/psu-data.json", PsuData.class);
        psuIdData = jsonReader.getObjectFromFile("json/service/psu/piis/psu-data.json", PsuIdData.class);
        piisConsentEntity = buildPiisConsentEntity();
        cmsPiisConsent = buildCmsPiisConsent();
    }

    @Test
    void getConsent_success() {
        // Given
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);
        when(piisConsentMapper.mapToCmsPiisConsent(piisConsentEntity)).thenReturn(cmsPiisConsent);

        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any())).thenReturn(Optional.ofNullable(piisConsentEntity));
        when(piisConsentLazyMigrationService.migrateIfNeeded(piisConsentEntity)).thenReturn(piisConsentEntity);

        // When
        Optional<CmsPiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), cmsPiisConsent);
    }

    @Test
    void getConsent_shouldIgnoreIpAddressDifferenceInPsu() {
        // Given
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);
        when(piisConsentMapper.mapToCmsPiisConsent(piisConsentEntity)).thenReturn(cmsPiisConsent);

        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any())).thenReturn(Optional.ofNullable(piisConsentEntity));
        when(piisConsentLazyMigrationService.migrateIfNeeded(piisConsentEntity)).thenReturn(piisConsentEntity);
        PsuIdData psuIdDataWithIp = jsonReader.getObjectFromFile("json/service/psu/piis/psu-data-ip-address.json", PsuIdData.class);

        // When
        Optional<CmsPiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdDataWithIp, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), cmsPiisConsent);
    }

    @Test
    void getConsent_wrongPsu_shouldReturnEmpty() {
        // Given
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        PsuData invalidPsuData = jsonReader.getObjectFromFile("json/service/psu/piis/psu-data-invalid.json", PsuData.class);
        when(psuDataMapper.mapToPsuIdData(invalidPsuData)).thenReturn(psuIdDataNotExist);
        ConsentEntity consentEntityWithInvalidPsu = buildPiisConsentEntity(invalidPsuData);
        when(consentJpaRepository.findOne(any())).thenReturn(Optional.of(consentEntityWithInvalidPsu));

        // When
        Optional<CmsPiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consent.isPresent());
    }

    @Test
    void getConsent_nullPsu_shouldReturnEmpty() {
        // Given
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntityWithInvalidPsu = buildPiisConsentEntity(null);
        when(consentJpaRepository.findOne(any())).thenReturn(Optional.of(consentEntityWithInvalidPsu));

        // When
        Optional<CmsPiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consent.isPresent());
    }

    @Test
    void getConsent_fail() {
        Optional<CmsPiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(consent.isPresent());
    }

    @Test
    void getConsentsForPsu_success() {
        // Given
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll(any())).thenReturn(Collections.singletonList(piisConsentEntity));

        // When
        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, null, null);

        // Then
        assertFalse(consents.isEmpty());
        assertEquals(1, consents.size());
    }


    @Test
    void getConsentsForPsu_successPagination() {
        // Given
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdData, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(pageRequest);

        when(consentJpaRepository.findAll(any(), eq(pageRequest))).thenReturn(new PageImpl<>(Collections.singletonList(piisConsentEntity)));

        // When
        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertFalse(consents.isEmpty());
        assertEquals(1, consents.size());
    }

    @Test
    void getConsentsForPsu_success_shouldIgnoreIpAddressDifferenceInPsu() {
        // Given
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);
        PsuIdData psuIdDataWithIp = jsonReader.getObjectFromFile("json/service/psu/piis/psu-data-ip-address.json", PsuIdData.class);
        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdDataWithIp, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(pageRequest);
        when(consentJpaRepository.findAll(any(), eq(pageRequest))).thenReturn(new PageImpl(Collections.singletonList(piisConsentEntity)));

        // When
        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdDataWithIp, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertFalse(consents.isEmpty());
        assertEquals(1, consents.size());
    }

    @Test
    void getConsentsForPsu_fail() {
        PageRequest pageRequest = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);
        when(pageRequestBuilder.getPageable(PAGE_INDEX, ITEMS_PER_PAGE)).thenReturn(pageRequest);

        when(piisConsentEntitySpecification.byPsuDataAndInstanceId(psuIdDataNotExist, "UNDEFINED")).thenReturn(specification);
        when(consentJpaRepository.findAll(specification, pageRequest)).thenReturn(Page.empty());

        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdDataNotExist, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);

        assertTrue(consents.isEmpty());
    }

    @Test
    void revokeConsent() {
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(consentEntity));
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);

        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertTrue(revokeConsent);
        verify(piisConsentEntitySpecification).byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    void revokeConsent_fail_wrongPsu() {
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntity = buildPiisConsentEntity();
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(consentEntity));
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);

        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdDataNotExist, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(revokeConsent);
        verify(consentJpaRepository, never()).save(any());
    }

    @Test
    void revokeConsent_fail_statusFinalised() {
        when(piisConsentEntitySpecification.byConsentIdAndInstanceId(EXTERNAL_CONSENT_ID_FINALISED, DEFAULT_SERVICE_INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity consentEntityWithFinalisedStatus = buildFinalisedPiisConsentEntity();
        //noinspection unchecked
        when(consentJpaRepository.findOne(any(Specification.class))).thenReturn(Optional.of(consentEntityWithFinalisedStatus));
        when(psuDataMapper.mapToPsuIdData(psuData)).thenReturn(psuIdData);

        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID_FINALISED, DEFAULT_SERVICE_INSTANCE_ID);

        assertFalse(revokeConsent);
        verify(consentJpaRepository, never()).save(any());
    }

    private ConsentEntity buildFinalisedPiisConsentEntity() {
        ConsentEntity consentEntity = buildPiisConsentEntity();
        consentEntity.setConsentStatus(ConsentStatus.EXPIRED);
        return consentEntity;
    }

    private ConsentEntity buildPiisConsentEntity() {
        return buildPiisConsentEntity(psuData);
    }

    private ConsentEntity buildPiisConsentEntity(PsuData customPsuData) {
        ConsentEntity piisConsentEntity = new ConsentEntity();
        piisConsentEntity.setPsuDataList(Collections.singletonList(customPsuData));
        piisConsentEntity.setConsentStatus(ConsentStatus.VALID);
        return piisConsentEntity;
    }

    private CmsPiisConsent buildCmsPiisConsent() {
        CmsPiisConsent piisConsent = new CmsPiisConsent();
        piisConsent.setPsuData(psuIdData);
        return piisConsent;
    }
}
