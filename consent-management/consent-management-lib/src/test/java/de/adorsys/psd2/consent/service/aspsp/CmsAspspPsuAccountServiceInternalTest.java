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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPsuAccountServiceInternalTest {
    private static final String PSU_ID = "PSU-ID-1";
    private final String INSTANCE_ID = "Instance id";
    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private AisConsent aisConsent;
    private PiisConsentEntity piisConsentEntity;

    private JsonReader jsonReader = new JsonReader();
    @InjectMocks
    private CmsAspspPsuAccountServiceInternal cmsAspspPsuAccountServiceInternal;

    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;
    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;

    @Before
    public void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/AisConsent.json", AisConsent.class);
        piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", PiisConsentEntity.class);
    }

    @Test
    public void revokeAllConsents_Success_closeBothType() {
        // given
        when(aisConsentSpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentJpaRepository.findAll((any()))).thenReturn(Collections.singletonList(aisConsent));

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.singletonList(piisConsentEntity));

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertThat(actualResult).isTrue();
        verify(aisConsentJpaRepository, times(1)).save(aisConsent);
        verify(piisConsentRepository, times(1)).save(piisConsentEntity);
    }

    @Test
    public void revokeAllConsents_Success_closeAis() {
        // given
        when(aisConsentSpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentJpaRepository.findAll((any()))).thenReturn(Collections.singletonList(aisConsent));

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.emptyList());

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertThat(actualResult).isTrue();
        verify(aisConsentJpaRepository, times(1)).save(aisConsent);
        verify(piisConsentRepository, never()).save(any());
    }

    @Test
    public void revokeAllConsents_Success_closePiis() {
        // given
        when(aisConsentSpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentJpaRepository.findAll((any()))).thenReturn(Collections.emptyList());

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.singletonList(piisConsentEntity));

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertThat(actualResult).isTrue();
        verify(aisConsentJpaRepository, never()).save(any());
        verify(piisConsentRepository, times(1)).save(piisConsentEntity);
    }

    @Test
    public void revokeAllConsents_NoConsents() {
        // given
        when(aisConsentSpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(aisConsentJpaRepository.findAll((any()))).thenReturn(Collections.emptyList());

        when(piisConsentEntitySpecification.byAspspAccountIdAndPsuIdDataAndInstanceId(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(piisConsentRepository.findAll((any()))).thenReturn(Collections.emptyList());

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertThat(actualResult).isFalse();
        verify(aisConsentJpaRepository, never()).save(any());
        verify(piisConsentRepository, never()).save(any());
    }
}
