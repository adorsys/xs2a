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

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.consent.repository.specification.PiisConsentEntitySpecification;
import de.adorsys.psd2.consent.service.mapper.PiisConsentMapper;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
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
public class CmsAspspPiisFundsExportServiceInternalTest {
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String SERVICE_INSTANCE_ID = "instance id";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "psu id";
    private static final String WRONG_PSU_ID = "wrong psu id";

    private static final String ASPSP_ACCOUNT_ID = "aspsp account id";
    private static final String WRONG_ASPSP_ACCOUNT_ID = "wrong aspsp account id";

    private static final OffsetDateTime CREATION_TIMESTAMP =
        OffsetDateTime.of(2019, 2, 4, 12, 0, 0, 0, ZoneOffset.UTC);

    @InjectMocks
    private CmsAspspPiisFundsExportServiceInternal cmsAspspPiisFundsExportServiceInternal;
    @Mock
    private PiisConsentRepository piisConsentRepository;
    @Mock
    private PiisConsentEntitySpecification piisConsentEntitySpecification;
    @Mock
    private PiisConsentMapper piisConsentMapper;

    private PsuIdData psuIdData;
    private PsuIdData wrongPsuIdData;

    @Before
    public void setUp() {
        psuIdData = buildPsuIdData(PSU_ID);
        wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);

        when(piisConsentMapper.mapToPiisConsentList(Collections.singletonList(buildPiisConsentEntity())))
            .thenReturn(Collections.singletonList(buildPiisConsent()));
    }

    @Test
    public void exportConsentsByTpp_success() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expectedConsent = buildPiisConsent();

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID);

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(expectedConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByTpp_success_nullInstanceId() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expectedConsent = buildPiisConsent();

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, null);

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(expectedConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                               CREATION_DATE_TO, psuIdData, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByTpp_failure_wrongTppAuthorisationNumber() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());

    }

    @Test
    public void exportConsentsByTpp_failure_nullTppAuthorisationNumber() {
        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp(null, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByTpp_failure_blankTppAuthorisationNumber() {
        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByTpp("", CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, psuIdData, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(any(), any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByPsu_success() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expectedConsent = buildPiisConsent();

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(expectedConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByPsu_success_nullInstanceId() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expectedConsent = buildPiisConsent();

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(psuIdData, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, null);

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(expectedConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(psuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByPsu_failure_wrongPsuIdData() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(wrongPsuIdData, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byPsuIdDataAndCreationPeriodAndInstanceId(wrongPsuIdData, CREATION_DATE_FROM,
                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByPsu_failure_nullPsuIdData() {
        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(null, CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByPsu_failure_emptyPsuIdData() {
        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByPsu(buildEmptyPsuIdData(), CREATION_DATE_FROM,
                                                                       CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byPsuIdDataAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    @Test
    public void exportConsentsByAccountId_success() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expectedConsent = buildPiisConsent();

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(expectedConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByAccountId__success_nullInstanceId() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class)))
            .thenReturn(Collections.singletonList(buildPiisConsentEntity()));
        PiisConsent expectedConsent = buildPiisConsent();

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, null);

        // Then
        assertFalse(piisConsents.isEmpty());
        assertTrue(piisConsents.contains(expectedConsent));
        verify(piisConsentEntitySpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, DEFAULT_SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByAccountId_wrongAspspAccountId() {
        // Given
        //noinspection unchecked
        when(piisConsentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, times(1))
            .byAspspAccountIdAndCreationPeriodAndInstanceId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM,
                                                            CREATION_DATE_TO, SERVICE_INSTANCE_ID);
    }

    @Test
    public void exportConsentsByAccountId__failure_blankAspspAccountId() {
        // When
        Collection<PiisConsent> piisConsents =
            cmsAspspPiisFundsExportServiceInternal.exportConsentsByAccountId("", CREATION_DATE_FROM,
                                                                             CREATION_DATE_TO, SERVICE_INSTANCE_ID);

        // Then
        assertTrue(piisConsents.isEmpty());
        verify(piisConsentEntitySpecification, never())
            .byAspspAccountIdAndCreationPeriodAndInstanceId(any(), any(), any(), any());
    }

    private PiisConsentEntity buildPiisConsentEntity() {
        PiisConsentEntity piisConsentEntity = new PiisConsentEntity();
        piisConsentEntity.setPsuData(buildPsuData());
        piisConsentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        return piisConsentEntity;
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setPsuData(buildPsuIdData(PSU_ID));
        piisConsent.setCreationTimestamp(CREATION_TIMESTAMP);
        return piisConsent;
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


}
