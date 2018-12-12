/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPiisServiceInternalTest {
    @InjectMocks
    CmsPsuPiisServiceInternal cmsPsuPiisServiceInternal;
    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @Spy
    private PsuDataMapper psuDataMapper;

    private PiisConsent piisConsent;
    private PiisConsentEntity piisConsentEntity;
    private PsuIdData psuIdData;
    private PsuIdData psuIdDataNotExist;
    private PsuData psuData;
    private final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private final String EXTERNAL_CONSENT_ID_FINALISED = "4b112130-6a96-4941-a220-2da8a4af2c64";
    private final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";

    @Before
    public void setUp() {
        psuIdData = new PsuIdData("777", null, null, null);
        psuIdDataNotExist = new PsuIdData("000", null, null, null);
        psuData = psuDataMapper.mapToPsuData(psuIdData);
        piisConsentEntity = buildPiisConsentEntity(ConsentStatus.VALID);
        piisConsent = buildConsent();
        when(piisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(piisConsentEntity));
        when(piisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_FINALISED)).thenReturn(Optional.of(buildPiisConsentEntity(ConsentStatus.TERMINATED_BY_ASPSP)));
        when(piisConsentRepository.findByPsuDataPsuId(psuIdData.getPsuId())).thenReturn(Collections.singletonList(piisConsentEntity));
        when(piisConsentRepository.findByPsuDataPsuId(psuIdDataNotExist.getPsuId())).thenReturn(Collections.emptyList());
        when(piisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(piisConsentRepository.save(piisConsentEntity)).thenReturn(piisConsentEntity);
        when(piisConsentMapper.mapToPiisConsent(piisConsentEntity)).thenReturn(piisConsent);
    }

    @Test
    public void getConsent_success() {
        // Given
        // When
        Optional<PiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Then
        assertTrue(consent.isPresent());
        assertEquals(consent.get(), piisConsent);
    }

    @Test
    public void getConsent_fail() {
        // Given
        // When
        Optional<PiisConsent> consent = cmsPsuPiisServiceInternal.getConsent(psuIdData, EXTERNAL_CONSENT_ID_NOT_EXIST);
        // Then
        assertFalse(consent.isPresent());
    }

    @Test
    public void getConsentsForPsu_success() {
        // Given
        // When
        List<PiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdData);
        // Then
        assertFalse(consents.isEmpty());
        assertEquals(consents.size(), 1);
    }

    @Test
    public void getConsentsForPsu_fail() {
        // Given
        // When
        List<PiisConsent> consents = cmsPsuPiisServiceInternal.getConsentsForPsu(psuIdDataNotExist);
        // Then
        assertTrue(consents.isEmpty());
    }

    @Test
    public void revokeConsent_success() {
        // Given
        ArgumentCaptor<PiisConsentEntity> argumentCaptor = ArgumentCaptor.forClass(PiisConsentEntity.class);
        // When
        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID);
        // Then
        assertThat(revokeConsent).isTrue();
        verify(piisConsentRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getConsentStatus()).isEqualTo(ConsentStatus.REVOKED_BY_PSU);
    }

    @Test
    public void revokeConsent_fail_wrongPsu() {
        // Given
        // When
        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdDataNotExist, EXTERNAL_CONSENT_ID);
        // Then
        assertThat(revokeConsent).isFalse();
    }

    @Test
    public void revokeConsent_fail_statusFinalised() {
        // Given
        // When
        boolean revokeConsent = cmsPsuPiisServiceInternal.revokeConsent(psuIdData, EXTERNAL_CONSENT_ID_FINALISED);
        // Then
        assertThat(revokeConsent).isFalse();
    }

    private PiisConsentEntity buildPiisConsentEntity(ConsentStatus status) {
        PiisConsentEntity piisConsentEntity = new PiisConsentEntity();
        piisConsentEntity.setPsuData(psuData);
        piisConsentEntity.setConsentStatus(status);
        return piisConsentEntity;
    }

    private PiisConsent buildConsent() {
        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setPsuData(psuIdData);
        return piisConsent;
    }
}
