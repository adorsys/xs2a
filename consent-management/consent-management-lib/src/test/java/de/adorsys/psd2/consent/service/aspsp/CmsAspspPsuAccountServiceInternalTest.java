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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAspspPsuAccountServiceInternalTest {
    private static final String PSU_ID = "PSU-ID-1";
    private final String INSTANCE_ID = "Instance id";
    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private ConsentEntity aisConsent;
    private PiisConsentEntity piisConsentEntity;

    private JsonReader jsonReader = new JsonReader();
    @InjectMocks
    private CmsAspspPsuAccountServiceInternal cmsAspspPsuAccountServiceInternal;

    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/AisConsent.json", ConsentEntity.class);
        piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", PiisConsentEntity.class);
    }

    @Test
    void revokeAllConsents_Success_closeBothType() {
        // given
        when(aisConsentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.singletonList(aisConsent));

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.singletonList(piisConsentEntity));

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertTrue(actualResult);
        verify(consentJpaRepository, times(1)).save(aisConsent);
        verify(piisConsentRepository, times(1)).save(piisConsentEntity);
        verify(aisConsentSpecification).byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID);
    }

    @Test
    void revokeAllConsents_Success_closeAis() {
        // given
        when(aisConsentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.singletonList(aisConsent));

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.emptyList());

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertTrue(actualResult);
        verify(consentJpaRepository, times(1)).save(aisConsent);
        verify(piisConsentRepository, never()).save(any());
        verify(aisConsentSpecification).byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID);
    }

    @Test
    void revokeAllConsents_Success_closePiis() {
        // given
        when(aisConsentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.emptyList());

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.singletonList(piisConsentEntity));

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertTrue(actualResult);
        verify(consentJpaRepository, never()).save(any());
        verify(piisConsentRepository, times(1)).save(piisConsentEntity);
    }

    @Test
    void revokeAllConsents_NoConsents() {
        // given
        when(aisConsentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.emptyList());

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.emptyList());

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertFalse(actualResult);
        verify(consentJpaRepository, never()).save(any());
        verify(piisConsentRepository, never()).save(any());
    }
}
