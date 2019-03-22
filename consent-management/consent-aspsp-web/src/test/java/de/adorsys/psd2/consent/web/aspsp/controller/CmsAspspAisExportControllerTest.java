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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspAisExportControllerTest {
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "psu id";
    private static final String WRONG_PSU_ID = "wrong psu id";
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final OffsetDateTime CREATION_DATE_TIME = OffsetDateTime.now();
    private static final OffsetDateTime STATUS_CHANGE_DATE_TIME = OffsetDateTime.now();

    @Mock
    private CmsAspspAisExportService cmsAspspAisExportService;
    @InjectMocks
    private CmsAspspAisExportController cmsAspspAisExportController;

    @Before
    public void setUp() {
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        when(cmsAspspAisExportService.exportConsentsByTpp(eq(TPP_AUTHORISATION_NUMBER), eq(CREATION_DATE_FROM),
                                                          eq(CREATION_DATE_TO), eq(psuIdData), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn(Collections.singletonList(buildAisAccountConsent()));
        when(cmsAspspAisExportService.exportConsentsByTpp(eq(WRONG_TPP_AUTHORISATION_NUMBER), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        when(cmsAspspAisExportService.exportConsentsByPsu(eq(psuIdData), eq(CREATION_DATE_FROM), eq(CREATION_DATE_TO), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn(Collections.singletonList(buildAisAccountConsent()));
        PsuIdData wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);
        when(cmsAspspAisExportService.exportConsentsByPsu(eq(wrongPsuIdData), any(), any(), any()))
            .thenReturn(Collections.emptyList());
    }

    @Test
    public void getConsentsByTpp_success() {
        // Given
        Collection<AisAccountConsent> expected = Collections.singletonList(buildAisAccountConsent());

        // When
        ResponseEntity<Collection<AisAccountConsent>> actual =
            cmsAspspAisExportController.getConsentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                         CREATION_DATE_TO, PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void getConsentsByTpp_failure_wrongTppAuthorisationNumber() {
        // When
        ResponseEntity<Collection<AisAccountConsent>> actual =
            cmsAspspAisExportController.getConsentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                         CREATION_DATE_TO, PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(Collections.emptyList(), actual.getBody());
    }

    @Test
    public void getConsentsByPsu_success() {
        // Given
        Collection<AisAccountConsent> expected = Collections.singletonList(buildAisAccountConsent());

        // When
        ResponseEntity<Collection<AisAccountConsent>> actual =
            cmsAspspAisExportController.getConsentsByPsu(CREATION_DATE_FROM, CREATION_DATE_TO, PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void getConsentsByPsu_failure_wrongPsuIdData() {
        // When
        ResponseEntity<Collection<AisAccountConsent>> actual =
            cmsAspspAisExportController.getConsentsByPsu(CREATION_DATE_FROM, CREATION_DATE_TO, WRONG_PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(Collections.emptyList(), actual.getBody());
    }

    private PsuIdData buildPsuIdData(String id) {
        return new PsuIdData(id, null, null, null);
    }

    private AisAccountConsent buildAisAccountConsent() {
        return new AisAccountConsent(EXTERNAL_CONSENT_ID,
                                     null, false,
                                     null, 0,
                                     null, null,
                                     false, false, null, null, null, false, Collections.emptyList(), 0, CREATION_DATE_TIME, STATUS_CHANGE_DATE_TIME);
    }
}
