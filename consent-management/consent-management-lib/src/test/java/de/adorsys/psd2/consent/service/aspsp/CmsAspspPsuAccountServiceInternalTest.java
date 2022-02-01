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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.ConsentSpecification;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private ConsentEntity piisConsentEntity;
    private List<ConsentEntity> allConsents;

    private JsonReader jsonReader = new JsonReader();
    @InjectMocks
    private CmsAspspPsuAccountServiceInternal cmsAspspPsuAccountServiceInternal;

    @Mock
    private ConsentSpecification consentSpecification;
    @Mock
    private ConsentJpaRepository consentJpaRepository;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/AisConsent.json", ConsentEntity.class);
        piisConsentEntity = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-entity.json", ConsentEntity.class);
        allConsents = Arrays.asList(aisConsent, piisConsentEntity);
    }

    @Test
    void revokeAllConsents_Success_closeBothType() {
        // given
        when(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(allConsents);

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertTrue(actualResult);
        verify(consentSpecification).byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID);
    }

    @Test
    void revokeAllConsents_Success_closeAis() {
        // given
        when(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.singletonList(aisConsent));

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertTrue(actualResult);
        verify(consentSpecification).byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID);
    }

    @Test
    void revokeAllConsents_Success_closePiis() {
        // given
        when(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.singletonList(piisConsentEntity));

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertTrue(actualResult);
    }

    @Test
    void revokeAllConsents_NoConsents() {
        // given
        when(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findAll((any()))).thenReturn(Collections.emptyList());

        // when
        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        //then
        assertFalse(actualResult);
        verify(consentJpaRepository, never()).save(any());
    }

    @Test
    void revokeAllConsents_withFinalisedConsents_shouldIgnoreFinalised() {
        when(consentSpecification.byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        ConsentEntity finalisedConsent = jsonReader.getObjectFromFile("json/service/aspsp/consent-entity-finalised.json", ConsentEntity.class);
        List<ConsentEntity> consentEntities = Arrays.asList(piisConsentEntity, finalisedConsent);
        when(consentJpaRepository.findAll((any()))).thenReturn(consentEntities);

        boolean actualResult = cmsAspspPsuAccountServiceInternal.revokeAllConsents(ASPSP_ACCOUNT_ID, PSU_ID_DATA, INSTANCE_ID);

        assertTrue(actualResult);
        verify(consentSpecification).byPsuIdDataAndAspspAccountIdAndInstanceId(PSU_ID_DATA, ASPSP_ACCOUNT_ID, INSTANCE_ID);
        verifyNoMoreInteractions(consentJpaRepository);
    }
}
