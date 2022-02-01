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

import de.adorsys.psd2.consent.domain.AdditionalPsuData;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.migration.AisConsentLazyMigrationService;
import de.adorsys.psd2.consent.service.psu.util.PsuDataUpdater;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuConsentServiceInternalTest {
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String CORRECT_PSU_ID = "anton.brueckner";
    private static final String INSTANCE_ID = "UNDEFINED";

    @InjectMocks
    private CmsPsuConsentServiceInternal cmsPsuConsentServiceInternal;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private CmsPsuService cmsPsuService;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AisConsentLazyMigrationService aisConsentLazyMigrationService;
    @Mock
    private PsuDataUpdater psuDataUpdater;

    private ConsentEntity consentEntity;
    private PsuIdData psuIdData;
    private PsuData psuData;
    private JsonReader jsonReader;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData(CORRECT_PSU_ID);
        psuData = new PsuData(CORRECT_PSU_ID, "", "", "", "");
        jsonReader = new JsonReader();
        consentEntity = buildConsent();
    }

    @Test
    void updatePsuData_psuIdIsBlank() {
        //Given
        when(psuDataMapper.mapToPsuData(psuIdData, INSTANCE_ID))
            .thenReturn(buildPsuData(""));
        AuthorisationEntity authorisation = buildAisConsentAuthorisation(null);
        //When
        boolean updatePsuData = cmsPsuConsentServiceInternal.updatePsuData(authorisation, psuIdData, ConsentType.AIS);
        //Then
        assertFalse(updatePsuData);
        verify(authorisationRepository, never()).save(authorisation);
        verify(aisConsentLazyMigrationService, never()).migrateIfNeeded(consentEntity);
    }

    @ParameterizedTest
    @EnumSource(ConsentType.class)
    void updatePsuData_noPsuDataInAuthorisation_success(ConsentType consentType) {
        //Given
        when(psuDataMapper.mapToPsuData(psuIdData, INSTANCE_ID))
            .thenReturn(psuData);
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.of(consentEntity));
        if (consentType == ConsentType.AIS) {
            when(aisConsentLazyMigrationService.migrateIfNeeded(consentEntity))
                .thenReturn(consentEntity);
        }

        when(cmsPsuService.definePsuDataForAuthorisation(psuData, Collections.singletonList(psuData)))
            .thenReturn(Optional.of(psuData));
        AuthorisationEntity authorisation = buildAisConsentAuthorisation(null);
        //When
        boolean updatePsuData = cmsPsuConsentServiceInternal.updatePsuData(authorisation, psuIdData, consentType);
        //Then
        assertTrue(updatePsuData);
        verify(authorisationRepository, never()).save(authorisation);
        verify(aisConsentLazyMigrationService, consentType == ConsentType.AIS ? atLeastOnce() : never()).migrateIfNeeded(consentEntity);
        assertEquals(psuData, authorisation.getPsuData());
    }

    @Test
    void updatePsuData_psuDataInAuthorisation_success() {
        //Given
        PsuData psuDataRequest = new PsuData(CORRECT_PSU_ID, "", "", "", "", buildAdditionalPsuData(null));
        when(psuDataMapper.mapToPsuData(psuIdData, INSTANCE_ID))
            .thenReturn(psuDataRequest);
        PsuData psuData = new PsuData(CORRECT_PSU_ID, "", "", "", "", buildAdditionalPsuData(5L));
        psuData.setId(7L);
        AuthorisationEntity authorisation = buildAisConsentAuthorisation(psuData);
        when(psuDataUpdater.updatePsuDataEntity(psuData, psuDataRequest)).thenReturn(psuData);
        //When
        boolean updatePsuData = cmsPsuConsentServiceInternal.updatePsuData(authorisation, psuIdData, ConsentType.AIS);
        //Then
        assertTrue(updatePsuData);
        verify(aisConsentLazyMigrationService, never()).migrateIfNeeded(consentEntity);
        assertEquals(psuData, authorisation.getPsuData());
    }

    @Test
    void updatePsuData_noConsentAuthorisation_failed() {
        //Given
        when(psuDataMapper.mapToPsuData(psuIdData, INSTANCE_ID))
            .thenReturn(psuData);
        when(consentJpaRepository.findByExternalId(EXTERNAL_CONSENT_ID))
            .thenReturn(Optional.empty());
        AuthorisationEntity authorisation = buildAisConsentAuthorisation(null);
        //When
        boolean updatePsuData = cmsPsuConsentServiceInternal.updatePsuData(authorisation, psuIdData, ConsentType.AIS);
        //Then
        assertFalse(updatePsuData);
        verify(authorisationRepository, never()).save(authorisation);
        verify(aisConsentLazyMigrationService, never()).migrateIfNeeded(consentEntity);
    }

    private AdditionalPsuData buildAdditionalPsuData(Long id) {
        AdditionalPsuData additionalPsuData = new AdditionalPsuData();
        additionalPsuData.setId(id);
        additionalPsuData.setPsuIpPort("1.1.1.1");
        return additionalPsuData;
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, "", "", "", "");
    }

    private PsuData buildPsuData(String psuId) {
        return new PsuData(psuId, "", "", "", "");
    }

    private AuthorisationEntity buildAisConsentAuthorisation(PsuData psuData) {
        AuthorisationEntity aisConsentAuthorization = new AuthorisationEntity();
        aisConsentAuthorization.setType(AuthorisationType.CONSENT);
        aisConsentAuthorization.setExternalId(AUTHORISATION_ID);
        aisConsentAuthorization.setScaStatus(ScaStatus.RECEIVED);
        aisConsentAuthorization.setParentExternalId(EXTERNAL_CONSENT_ID);
        aisConsentAuthorization.setAuthorisationExpirationTimestamp(OffsetDateTime.now().plusDays(1));
        aisConsentAuthorization.setPsuData(psuData);
        return aisConsentAuthorization;
    }

    private ConsentEntity buildConsent() {
        ConsentEntity aisConsent = jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
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
}
