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

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService;
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
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspPisExportControllerTest {
    private static final String PAYMENT_ID = "payment id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String WRONG_TPP_AUTHORISATION_NUMBER = "wrong authorisation number";
    private static final LocalDate CREATION_DATE_FROM = LocalDate.of(2019, 1, 1);
    private static final LocalDate CREATION_DATE_TO = LocalDate.of(2020, 12, 1);
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PSU_ID = "psu id";
    private static final String WRONG_PSU_ID = "wrong psu id";
    private static final String ASPSP_ACCOUNT_ID = "account id";
    private static final String WRONG_ASPSP_ACCOUNT_ID = "wrong account id";

    @Mock
    private CmsAspspPisExportService cmsAspspPisExportService;
    @InjectMocks
    private CmsAspspPisExportController cmsAspspPisExportController;

    @Before
    public void setUp() {
        PsuIdData psuIdData = buildPsuIdData(PSU_ID);
        when(cmsAspspPisExportService.exportPaymentsByTpp(eq(TPP_AUTHORISATION_NUMBER), eq(CREATION_DATE_FROM),
                                                          eq(CREATION_DATE_TO), eq(psuIdData), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn(Collections.singletonList(buildCmsPayment()));
        when(cmsAspspPisExportService.exportPaymentsByTpp(eq(WRONG_TPP_AUTHORISATION_NUMBER), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        when(cmsAspspPisExportService.exportPaymentsByPsu(eq(psuIdData), eq(CREATION_DATE_FROM), eq(CREATION_DATE_TO), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn(Collections.singletonList(buildCmsPayment()));
        PsuIdData wrongPsuIdData = buildPsuIdData(WRONG_PSU_ID);
        when(cmsAspspPisExportService.exportPaymentsByPsu(eq(wrongPsuIdData), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        when(cmsAspspPisExportService.exportPaymentsByAccountId(eq(ASPSP_ACCOUNT_ID), eq(CREATION_DATE_FROM), eq(CREATION_DATE_TO), eq(DEFAULT_SERVICE_INSTANCE_ID)))
            .thenReturn(Collections.singletonList(buildCmsPayment()));
        when(cmsAspspPisExportService.exportPaymentsByAccountId(eq(WRONG_ASPSP_ACCOUNT_ID), any(), any(), any()))
            .thenReturn(Collections.emptyList());

    }

    @Test
    public void getPaymentsByTpp_success() {
        // Given
        Collection<CmsPayment> expected = Collections.singletonList(buildCmsPayment());

        // When
        ResponseEntity<Collection<CmsPayment>> actual =
            cmsAspspPisExportController.getPaymentsByTpp(TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                         CREATION_DATE_TO, PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void getPaymentsByTpp_failure_wrongTppAuthorisationNumber() {
        // When
        ResponseEntity<Collection<CmsPayment>> actual =
            cmsAspspPisExportController.getPaymentsByTpp(WRONG_TPP_AUTHORISATION_NUMBER, CREATION_DATE_FROM,
                                                         CREATION_DATE_TO, PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(Collections.emptyList(), actual.getBody());
    }

    @Test
    public void getPaymentsByPsu_success() {
        // Given
        Collection<CmsPayment> expected = Collections.singletonList(buildCmsPayment());

        // When
        ResponseEntity<Collection<CmsPayment>> actual =
            cmsAspspPisExportController.getPaymentsByPsu(CREATION_DATE_FROM, CREATION_DATE_TO, PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void getPaymentsByPsu_failure_wrongPsuIdData() {
        // When
        ResponseEntity<Collection<CmsPayment>> actual =
            cmsAspspPisExportController.getPaymentsByPsu(CREATION_DATE_FROM, CREATION_DATE_TO, WRONG_PSU_ID, null,
                                                         null, null, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(Collections.emptyList(), actual.getBody());
    }

    @Test
    public void getPaymentsByAccountId_success() {
        // Given
        Collection<CmsPayment> expected = Collections.singletonList(buildCmsPayment());

        // When
        ResponseEntity<Collection<CmsPayment>> actual =
            cmsAspspPisExportController.getPaymentsByAccountId(ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO,
                                                               DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }

    @Test
    public void getPaymentsByAccountId_failure_wrongAccountId() {
        // When
        ResponseEntity<Collection<CmsPayment>> actual =
            cmsAspspPisExportController.getPaymentsByAccountId(WRONG_ASPSP_ACCOUNT_ID, CREATION_DATE_FROM, CREATION_DATE_TO,
                                                               DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(Collections.emptyList(), actual.getBody());
    }

    private CmsPayment buildCmsPayment() {
        CmsSinglePayment cmsPayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        cmsPayment.setPaymentId(PAYMENT_ID);

        return cmsPayment;
    }

    private PsuIdData buildPsuIdData(String id) {
        return new PsuIdData(id, null, null, null);
    }
}
