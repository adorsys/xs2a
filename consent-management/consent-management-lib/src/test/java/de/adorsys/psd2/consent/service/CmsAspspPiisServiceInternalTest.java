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

import de.adorsys.psd2.consent.aspsp.api.piis.PiisConsent;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.service.mapper.AccountReferenceMapper;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPiisServiceInternalTest {
    private static final long CONSENT_INTERNAL_ID = 1;
    private static final String CONSENT_EXTERNAL_ID = "5bcf664f-68ce-498d-9a93-fe0cce32f6b6";
    private static final String CONSENT_EXTERNAL_ID_WRONG = "efe6d8bd-c6bc-4866-81a3-87ac755ffa4b";
    private static final String PSU_ID = "PSU-ID-1";
    private static final String PSU_ID_WRONG = "PSU-ID-2";
    private static final LocalDate EXPIRE_DATE = LocalDate.now().plusDays(100);
    private static final int FREQUENCY_PER_DAY = 4;

    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AccountReferenceMapper accountReferenceMapper;
    @Mock
    private PiisConsentMapper piisConsentMapper;
    @InjectMocks
    private CmsAspspPiisServiceInternal cmsAspspPiisServiceInternal;

    @Before
    public void setUp() {
        when(psuDataMapper.mapToPsuData(buildPsuIdData())).thenReturn(buildPsuData());
        when(piisConsentMapper.mapToPiisConsent(buildPiisConsentEntity())).thenReturn(buildPiisConsent());
        when(piisConsentRepository.findByPsuDataPsuId(eq(PSU_ID))).thenReturn(buildPiisConsentEntityList());
        when(piisConsentRepository.findByExternalId(CONSENT_EXTERNAL_ID)).thenReturn(Optional.of(buildPiisConsentEntity()));
        when(piisConsentRepository.findByExternalId(CONSENT_EXTERNAL_ID_WRONG)).thenReturn(Optional.empty());
        when(piisConsentRepository.save(any(PiisConsentEntity.class))).thenReturn(buildPiisConsentEntity());
    }

    @Test
    public void createConsent_Success() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent());

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        List<AccountReference> accounts = buildAccountReferenceList();

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, null, accounts,
                                                                            EXPIRE_DATE, FREQUENCY_PER_DAY);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(StringUtils.isNotBlank(actual.get())).isTrue();
    }

    @Test
    public void createConsent_Failure_FailedToSave() {
        when(piisConsentRepository.save(any(PiisConsentEntity.class)))
            .thenReturn(buildConsent(null));

        // Given
        PsuIdData psuIdData = buildPsuIdData();
        List<AccountReference> accounts = buildAccountReferenceList();

        // When
        Optional<String> actual = cmsAspspPiisServiceInternal.createConsent(psuIdData, null, accounts,
                                                                            EXPIRE_DATE, FREQUENCY_PER_DAY);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void getConsentsForPsu_Success() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        PiisConsent expected = buildPiisConsent();

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData);

        // Then
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.get(0)).isEqualTo(expected);
    }

    @Test
    public void getConsentsForPsu_Failure_WrongPsuId() {
        // Given
        PsuIdData psuIdData = buildPsuIdData(PSU_ID_WRONG);

        // When
        List<PiisConsent> actual = cmsAspspPiisServiceInternal.getConsentsForPsu(psuIdData);

        // Then
        assertThat(actual.isEmpty()).isTrue();
    }

    @Test
    public void terminateConsent_Success() {
        ArgumentCaptor<PiisConsentEntity> argumentCaptor = ArgumentCaptor.forClass(PiisConsentEntity.class);

        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID);

        // Then
        assertThat(actual).isTrue();
        verify(piisConsentRepository).save(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getConsentStatus()).isEqualTo(ConsentStatus.TERMINATED_BY_ASPSP);
    }

    @Test
    public void terminateConsent_Failure_WrongConsentId() {
        // When
        boolean actual = cmsAspspPiisServiceInternal.terminateConsent(CONSENT_EXTERNAL_ID_WRONG);

        // Then
        assertThat(actual).isFalse();
        verify(piisConsentRepository, never()).save(any(PiisConsentEntity.class));
    }

    private PiisConsentEntity buildConsent() {
        return buildConsent(CONSENT_INTERNAL_ID);
    }

    private PiisConsentEntity buildConsent(Long id) {
        PiisConsentEntity piisConsent = new PiisConsentEntity();
        piisConsent.setId(id);
        piisConsent.setExternalId(CONSENT_EXTERNAL_ID);
        piisConsent.setRequestDateTime(OffsetDateTime.now());
        piisConsent.setPsuData(buildPsuData());
        piisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return piisConsent;
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null);
    }

    private PsuIdData buildPsuIdData() {
        return buildPsuIdData(PSU_ID);
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference("resourceId",
                                    "DE89370400440532013000",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
    }

    private List<AccountReference> buildAccountReferenceList() {
        return Collections.singletonList(buildAccountReference());
    }

    private List<PiisConsentEntity> buildPiisConsentEntityList() {
        return Collections.singletonList(buildPiisConsentEntity());
    }

    private PiisConsentEntity buildPiisConsentEntity() {
        PiisConsentEntity consentEntity = new PiisConsentEntity();
        consentEntity.setExternalId(CONSENT_EXTERNAL_ID);
        return consentEntity;
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent consent = new PiisConsent();
        consent.setId(CONSENT_EXTERNAL_ID);
        return consent;
    }
}
