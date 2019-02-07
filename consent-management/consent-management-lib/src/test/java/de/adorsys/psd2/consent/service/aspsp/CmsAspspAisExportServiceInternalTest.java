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

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspAisExportServiceInternalTest {
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "psu id";
    private static final String WRONG_PSU_ID = "wrong psu id";
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final String WRONG_ASPSP_ACCOUNT_ID = "wrong aspsp account id";

    private PsuIdData psuIdData;
    private PsuIdData wrongPsuIdData;
    private PsuData psuData;

    @InjectMocks
    private CmsAspspAisExportServiceInternal cmsAspspAisExportServiceInternal;
    @Mock
    private AisConsentSpecification aisConsentSpecification;
    @Mock
    private AisConsentRepository aisConsentRepository;
    @Mock
    private AisConsentMapper aisConsentMapper;

    @Before
    public void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);
        psuData = buildPsuData();

        when(aisConsentMapper.mapToAisAccountConsent(buildAisConsent())).thenReturn(buildAisAccountConsent());
    }

    @Test
    public void exportConsentsByTpp_success() {
        // Given
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildAisConsent()));
        AisAccountConsent expectedConsent = buildAisAccountConsent();

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(aisConsents.isEmpty());
        assertTrue(aisConsents.contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByTpp_failure_wrongTppAuthorisationNumber() {
        // Given
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.emptyList());

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByTpp_failure_nullTppAuthorisationNumber() {
        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByTpp(null, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByPsu_success() {
        // Given
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildAisConsent()));
        AisAccountConsent expectedConsent = buildAisAccountConsent();

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(aisConsents.isEmpty());
        assertTrue(aisConsents.contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByPsu_failure_wrongPsuIdData() {
        // Given
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.emptyList());

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsu(wrongPsuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(wrongPsuIdData, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByPsu_failure_nullPsuIdData() {
        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsu(null, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByPsu_failure_emptyPsuIdData() {
        // Given
        PsuIdData emptyPsuIdData = buildEmptyPsuIdData();

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByPsu(emptyPsuIdData, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, never()).byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByAccountId_success() {
        // Given
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildAisConsent()));
        AisAccountConsent expectedConsent = buildAisAccountConsent();

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(aisConsents.isEmpty());
        assertTrue(aisConsents.contains(expectedConsent));
        verify(aisConsentSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByAccountId_failure_wrongAspspAccountId() {
        // Given
        //noinspection unchecked
        when(aisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.emptyList());

        // When
        Collection<AisAccountConsent> aisConsents =
            cmsAspspAisExportServiceInternal.exportConsentsByAccountId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                 CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(aisConsents.isEmpty());
        verify(aisConsentSpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private PsuIdData buildEmptyPsuIdData() {
        return new PsuIdData(null, null, null, null);
    }

    private PsuData buildPsuData() {
        return new PsuData(PSU_ID, null, null, null);
    }

    private AisAccountConsent buildAisAccountConsent() {
        return new AisAccountConsent(EXTERNAL_CONSENT_ID,
                                     null, false,
                                     null, 0,
                                     null, null,
                                     false, false, null, null, null);
    }

    private AisConsent buildAisConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now().plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuData(psuData);
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return aisConsent;
    }
}
