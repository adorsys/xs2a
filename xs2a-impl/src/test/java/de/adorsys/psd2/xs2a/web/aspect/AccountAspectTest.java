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
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.link.AccountDetailsLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportByPeriodHugeLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportByPeriodLinks;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountAspectTest {

    private static final String CONSENT_ID = "some consent id";
    private static final String ACCOUNT_ID = "some account id";
    private static final String RESOURCE_ID = "some resource id";
    private static final String REQUEST_URI = "/v1/accounts";
    private static final String ERROR_TEXT = "Error occurred while processing";

    @InjectMocks
    private AccountAspect aspect;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private MessageService messageService;
    @Mock
    private Xs2aTransactionsReport transactionsReport;
    @Mock
    private Xs2aAccountReport accountReport;

    private Xs2aAccountDetails accountDetails;
    private AccountConsent accountConsent;
    private AspspSettings aspspSettings;
    private ResponseObject responseObject;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        accountConsent = jsonReader.getObjectFromFile("json/aspect/account_consent.json", AccountConsent.class);
        accountDetails = jsonReader.getObjectFromFile("json/aspect/account_details.json", Xs2aAccountDetails.class);
    }

    @Test
    public void getAccountDetailsAspect_success() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);

        responseObject = ResponseObject.<Xs2aAccountDetailsHolder>builder()
                             .body(new Xs2aAccountDetailsHolder(accountDetails, accountConsent))
                             .build();
        ResponseObject actualResponse = aspect.getAccountDetailsAspect(responseObject, CONSENT_ID, ACCOUNT_ID, true, REQUEST_URI);

        verify(aspspProfileService, times(2)).getAspspSettings();
        assertNotNull(accountDetails.getLinks());
        assertTrue(accountDetails.getLinks() instanceof AccountDetailsLinks);

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void getAccountDetailsAspect_withError_shouldAddTextErrorMessage() {
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.getAccountDetailsAspect(responseObject, CONSENT_ID, ACCOUNT_ID, true, REQUEST_URI);

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

    @Test
    public void getAccountDetailsListAspect_success() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);

        responseObject = ResponseObject.<Xs2aAccountListHolder>builder()
                             .body(new Xs2aAccountListHolder(Collections.singletonList(accountDetails), accountConsent))
                             .build();
        ResponseObject actualResponse = aspect.getAccountDetailsListAspect(responseObject, CONSENT_ID, true, REQUEST_URI);

        verify(aspspProfileService, times(2)).getAspspSettings();
        assertNotNull(accountDetails.getLinks());
        assertTrue(accountDetails.getLinks() instanceof AccountDetailsLinks);

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void getAccountDetailsListAspect_withError_shouldAddTextErrorMessage() {
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.getAccountDetailsListAspect(responseObject, CONSENT_ID, true, REQUEST_URI);

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

    @Test
    public void getTransactionsReportByPeriod_successHugeReport() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(transactionsReport.isTransactionReportHuge()).thenReturn(true);

        responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                             .body(transactionsReport)
                             .build();
        ResponseObject actualResponse = aspect.getTransactionsReportByPeriod(responseObject, CONSENT_ID, ACCOUNT_ID,
                                                                             null, true, LocalDate.now(),
                                                                             LocalDate.now(), BookingStatus.BOOKED,
                                                                             REQUEST_URI);

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(transactionsReport, times(1)).setLinks(any(TransactionsReportByPeriodHugeLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void getTransactionsReportByPeriod_successNotHugeReport() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(transactionsReport.isTransactionReportHuge()).thenReturn(false);
        when(transactionsReport.getAccountReport()).thenReturn(accountReport);

        responseObject = ResponseObject.<Xs2aTransactionsReport>builder()
                             .body(transactionsReport)
                             .build();
        ResponseObject actualResponse = aspect.getTransactionsReportByPeriod(responseObject, CONSENT_ID, ACCOUNT_ID,
                                                                             null, true, LocalDate.now(),
                                                                             LocalDate.now(), BookingStatus.BOOKED,
                                                                             REQUEST_URI);

        verify(aspspProfileService, times(2)).getAspspSettings();
        verify(accountReport, times(1)).setLinks(any(TransactionsReportByPeriodLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    public void getTransactionsReportByPeriod_withError_shouldAddTextErrorMessage() {
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.getTransactionsReportByPeriod(responseObject, CONSENT_ID, ACCOUNT_ID,
                                                                             null, true, LocalDate.now(),
                                                                             LocalDate.now(), BookingStatus.BOOKED,
                                                                             REQUEST_URI);

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
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
        when(messageService.getMessage(any())).thenReturn(ERROR_TEXT);

        responseObject = ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = aspect.getTransactionDetailsAspect(responseObject, CONSENT_ID, ACCOUNT_ID, RESOURCE_ID, REQUEST_URI);

        assertTrue(actualResponse.hasError());
        assertEquals(ERROR_TEXT, actualResponse.getError().getTppMessage().getText());
    }

}
