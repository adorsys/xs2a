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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentRepositoryImplTest {
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

    @Before
    public void setUp() {
        when(calculatingFactory.getServiceByChecksum(any())).thenReturn(Optional.of(checksumCalculatingService));

        psuData = buildPsuData(CORRECT_PSU_ID);
        jsonReader = new JsonReader();
    }

    @Test
    public void verifyAndSave_ReceivedToValidStatus_success() {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.RECEIVED, ConsentStatus.VALID);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        AisConsent actualResult = aisConsentVerifyingRepository.verifyAndSave(aisConsent);

        // then
        assertThat(actualResult).isEqualTo(aisConsent);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    @Test
    public void verifyAndSave_failedSha() {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);

        // when
        aisConsentVerifyingRepository.verifyAndSave(aisConsent);
        verify(aisConsentRepository, times(0)).save(aisConsent);
    }

    @Test
    public void verifyAndSave_correctSha() {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        AisConsent actualResult = aisConsentVerifyingRepository.verifyAndSave(aisConsent);

        // then
        assertThat(actualResult).isEqualTo(aisConsent);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    @Test
    public void verifyAndUpdate_success() {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        AisConsent actualResult = aisConsentVerifyingRepository.verifyAndUpdate(aisConsent);

        // then
        assertThat(actualResult).isEqualTo(aisConsent);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    @Test
    public void verifyAndUpdate_failedSha() {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);

        // when
        aisConsentVerifyingRepository.verifyAndUpdate(aisConsent);
        verify(aisConsentRepository, times(0)).save(aisConsent);
    }

    @Test
    public void verifyAndSaveAll_success() {
        // given
        AisConsent aisConsent = buildConsent(ConsentStatus.VALID, ConsentStatus.VALID);
        aisConsent.setChecksum(CHECKSUM);
        List<AisConsent> asList = Collections.singletonList(aisConsent);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(aisConsent)).thenReturn(aisConsent);

        // when
        List<AisConsent> actualResult = aisConsentVerifyingRepository.verifyAndSaveAll(asList);

        // then
        assertThat(actualResult).isEqualTo(asList);
        verify(aisConsentRepository, times(1)).save(aisConsent);
    }

    private AisConsent buildConsent(ConsentStatus previousStatus, ConsentStatus currentStatus) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/AisConsent.json", AisConsent.class);

        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setExpireDate(LocalDate.now().plusDays(1));
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
