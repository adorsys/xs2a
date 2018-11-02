/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aBookingStatus;
import de.adorsys.aspsp.xs2a.domain.account.*;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccessType;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {
    private static final boolean WITH_BALANCE = false;
    private static final String CONSENT_ID = "Test consentId";
    private static final String ACCOUNT_ID = UUID.randomUUID().toString();
    private static final String TRANSACTION_ID = "Test transactionId";
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE_FROM = LocalDate.of(2018, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.now();
    private static final Xs2aBookingStatus BOTH_XS2A_BOOKING_STATUS = Xs2aBookingStatus.BOTH;
    private static final SpiResponseStatus LOGICAL_FAILURE_RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final MessageErrorCode FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final MessageErrorCode CONSENT_INVALID_MESSAGE_ERROR_CODE = MessageErrorCode.CONSENT_INVALID;
    private static final MessageErrorCode RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE = MessageErrorCode.RESOURCE_UNKNOWN_404;
    private static final MessageError CONSENT_INVALID_MESSAGE_ERROR = new MessageError(new TppMessageInformation(MessageCategory.ERROR, CONSENT_INVALID_MESSAGE_ERROR_CODE));
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("Test AspspConsentData".getBytes(), CONSENT_ID);
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final List<SpiAccountDetails> EMPTY_ACCOUNT_DETAILS_LIST = Collections.emptyList();
    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE = buildSpiAccountReference();
    private static final Xs2aAccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiTransactionReport SPI_TRANSACTION_REPORT = buildSpiTransactionReport();
    private static final ResponseObject<AccountConsent> ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE = buildErrorAllowedAccountDataResponse();
    private static final ResponseObject<AccountConsent> SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE = buildSuccessAllowedAccountDataResponse();

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountSpi accountSpi;
    @Mock
    private SpiToXs2aAccountDetailsMapper accountDetailsMapper;
    @Mock
    private SpiToXs2aBalanceMapper balanceMapper;
    @Mock
    private SpiToXs2aBalanceReportMapper balanceReportMapper;
    @Mock
    private SpiToXs2aAccountReferenceMapper referenceMapper;
    @Mock
    private SpiTransactionListToXs2aAccountReportMapper transactionsToAccountReportMapper;
    @Mock
    private SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;
    @Mock
    private ValueValidatorService validatorService;
    @Mock
    private ConsentService consentService;
    @Mock
    private AisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private AisConsentDataService aisConsentDataService;
    @Mock
    private SpiAccountDetails spiAccountDetails;
    @Mock
    private Xs2aAccountDetails xs2aAccountDetails;
    @Mock
    private Xs2aBalancesReport xs2aBalancesReport;
    @Mock
    private SpiTransaction spiTransaction;
    @Mock
    private Xs2aAccountReport xs2aAccountReport;
    @Mock
    private Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;


    @Before
    public void setUp() {
        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        doNothing()
            .when(aisConsentService).consentActionLog(anyString(), anyString(), any(ActionStatus.class));
    }

    @Test
    public void getAccountDetailsList_Failure_AllowedAccountDataHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE);

        ResponseObject<Map<String, List<Xs2aAccountDetails>>> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getAccountDetailsList_Failure_SpiResponseHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(WITH_BALANCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(EMPTY_ACCOUNT_DETAILS_LIST));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        ResponseObject<Map<String, List<Xs2aAccountDetails>>> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getAccountDetailsList_Success() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(WITH_BALANCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        ResponseObject<Map<String, List<Xs2aAccountDetails>>> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Map<String, List<Xs2aAccountDetails>> body = actualResponse.getBody();

        assertThat(MapUtils.isNotEmpty(body)).isTrue();

        List<Xs2aAccountDetails> accountDetailsList = body.get("accountList");

        assertThat(CollectionUtils.isNotEmpty(accountDetailsList)).isTrue();
        assertThat(CollectionUtils.isEqualCollection(accountDetailsList, xs2aAccountDetailsList)).isTrue();
    }

    @Test
    public void getAccountDetails_Failure_AllowedAccountDataHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE);

        ResponseObject<Xs2aAccountDetails> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getAccountDetails_Failure_SpiResponseHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestAccountDetailForAccount(WITH_BALANCE, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(spiAccountDetails));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aAccountDetails> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getAccountDetails_Failure_SpiResponseBodyIsNull() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountDetailForAccount(WITH_BALANCE, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(null));

        ResponseObject<Xs2aAccountDetails> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);
    }

    @Test
    public void getAccountDetails_Success() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestAccountDetailForAccount(WITH_BALANCE, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails))
            .thenReturn(xs2aAccountDetails);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aAccountDetails> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aAccountDetails body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body).isEqualTo(xs2aAccountDetails);
    }

    @Test
    public void getBalancesReport_Failure_AllowedAccountDataHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE);

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getBalancesReport_Failure_SpiResponseHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(accountSpi.requestBalancesForAccount(SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(Collections.EMPTY_LIST));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getBalancesReport_Failure_SpiResponseBodyIsNull() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestBalancesForAccount(SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(null));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);
    }

    @Test
    public void getBalancesReport_Failure_ConsentNotContainsAccountReference() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(buildEmptyAllowedAccountDataResponse());

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestBalancesForAccount(SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(Collections.EMPTY_LIST));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);
    }

    @Test
    public void getBalancesReport_Success() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestBalancesForAccount(SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(Collections.EMPTY_LIST));

        when(balanceReportMapper.mapToXs2aBalancesReport(Collections.emptyList(), SPI_ACCOUNT_REFERENCE))
            .thenReturn(xs2aBalancesReport);

        when(xs2aBalancesReport.getXs2aAccountReference())
            .thenReturn(XS2A_ACCOUNT_REFERENCE);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aBalancesReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getXs2aAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_AllowedAccountDataHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE);

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, DATE_FROM, DATE_TO, BOTH_XS2A_BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_SpiResponseHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionsForAccount(WITH_BALANCE, DATE_FROM, DATE_TO, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(SPI_TRANSACTION_REPORT));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, DATE_FROM, DATE_TO, BOTH_XS2A_BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_SpiResponseBodyIsNull() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionsForAccount(WITH_BALANCE, DATE_FROM, DATE_TO, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(null));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, DATE_FROM, DATE_TO, BOTH_XS2A_BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);
    }

    @Test
    public void getTransactionsReportByPeriod_Success() {
        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        when(consentService.getValidatedConsent(CONSENT_ID, WITH_BALANCE))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

       when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestTransactionsForAccount(WITH_BALANCE, DATE_FROM, DATE_TO, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList());

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(Collections.emptyList()))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(referenceMapper.mapToXs2aAccountReference(SPI_ACCOUNT_REFERENCE))
            .thenReturn(Optional.of(XS2A_ACCOUNT_REFERENCE));

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, DATE_FROM, DATE_TO, BOTH_XS2A_BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getXs2aAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();
    }

    @Test
    public void getAccountReportByTransactionId_Failure_AllowedAccountDataHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE);

        ResponseObject<Xs2aAccountReport> actualResponse = accountService.getAccountReportByTransactionId(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getAccountReportByTransactionId_Failure_SpiResponseHasError() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentService.isValidAccountByAccess(anyString(), any()))
            .thenReturn(true);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(TRANSACTION_ID, ACCOUNT_ID, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(spiTransaction));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(FORMAT_ERROR_CODE);

        ResponseObject<Xs2aAccountReport> actualResponse = accountService.getAccountReportByTransactionId(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getAccountReportByTransactionId_Failure_SpiResponseBodyIsNull() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(TRANSACTION_ID, ACCOUNT_ID, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(null));

        when(messageErrorCodeMapper.mapToMessageErrorCode(LOGICAL_FAILURE_RESPONSE_STATUS))
            .thenReturn(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);

        ResponseObject<Xs2aAccountReport> actualResponse = accountService.getAccountReportByTransactionId(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(RESOURCE_UNKNOWN_404_MESSAGE_ERROR_CODE);
    }

    @Test
    public void getAccountReportByTransactionId_Success() {
        when(consentService.getValidatedConsent(CONSENT_ID))
            .thenReturn(SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE);

        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(TRANSACTION_ID, ACCOUNT_ID, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(Collections.singletonList(spiTransaction)))
            .thenReturn(Optional.of(xs2aAccountReport));

        ResponseObject<Xs2aAccountReport> actualResponse = accountService.getAccountReportByTransactionId(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aAccountReport body = actualResponse.getBody();

        assertThat(body).isEqualTo(xs2aAccountReport);
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(LOGICAL_FAILURE_RESPONSE_STATUS);
    }

    // Needed because SpiAccountReference is final, so it's impossible to mock it
    private static SpiAccountReference buildSpiAccountReference() {
        return new SpiAccountReference(ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    private static Xs2aAccountReference buildXs2aAccountReference() {
        return new Xs2aAccountReference(ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    // Needed because SpiTransactionReport is final, so it's impossible to mock it
    private static SpiTransactionReport buildSpiTransactionReport() {
        return new SpiTransactionReport(Collections.emptyList(), Collections.emptyList());
    }

    // Needed because ResponseObject is final, so it's impossible to mock it
    private static ResponseObject<AccountConsent> buildSuccessAllowedAccountDataResponse() {
        return ResponseObject.<AccountConsent>builder()
            .body(createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE)))
            .build();
    }

    // Needed because ResponseObject is final, so it's impossible to mock it
    private static ResponseObject<AccountConsent> buildErrorAllowedAccountDataResponse() {
        return ResponseObject.<AccountConsent>builder()
            .fail(CONSENT_INVALID_MESSAGE_ERROR)
            .build();
    }

    // Needed because ResponseObject is final, so it's impossible to mock it
    private static ResponseObject<AccountConsent> buildEmptyAllowedAccountDataResponse() {
        return ResponseObject.<AccountConsent>builder()
            .body(createConsent(CONSENT_ID, createEmptyAccountAccess()))
            .build();
    }

    private static AccountConsent createConsent(String id, Xs2aAccountAccess access) {
        return new AccountConsent(id, access, false, LocalDate.now(), 4, null, ConsentStatus.VALID, false, false, null, UUID.randomUUID().toString());
    }

    private static Xs2aAccountAccess createAccountAccess(Xs2aAccountReference accountReference) {
        return new Xs2aAccountAccess(Collections.singletonList(accountReference), Collections.singletonList(accountReference), Collections.singletonList(accountReference), Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES);
    }

    private static Xs2aAccountAccess createEmptyAccountAccess() {
        return new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES);
    }

}
