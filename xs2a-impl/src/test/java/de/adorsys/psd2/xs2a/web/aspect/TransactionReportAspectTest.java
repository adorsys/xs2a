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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionReportAspectTest {
    private static final int MAX_NUMBER = 15;
    private static final String HUGE_REPORT = "very_big_report_the_has_too_many_symbols";
    private static final String SMALL_REPORT = "small_report";

    @Mock
    private JsonConverter jsonConverter;
    @Mock
    private Xs2aTransactionsReport transactionsReport;

    private TransactionReportAspect aspect;
    private ResponseObject responseObject;

    @Before
    public void setUp() {
        aspect = new TransactionReportAspect(MAX_NUMBER, jsonConverter);
    }

    @Test
    public void invokeGetTransactionsReportByPeriodAspect_isTransactionReportHuge() {
        when(jsonConverter.toJson(transactionsReport)).thenReturn(Optional.of(HUGE_REPORT));

        responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                             .body(transactionsReport)
                             .build();
        aspect.invokeGetTransactionsReportByPeriodAspect(responseObject);

        verify(transactionsReport, times(1)).setTransactionReportHuge(true);
    }

    @Test
    public void invokeGetTransactionsReportByPeriodAspect_isNotTransactionReportHuge() {
        when(jsonConverter.toJson(transactionsReport)).thenReturn(Optional.of(SMALL_REPORT));

        responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                             .body(transactionsReport)
                             .build();
        aspect.invokeGetTransactionsReportByPeriodAspect(responseObject);

        verify(transactionsReport, times(1)).setTransactionReportHuge(false);
    }

    @Test
    public void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        responseObject = ResponseObject.builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.invokeGetTransactionsReportByPeriodAspect(responseObject);
        assertTrue(actualResponse.hasError());
    }
}
