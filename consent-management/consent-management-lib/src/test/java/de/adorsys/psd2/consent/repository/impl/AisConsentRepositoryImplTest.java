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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingFactory;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentRepositoryImplTest {
    private static final byte[] CHECKSUM = "checksum in consent".getBytes();

    private AisConsent aisConsent;
    private ConsentEntity consentEntity;
    private JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AisConsentRepositoryImpl aisConsentVerifyingRepository;

    @Mock
    private ConsentJpaRepository aisConsentRepository;
    @Mock
    private ChecksumCalculatingFactory calculatingFactory;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private ChecksumCalculatingService checksumCalculatingService;

    @BeforeEach
    void setUp() {
        consentEntity = buildConsentEntity(ConsentStatus.VALID);
        aisConsent = jsonReader.getObjectFromFile("json/ais-consent.json", AisConsent.class);
    }

    @Test
    void verifyAndSave_ReceivedToValidStatus_success() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        ConsentEntity previousConsentEntity = buildConsentEntity(ConsentStatus.RECEIVED);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(previousConsentEntity));
        when(aisConsentRepository.save(consentEntity))
            .thenReturn(consentEntity);

        // When
        ConsentEntity actualResult = aisConsentVerifyingRepository.verifyAndSave(consentEntity);

        // Then
        assertEquals(consentEntity, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void verifyAndSave_finalisedStatus_failedSha() {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any()))
            .thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM))
            .thenReturn(false);

        consentEntity.setConsentStatus(ConsentStatus.REJECTED);
        consentEntity.setChecksum(CHECKSUM);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndSave(consentEntity));

        // Then
        verify(aisConsentRepository, times(0)).save(consentEntity);
    }

    @Test
    void verifyAndSave_failedSha() {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any()))
            .thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM))
            .thenReturn(false);

        consentEntity.setChecksum(CHECKSUM);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndSave(consentEntity));

        // Then
        verify(aisConsentRepository, times(0)).save(consentEntity);
    }

    @Test
    void verifyAndSave_correctSha() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any()))
            .thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM))
            .thenReturn(true);
        when(aisConsentRepository.save(consentEntity))
            .thenReturn(consentEntity);

        consentEntity.setChecksum(CHECKSUM);

        // When
        ConsentEntity actualResult = aisConsentVerifyingRepository.verifyAndSave(consentEntity);

        // Then
        assertEquals(consentEntity, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void verifyAndUpdate_success() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any()))
            .thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM))
            .thenReturn(true);
        when(aisConsentRepository.save(consentEntity))
            .thenReturn(consentEntity);

        consentEntity.setChecksum(CHECKSUM);

        // When
        ConsentEntity actualResult = aisConsentVerifyingRepository.verifyAndUpdate(consentEntity);

        // Then
        assertEquals(consentEntity, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void verifyAndUpdate_failedSha() {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any()))
            .thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM))
            .thenReturn(false);

        consentEntity.setChecksum(CHECKSUM);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndUpdate(consentEntity));

        // Then
        verify(aisConsentRepository, times(0)).save(consentEntity);
    }

    @Test
    void verifyAndSaveAll_success() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS)))
            .thenReturn(Optional.of(checksumCalculatingService));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM))
            .thenReturn(true);
        when(aisConsentRepository.save(consentEntity))
            .thenReturn(consentEntity);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any()))
            .thenReturn(aisConsent);

        consentEntity.setChecksum(CHECKSUM);
        List<ConsentEntity> asList = Collections.singletonList(consentEntity);

        // When
        List<ConsentEntity> actualResult = aisConsentVerifyingRepository.verifyAndSaveAll(asList);

        // Then
        assertEquals(asList, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void getActualAisConsent_success() {
        // Given
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));

        // When
        Optional<ConsentEntity> optionalConsentEntity = aisConsentVerifyingRepository.getActualAisConsent(consentEntity.getExternalId());

        // Then
        assertEquals(Optional.of(consentEntity), optionalConsentEntity);
    }

    @Test
    void getActualAisConsent_empty() {
        consentEntity.setConsentStatus(ConsentStatus.REJECTED);
        // Given
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId()))
            .thenReturn(Optional.of(consentEntity));

        // When
        Optional<ConsentEntity> optionalConsentEntity = aisConsentVerifyingRepository.getActualAisConsent(consentEntity.getExternalId());

        // Then
        assertEquals(Optional.empty(), optionalConsentEntity);
    }

    private ConsentEntity buildConsentEntity(ConsentStatus currentStatus) {
        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);
        consentEntity.setConsentStatus(currentStatus);

        return consentEntity;
    }
}
