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

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionAspectTest {

    private static final String CONSENT_ID = "some consent id";
    private static final String ACCOUNT_ID = "some account id";
    private static final String RESOURCE_ID = "some resource id";
    private static final String DOWNLOAD_ID = "1234asdfqw==";
    private static final String REQUEST_URI = "/v1/accounts";
    private Xs2aTransactionsReportByPeriodRequest xs2aTransactionsReportByPeriodRequest;

    @Mock
    private AspspProfileService aspspProfileService;

    @Mock
    private Xs2aTransactionsReport transactionsReport;

    private AspspSettings aspspSettings;
    private ResponseObject responseObject;
    private JsonReader jsonReader = new JsonReader();
    private TransactionAspect aspect;

    @Before
    public void setUp() {
        aspect = new TransactionAspect(aspspProfileService);
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
    }

    @Test
    public void getTransactionsReportByPeriod_successReport() {
        xs2aTransactionsReportByPeriodRequest = jsonReader.getObjectFromFile("json/Xs2aTransactionsReportByPeriodRequest.json", Xs2aTransactionsReportByPeriodRequest.class);

        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(transactionsReport.getAccountReport()).thenReturn(new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null));

        responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                             .body(transactionsReport)
                             .build();
        ResponseObject actualResponse = aspect.getTransactionsReportByPeriod(responseObject, xs2aTransactionsReportByPeriodRequest);

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void getTransactionsReportByPeriod_withError_shouldAddTextErrorMessage() {
        xs2aTransactionsReportByPeriodRequest = jsonReader.getObjectFromFile("json/Xs2aTransactionsReportByPeriodRequest.json", Xs2aTransactionsReportByPeriodRequest.class);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();


        ResponseObject actualResponse = aspect.getTransactionsReportByPeriod(responseObject, xs2aTransactionsReportByPeriodRequest);

        assertTrue(actualResponse.hasError());
    }

    @Test
    public void getTransactionDetailsAspect_successNotHugeReport() {
        responseObject = ResponseObject.<Transactions>builder()
                             .body(new Transactions())
                             .build();
        ResponseObject actualResponse = aspect.getTransactionDetailsAspect(responseObject, CONSENT_ID, ACCOUNT_ID,
                                                                           RESOURCE_ID, REQUEST_URI);

        assertFalse(actualResponse.hasError());
        assertEquals(responseObject, actualResponse);
    }

    @Test
    public void getTransactionDetailsAspect_withError_shouldAddTextErrorMessage() {
        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.getTransactionDetailsAspect(responseObject, CONSENT_ID, ACCOUNT_ID, RESOURCE_ID, REQUEST_URI);

        assertTrue(actualResponse.hasError());
    }

    @Test
    public void downloadTransactionsAspect_successNotHugeReport() {
        responseObject = ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                             .body(new Xs2aTransactionsDownloadResponse())
                             .build();
        ResponseObject actualResponse = aspect.downloadTransactions(responseObject, CONSENT_ID, ACCOUNT_ID,
                                                                    DOWNLOAD_ID);

        assertFalse(actualResponse.hasError());
        assertEquals(responseObject, actualResponse);
    }

    @Test
    public void downloadTransactionsAspect_withError_shouldAddTextErrorMessage() {
        responseObject = ResponseObject.<Xs2aTransactionsDownloadResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.downloadTransactions(responseObject, CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        assertTrue(actualResponse.hasError());
    }
}
