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

package de.adorsys.psd2.consent.service.migration;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.migration.ObsoletePiisConsentJpaRepository;
import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PiisConsentLazyMigrationServiceTest {
    private static final String EXTERNAL_ID = "4c4e9624-9eb6-4d3f-86cd-7f70a11c3b5e";

    @InjectMocks
    private PiisConsentLazyMigrationService piisConsentLazyMigrationService;

    @Mock
    private ObsoletePiisConsentJpaRepository obsoletePiisConsentJpaRepository;
    @Mock
    private ConsentJpaRepository consentJpaRepository;

    @Spy
    private ConsentDataMapper consentDataMapper = new ConsentDataMapper();

    private JsonReader jsonReader = new JsonReader();

    @Test
    void migrateIfNeeded() {
        // Given
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(EXTERNAL_ID);

        PiisConsentEntity obsoletePiisConsent = jsonReader.getObjectFromFile("json/service/migration/piis-consent-entity.json", PiisConsentEntity.class);
        when(obsoletePiisConsentJpaRepository.findByExternalId(EXTERNAL_ID))
            .thenReturn(Optional.of(obsoletePiisConsent));

        // When
        piisConsentLazyMigrationService.migrateIfNeeded(consentEntity);

        // Then
        verify(obsoletePiisConsentJpaRepository, times(1)).findByExternalId(EXTERNAL_ID);
        verify(consentJpaRepository, times(1)).save(consentEntity);

        assertNotNull(consentEntity.getData());
        PiisConsentData actual = consentDataMapper.mapToPiisConsentData(consentEntity.getData());

        PiisConsentData expected = jsonReader.getObjectFromFile("json/service/migration/piis-consent.json", PiisConsentData.class);
        assertEquals(expected, actual);
    }

    @Test
    void migrateIfNeeded_consentDataExists() {
        // Given
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setData("data".getBytes());

        // When
        piisConsentLazyMigrationService.migrateIfNeeded(consentEntity);

        // Then
        verify(obsoletePiisConsentJpaRepository, never()).findByExternalId(any());
        verify(consentJpaRepository, never()).save(any());
    }
}
