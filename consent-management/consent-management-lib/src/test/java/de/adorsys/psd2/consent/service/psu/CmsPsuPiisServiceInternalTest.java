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

import de.adorsys.psd2.consent.api.piis.CmsPiisConsent;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.PiisConsentLazyMigrationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsPsuPiisServiceInternalTest {
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
        // Given
        // When
        Optional<CmsPiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
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
        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
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
        when(consentJpaRepository.findAll(any())).thenReturn(Collections.singletonList(piisConsentEntity));

        // When
        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdDataWithIp, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(consents.isEmpty());
        assertEquals(1, consents.size());
    }

    @Test
    void getConsentsForPsu_fail() {
        // Given
        // When
        List<CmsPiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdDataNotExist, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        assertTrue(consents.isEmpty());
    }

    @Test
    void revokeConsent_fail_wrongPsu() {
        // Given
        // When
        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdDataNotExist, EXTERNAL_CONSENT_ID, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        assertFalse(revokeConsent);
    }

    @Test
    void revokeConsent_fail_statusFinalised() {
        // Given
        // When
        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID_FINALISED, DEFAULT_SERVICE_INSTANCE_ID);
        // Then
        assertFalse(revokeConsent);
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
