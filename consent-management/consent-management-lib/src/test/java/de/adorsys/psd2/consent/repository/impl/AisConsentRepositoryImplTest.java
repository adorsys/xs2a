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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentRepositoryImplTest {
    private static final String CORRECT_PSU_ID = "987654321";
    private static final byte[] CHECKSUM = "checksum in consent".getBytes();

    private JsonReader jsonReader;
    private PsuData psuData;

    @InjectMocks
    private AisConsentRepositoryImpl aisConsentVerifyingRepository;
    @Mock
    private AisConsentJpaRepository aisConsentRepository;
    @Mock
    private ChecksumCalculatingFactory calculatingFactory;

    @Mock
    private ChecksumCalculatingService checksumCalculatingService;

    @BeforeEach
    void setUp() {
        when(calculatingFactory.getServiceByChecksum(any())).thenReturn(Optional.of(checksumCalculatingService));

        psuData = buildPsuData(CORRECT_PSU_ID);
        jsonReader = new JsonReader();
    }

    @Test
    void verifyAndSave_ReceivedToValidStatus_success() throws WrongChecksumException {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.RECEIVED, ConsentStatus.VALID);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        AisConsent actualResult = aisConsentVerifyingRepository.verifyAndSave(aisConsent);

        // then
        assertEquals(aisConsent, actualResult);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    @Test
    void verifyAndSave_failedSha() {
        // Given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndSave(aisConsent));

        // Then
        verify(aisConsentRepository, times(0)).save(aisConsent);
    }

    @Test
    void verifyAndSave_correctSha() throws WrongChecksumException {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        AisConsent actualResult = aisConsentVerifyingRepository.verifyAndSave(aisConsent);

        // then
        assertEquals(aisConsent, actualResult);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    @Test
    void verifyAndUpdate_success() throws WrongChecksumException {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        AisConsent actualResult = aisConsentVerifyingRepository.verifyAndUpdate(aisConsent);

        // then
        assertEquals(aisConsent, actualResult);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    @Test
    void verifyAndUpdate_failedSha() {
        // Given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndUpdate(aisConsent));

        // Then
        verify(aisConsentRepository, times(0)).save(aisConsent);
    }

    @Test
    void verifyAndSaveAll_success() throws WrongChecksumException {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        List<AisConsent> asList = Collections.singletonList(aisConsent);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        List<AisConsent> actualResult = aisConsentVerifyingRepository.verifyAndSaveAll(asList);

        // then
        assertEquals(asList, actualResult);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    private AisConsent buildConsent(ConsentStatus previousStatus, ConsentStatus currentStatus) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/AisConsent.json", AisConsent.class);

        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setValidUntil(LocalDate.now().plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuDataList(Collections.singletonList(psuData));
        aisConsent.setConsentStatus(currentStatus);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setAisConsentRequestType(AisConsentRequestType.BANK_OFFERED);
        aisConsent.setPreviousConsentStatus(previousStatus);

        return aisConsent;
    }

    private PsuData buildPsuData(String psuId) {
        return new PsuData(psuId, "", "", "", "");
    }
}
