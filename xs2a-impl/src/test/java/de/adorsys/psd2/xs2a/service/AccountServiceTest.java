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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.*;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
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
    private static final SpiResponseStatus LOGICAL_FAILURE_RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final MessageErrorCode FORMAT_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final MessageErrorCode CONSENT_INVALID_MESSAGE_ERROR_CODE = MessageErrorCode.CONSENT_INVALID;
    private static final MessageError CONSENT_INVALID_MESSAGE_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID_MESSAGE_ERROR_CODE));
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData("Test AspspConsentData".getBytes(), CONSENT_ID);
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final List<SpiAccountDetails> EMPTY_ACCOUNT_DETAILS_LIST = Collections.emptyList();
    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE = buildSpiAccountReference();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiTransactionReport SPI_TRANSACTION_REPORT = buildSpiTransactionReport();
    private static final ResponseObject<AccountConsent> ERROR_ALLOWED_ACCOUNT_DATA_RESPONSE = buildErrorAllowedAccountDataResponse();
    private static final ResponseObject<AccountConsent> SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE = buildSuccessAllowedAccountDataResponse();
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(new SpiPsuData(null, null, null, null), new TppInfo(), UUID.randomUUID());
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOTH;
    private static final MessageError VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));

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
    private ValueValidatorService validatorService;
    @Mock
    private Xs2aAisConsentService aisConsentService;
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
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;
    @Mock
    private Transactions transactions;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetAccountListValidator getAccountListValidator;
    @Mock
    private GetAccountDetailsValidator getAccountDetailsValidator;
    @Mock
    private GetBalancesReportValidator getBalancesReportValidator;
    @Mock
    private GetTransactionsReportValidator getTransactionsReportValidator;
    @Mock
    private GetTransactionDetailsValidator getTransactionDetailsValidator;

    @Before
    public void setUp() {
        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);

        doNothing()
            .when(aisConsentService).consentActionLog(anyString(), anyString(), any(ActionStatus.class));

        when(spiContextDataProvider.provideWithPsuIdData(any(PsuIdData.class)))
            .thenReturn(SPI_CONTEXT_DATA);

        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getBalancesReportValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getTransactionsReportValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getTransactionDetailsValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
    }

    @Test
    public void getAccountDetailsList_Failure_AllowedAccountDataHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, WITH_BALANCE)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Xs2aAccountListHolder> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getAccountDetailsList_Failure_SpiResponseHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(EMPTY_ACCOUNT_DETAILS_LIST));

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(EMPTY_ACCOUNT_DETAILS_LIST), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(ErrorType.AIS_400).build());

        ResponseObject<Xs2aAccountListHolder> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getAccountDetailsList_Success() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        ResponseObject<Xs2aAccountListHolder> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aAccountListHolder body = actualResponse.getBody();

        assertThat(CollectionUtils.isNotEmpty(body.getAccountDetails())).isTrue();

        List<Xs2aAccountDetails> accountDetailsList = body.getAccountDetails();

        assertThat(CollectionUtils.isNotEmpty(accountDetailsList)).isTrue();
        assertThat(CollectionUtils.isEqualCollection(accountDetailsList, xs2aAccountDetailsList)).isTrue();
    }

    @Test
    public void getAccountDetailsList_shouldUpdateAccountReferences() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        // Given
        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Xs2aAccountDetails>> argumentCaptor = ArgumentCaptor.forClass((Class) List.class);

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        // Then
        Xs2aAccountAccess access = SUCCESS_ALLOWED_ACCOUNT_DATA_RESPONSE.getBody().getAccess();
        verify(accountReferenceUpdater).updateAccountReferences(eq(CONSENT_ID), eq(access), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(xs2aAccountDetailsList);

        Xs2aAccountListHolder responseBody = actualResponse.getBody();
        assertThat(responseBody.getAccountDetails()).isEqualTo(xs2aAccountDetailsList);
    }

    @Test
    public void getAccountList_Success_ShouldRecordEvent() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));
        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_ACCOUNT_LIST_REQUEST_RECEIVED);
    }

    @Test
    public void getAccountList_withInvalidConsent_shouldReturnValidationError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountService.getAccountList(CONSENT_ID, WITH_BALANCE);

        verify(getAccountListValidator).validate(new GetAccountListConsentObject(accountConsent, WITH_BALANCE));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getAccountDetails_Failure_AllowedAccountDataHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getAccountDetailsValidator.validate(new CommonAccountRequestObject(accountConsent, ACCOUNT_ID, WITH_BALANCE)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getAccountDetails_Failure_SpiResponseHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(spiAccountDetails));

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiAccountDetails), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(ErrorType.AIS_400).build());

        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getAccountDetails_failure_accountReferenceNotFoundInAccountAccess() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(getAccountDetailsValidator.validate(new CommonAccountRequestObject(accountConsent, ACCOUNT_ID, WITH_BALANCE)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getAccountDetails_Success() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails))
            .thenReturn(xs2aAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aAccountDetails body = actualResponse.getBody().getAccountDetails();

        assertThat(body).isNotNull();
        assertThat(body).isEqualTo(xs2aAccountDetails);
    }

    @Test
    public void getAccountDetails_Success_ShouldRecordEvent() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);
        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);
        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails))
            .thenReturn(xs2aAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_ACCOUNT_DETAILS_REQUEST_RECEIVED);
    }

    @Test
    public void getAccountDetails_withInvalidConsent_shouldReturnValidationError() {
        // Given
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE);

        // Then
        verify(getAccountDetailsValidator).validate(new CommonAccountRequestObject(accountConsent, ACCOUNT_ID, WITH_BALANCE));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getBalancesReport_Failure_AllowedAccountDataHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(getBalancesReportValidator.validate(new CommonConsentObject(accountConsent)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getBalancesReport_Failure_SpiResponseHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(Collections.emptyList()));

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(Collections.EMPTY_LIST), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(ErrorType.AIS_400).build());

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getBalancesReport_Failure_ConsentNotContainsAccountReference() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(Collections.emptyList()));

        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getBalancesReport_Success() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(Collections.emptyList()));

        when(balanceReportMapper.mapToXs2aBalancesReport(Collections.emptyList(), SPI_ACCOUNT_REFERENCE))
            .thenReturn(xs2aBalancesReport);

        when(xs2aBalancesReport.getXs2aAccountReference())
            .thenReturn(XS2A_ACCOUNT_REFERENCE);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

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
    public void getBalancesReport_Success_ShouldRecordEvent() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);
        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(Collections.emptyList()));
        when(balanceReportMapper.mapToXs2aBalancesReport(Collections.emptyList(), SPI_ACCOUNT_REFERENCE))
            .thenReturn(xs2aBalancesReport);
        when(xs2aBalancesReport.getXs2aAccountReference())
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_BALANCE_REQUEST_RECEIVED);
    }

    @Test
    public void getBalancesReport_withInvalidConsent_shouldReturnValidationError() {
        // Given
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getBalancesReportValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID);

        // Then
        verify(getBalancesReportValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_AllowedAccountDataHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionsReportValidator.validate(new CommonAccountRequestObject(accountConsent, ACCOUNT_ID, WITH_BALANCE)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getTransactionsReportByPeriod_Failure_SpiResponseHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(SPI_TRANSACTION_REPORT));

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(SPI_TRANSACTION_REPORT), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(ErrorType.AIS_400).build());

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getTransactionsReportByPeriod_failure_accountReferenceNotFoundInAccountAccess() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionsReportValidator.validate(new CommonAccountRequestObject(accountConsent, ACCOUNT_ID, WITH_BALANCE)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getTransactionsReportByPeriod_Success() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null);

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(referenceMapper.mapToXs2aAccountReference(SPI_ACCOUNT_REFERENCE))
            .thenReturn(Optional.of(XS2A_ACCOUNT_REFERENCE));

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();
    }

    @Test
    public void getTransactionsReportByPeriod_Success_ShouldRecordEvent() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        doNothing()
            .when(validatorService).validateAccountIdPeriod(ACCOUNT_ID, DATE_FROM, DATE_TO);

        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));
        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), null);
        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(SPI_ACCOUNT_REFERENCE))
            .thenReturn(Optional.of(XS2A_ACCOUNT_REFERENCE));
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);
    }

    @Test
    public void getTransactionsReportByPeriod_withInvalidConsent_shouldReturnValidationError() {
        // Given
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionsReportValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = accountService.getTransactionsReportByPeriod(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS);

        // Then
        verify(getTransactionsReportValidator).validate(new CommonAccountRequestObject(accountConsent, ACCOUNT_ID, WITH_BALANCE));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getTransactionDetails_Failure_AllowedAccountDataHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionDetailsValidator.validate(new CommonConsentObject(accountConsent)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Transactions> actualResponse = accountService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }

    @Test
    public void getTransactionDetails_Failure_SpiResponseHasError() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse(spiTransaction));

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiTransaction), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(FORMAT_ERROR_CODE).errorType(ErrorType.AIS_400).build());

        ResponseObject<Transactions> actualResponse = accountService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(FORMAT_ERROR_CODE);
    }

    @Test
    public void getTransactionDetails_failure_accountReferenceNotFoundInAccountAccess() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionDetailsValidator.validate(new CommonConsentObject(accountConsent)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_MESSAGE_ERROR));

        ResponseObject<Transactions> actualResponse = accountService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(CONSENT_INVALID_MESSAGE_ERROR);
    }


    @Test
    public void getTransactionDetails_Success() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(Collections.singletonList(spiTransaction), null))
            .thenReturn(Optional.of(xs2aAccountReport));

        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);

        ResponseObject<Transactions> actualResponse = accountService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();

        Transactions body = actualResponse.getBody();

        assertThat(body).isEqualTo(transactions);
    }

    @Test
    public void getTransactionDetails_Success_ShouldRecordEvent() {
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);
        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(XS2A_ACCOUNT_REFERENCE))
            .thenReturn(SPI_ACCOUNT_REFERENCE);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, SPI_ACCOUNT_REFERENCE, SPI_ACCOUNT_CONSENT, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));
        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(Collections.singletonList(spiTransaction), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);

        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);
    }

    @Test
    public void getTransactionDetails_withInvalidConsent_shouldReturnValidationError() {
        // Given
        AccountConsent accountConsent = createConsent(CONSENT_ID, createAccountAccess(XS2A_ACCOUNT_REFERENCE));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        when(getTransactionDetailsValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = accountService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID);

        // Then
        verify(getTransactionDetailsValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
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
        return new SpiAccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    // Needed because SpiTransactionReport is final, so it's impossible to mock it
    private static SpiTransactionReport buildSpiTransactionReport() {
        return new SpiTransactionReport(Collections.emptyList(), Collections.emptyList(), SpiTransactionReport.RESPONSE_TYPE_JSON, null);
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
        return new AccountConsent(id, access, false, LocalDate.now(), 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), 0);
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static Xs2aAccountAccess createAccountAccess(AccountReference accountReference) {
        return new Xs2aAccountAccess(Collections.singletonList(accountReference), Collections.singletonList(accountReference), Collections.singletonList(accountReference), AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES);
    }

    private static Xs2aAccountAccess createEmptyAccountAccess() {
        return new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES);
    }

}
