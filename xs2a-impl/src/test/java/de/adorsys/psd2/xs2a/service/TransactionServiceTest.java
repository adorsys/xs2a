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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsDownloadResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.ais.AccountHelperService;
import de.adorsys.psd2.xs2a.service.ais.TransactionService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAccountService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.service.validator.ais.account.DownloadTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountTransactionsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.DownloadTransactionListRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final boolean WITH_BALANCE = false;
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String TRANSACTION_ID = "22222222";
    private static final String IBAN = "DE62500105179972514662";
    private static final String BBAN = "89370400440532010000";
    private static final String PAN = "2356574632171234";
    private static final String MASKED_PAN = "235657******1234";
    private static final String MSISDN = "+49(0)911 360698-0";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE_FROM = LocalDate.of(2018, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.now();
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE_GLOBAL = buildSpiAccountReferenceGlobal();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiTransactionReport SPI_TRANSACTION_REPORT = buildSpiTransactionReport();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOTH;
    private static final BookingStatus BOOKING_STATUS_INFORMATION = BookingStatus.INFORMATION;
    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    private static final String ENTRY_REFERENCE_FROM = "777";
    private static final Boolean DELTA_LIST = Boolean.TRUE;
    private static final Xs2aTransactionsReportByPeriodRequest XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST = buildXs2aTransactionsReportByPeriodRequest();
    private static final Xs2aTransactionsReportByPeriodRequest XS2A_TRANSACTIONS_REPORT_BY_PERIOD_BOOKING_STATUS_INFO_REQUEST = buildXs2aTransactionsReportByPeriodBookingStatusInfoRequest();
    private static final String DOWNLOAD_ID = "dGVzdA==";
    private static final int DATA_SIZE_BYTES = 1000;
    private static final String FILENAME = "transactions.json";

    private SpiAccountReference spiAccountReference;
    private Xs2aTransactionsDownloadResponse xs2aTransactionsDownloadResponse;
    private AisConsent aisConsent;
    private InputStream inputStream;
    private TransactionsReportByPeriodObject transactionsReportByPeriodObject;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private AccountSpi accountSpi;
    @Mock
    private SpiToXs2aBalanceMapper balanceMapper;
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
    private SpiTransaction spiTransaction;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;
    @Mock
    private Transactions transactions;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetTransactionsReportValidator getTransactionsReportValidator;
    @Mock
    private GetTransactionDetailsValidator getTransactionDetailsValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private DownloadTransactionsReportValidator downloadTransactionsReportValidator;
    @Mock
    private SpiToXs2aDownloadTransactionsMapper spiToXs2aDownloadTransactionsMapper;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private Xs2aAccountService xs2aAccountService;
    @Mock
    SpiTransaction testSpiTransaction;


    @BeforeEach
    void setUp() {
        aisConsent = createConsent();
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        xs2aTransactionsDownloadResponse = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-transactions-download-response.json", Xs2aTransactionsDownloadResponse.class);
        inputStream = new ByteArrayInputStream("test string".getBytes());
        transactionsReportByPeriodObject = buildTransactionsReportByPeriodObject();
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
    }

    @Test
    void getTransactionsReportByPeriod_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getTransactionsReportByPeriod_Failure_AllowedAccountDataHasError() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        when(getTransactionsReportValidator.validate(transactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getTransactionsReportByPeriod_Failure_SpiResponseHasError() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(SPI_TRANSACTION_REPORT));
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(SPI_TRANSACTION_REPORT), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getTransactionsReportByPeriod_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getTransactionsReportValidator.validate(transactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getTransactionsReportByPeriod_With406ErrorInSpiTransactionReport() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorServiceNotSupportedSpiResponse());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, REQUESTED_FORMATS_INVALID);
    }

    @Test
    void getTransactionsReportByPeriod_Success() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<SpiTransactionReportParameters> argumentCaptor = ArgumentCaptor.forClass(SpiTransactionReportParameters.class);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();

        verify(accountSpi).requestTransactionsForAccount(any(SpiContextData.class), argumentCaptor.capture(), any(SpiAccountReference.class), any(SpiAccountConsent.class), eq(null));
        checkPassingParametersWithoutAnyChanges(argumentCaptor.getValue());
    }

    @Test
    void getTransactionsReportByPeriodBookingStatusInformation_Success() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParametersBookingStatusInfo(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(buildSpiTransactionReportNonEmptyTransactionList()));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.INFORMATION, Collections.singletonList(testSpiTransaction), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<SpiTransactionReportParameters> argumentCaptor = ArgumentCaptor.forClass(SpiTransactionReportParameters.class);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_BOOKING_STATUS_INFO_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();

        verify(accountSpi).requestTransactionsForAccount(any(SpiContextData.class), argumentCaptor.capture(), any(SpiAccountReference.class), any(SpiAccountConsent.class), eq(null));
        checkPassingParametersWithoutAnyChanges(argumentCaptor.getValue());
    }

    @Test
    void getTransactionsReportByPeriod_nullAccount() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(null);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNull();
        assertThatErrorIs(actualResponse, FORMAT_ERROR_UNKNOWN_ACCOUNT);

        verify(accountSpi, never()).requestTransactionsForAccount(any(SpiContextData.class), any(), any(SpiAccountReference.class), any(SpiAccountConsent.class), any());
    }

    @Test
    void getTransactionsReportByPeriod_WhenConsentIsGlobal_Success() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(SPI_ACCOUNT_REFERENCE_GLOBAL);

        AisConsent aisConsent = createConsent();

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), SPI_ACCOUNT_REFERENCE_GLOBAL, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(SPI_ACCOUNT_REFERENCE_GLOBAL))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertTrue(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList()));
    }

    @Test
    void getTransactionsReportByPeriod_filledTransactionsInReport() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);

        SpiTransactionReport transactionReportWithTransactions = new SpiTransactionReport(DOWNLOAD_ID, Collections.singletonList(spiTransaction), null, SpiTransactionReport.RESPONSE_TYPE_JSON, null, null, 3);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(transactionReportWithTransactions));

        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.singletonList(transactions), Collections.emptyList(), Collections.emptyList(), null);
        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.singletonList(spiTransaction), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();
        assertNotNull(body);
        assertEquals(xs2aAccountReport, body.getAccountReport());
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertEquals(Collections.emptyList(), body.getBalances());
    }

    @Test
    void getTransactionsReportByPeriod_nullTransactionListInReport() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        SpiTransactionReport transactionReportWithoutTransactions = new SpiTransactionReport(DOWNLOAD_ID, null, null, SpiTransactionReport.RESPONSE_TYPE_JSON, null, null, 5);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(transactionReportWithoutTransactions));

        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, null, null))
            .thenReturn(Optional.empty());
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aTransactionsReport body = actualResponse.getBody();
        assertNotNull(body);
        assertNull(body.getAccountReport());
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertEquals(Collections.emptyList(), body.getBalances());
    }

    @Test
    void getTransactionsReportByPeriod_Success_ShouldRecordEvent() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));
        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);
    }

    @Test
    void getTransactionsReportByPeriod_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aTransactionsReport> actualResponse = transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(getTransactionsReportValidator).validate(transactionsReportByPeriodObject);
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getTransactionsReportByPeriod_shouldRecordStatusInLoggingContext() {
        // Given
        when(getTransactionsReportValidator.validate(any(TransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspProfileService.isTransactionsWithoutBalancesSupported())
            .thenReturn(true);
        when(accountSpi.requestTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_TRANSACTION_REPORT));
        Xs2aAccountReport xs2aAccountReport = new Xs2aAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        when(transactionsToAccountReportMapper.mapToXs2aAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(referenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        transactionService.getTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void downloadTransactions_success() throws IOException {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        SpiTransactionsDownloadResponse spiTransactionsDownloadResponse = new SpiTransactionsDownloadResponse(inputStream, FILENAME, DATA_SIZE_BYTES);

        when(accountSpi.requestTransactionsByDownloadLink(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, new String(Base64.getDecoder().decode(DOWNLOAD_ID)), spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransactionsDownloadResponse));
        xs2aTransactionsDownloadResponse.setTransactionStream(inputStream);

        when(spiToXs2aDownloadTransactionsMapper.mapToXs2aTransactionsDownloadResponse(spiTransactionsDownloadResponse))
            .thenReturn(xs2aTransactionsDownloadResponse);

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertResponseHasNoErrors(actualResponse);
        assertEquals(DATA_SIZE_BYTES, (long) actualResponse.getBody().getDataSizeBytes());
        assertEquals(FILENAME, actualResponse.getBody().getDataFileName());
        inputStream.close();
    }

    @Test
    void downloadTransactions_Failure_no_consent_shouldReturn_400() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void downloadTransactions_Failure_validation_fails_shouldReturn_400() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.invalid(AIS_401, CONSENT_EXPIRED));

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_EXPIRED);
    }

    @Test
    void downloadTransactions_Failure_SPI_fails_shouldReturn_error() throws IOException {
        // Given
        SpiTransactionsDownloadResponse spiTransactionsDownloadResponse = new SpiTransactionsDownloadResponse(inputStream, FILENAME, DATA_SIZE_BYTES);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionsByDownloadLink(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, new String(Base64.getDecoder().decode(DOWNLOAD_ID)), spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(spiTransactionsDownloadResponse));

        ErrorHolder errorHolder = ErrorHolder.builder(AIS_401)
                                      .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                                      .build();
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiTransactionsDownloadResponse), ServiceType.AIS))
            .thenReturn(errorHolder);

        // When
        ResponseObject<Xs2aTransactionsDownloadResponse> actualResponse = transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        assertNotNull(actualResponse.getError().getTppMessage());
        inputStream.close();
    }

    @Test
    void downloadTransactions_shouldRecordStatusInLoggingContext() {
        // Given
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(downloadTransactionsReportValidator.validate(any(DownloadTransactionListRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        SpiTransactionsDownloadResponse spiTransactionsDownloadResponse = new SpiTransactionsDownloadResponse(inputStream, FILENAME, DATA_SIZE_BYTES);
        when(accountSpi.requestTransactionsByDownloadLink(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, new String(Base64.getDecoder().decode(DOWNLOAD_ID)), spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransactionsDownloadResponse));

        // When
        transactionService.downloadTransactions(CONSENT_ID, ACCOUNT_ID, DOWNLOAD_ID);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void getTransactionDetails_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());
        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getTransactionDetails_Failure_AllowedAccountDataHasError() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getTransactionDetailsValidator.validate(new CommonAccountTransactionsRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getTransactionDetails_Failure_SpiResponseHasError() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);

        doNothing().when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(spiTransaction));
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiTransaction), ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(AIS_400)
                            .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getTransactionDetails_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getTransactionDetailsValidator.validate(new CommonAccountTransactionsRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getTransactionDetails_Success() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        doNothing()
            .when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));
        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);
        Transactions body = actualResponse.getBody();
        assertThat(body).isEqualTo(transactions);
    }

    @Test
    void getTransactionDetails_Success_ShouldRecordEvent() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        doNothing().when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));
        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);
    }

    @Test
    void getTransactionDetails_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Transactions> actualResponse = transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        verify(getTransactionDetailsValidator).validate(new CommonAccountTransactionsRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getTransactionDetails_shouldRecordStatusInLoggingContext() {
        // Given
        when(getTransactionDetailsValidator.validate(any(CommonAccountTransactionsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        doNothing().when(validatorService).validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestTransactionForAccountByTransactionId(SPI_CONTEXT_DATA, TRANSACTION_ID, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiTransaction));
        when(spiToXs2aTransactionMapper.mapToXs2aTransaction(spiTransaction))
            .thenReturn(transactions);

        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        transactionService.getTransactionDetails(CONSENT_ID, ACCOUNT_ID, TRANSACTION_ID, REQUEST_URI);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    private void checkPassingParametersWithoutAnyChanges(SpiTransactionReportParameters parameters) {
        assertEquals(DATE_FROM, parameters.getDateFrom());
        assertEquals(DATE_TO, parameters.getDateTo());
        assertEquals(ENTRY_REFERENCE_FROM, parameters.getEntryReferenceFrom());
        assertEquals(DELTA_LIST, parameters.getDeltaList());
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorServiceNotSupportedSpiResponse() {
        return SpiResponse.<T>builder()
                   .error(new TppMessage(SERVICE_NOT_SUPPORTED))
                   .build();
    }

    private void assertThatErrorIs(ResponseObject<?> actualResponse, MessageErrorCode messageErrorCode) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(messageErrorCode);
    }

    private void assertResponseHasNoErrors(ResponseObject<?> actualResponse) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();
    }

    private static AisConsent createConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentData(buildAisConsentData());
        aisConsent.setId(CONSENT_ID);
        aisConsent.setValidUntil(LocalDate.now());
        aisConsent.setFrequencyPerDay(4);
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        aisConsent.setAuthorisations(Collections.emptyList());
        aisConsent.setConsentTppInformation(buildConsentTppInformation());
        aisConsent.setStatusChangeTimestamp(OffsetDateTime.now());
        aisConsent.setUsages(Collections.emptyMap());
        aisConsent.setStatusChangeTimestamp(OffsetDateTime.now());
        return aisConsent;
    }

    private static AisConsentData buildAisConsentData() {
        return AisConsentData.buildDefaultAisConsentData();
    }

    private static ConsentTppInformation buildConsentTppInformation() {
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(createTppInfo());
        return consentTppInformation;
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static SpiAccountReference buildSpiAccountReferenceGlobal() {
        return SpiAccountReference.builder().resourceId(ACCOUNT_ID).build();
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY, null);
    }

    // Needed because SpiTransactionReport is final, so it's impossible to mock it
    private static SpiTransactionReport buildSpiTransactionReport() {
        return new SpiTransactionReport(DOWNLOAD_ID, Collections.emptyList(), Collections.emptyList(), SpiTransactionReport.RESPONSE_TYPE_JSON, null, null, 5);
    }

    private SpiTransactionReport buildSpiTransactionReportNonEmptyTransactionList() {
        return new SpiTransactionReport(DOWNLOAD_ID, Collections.singletonList(testSpiTransaction), Collections.emptyList(), SpiTransactionReport.RESPONSE_TYPE_JSON, null, null, 5);
    }

    @NotNull
    private static Xs2aTransactionsReportByPeriodRequest buildXs2aTransactionsReportByPeriodRequest() {
        return new Xs2aTransactionsReportByPeriodRequest(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, null, null);
    }

    @NotNull
    private static Xs2aTransactionsReportByPeriodRequest buildXs2aTransactionsReportByPeriodBookingStatusInfoRequest() {
        return new Xs2aTransactionsReportByPeriodRequest(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS_INFORMATION, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, null, null);
    }

    @NotNull
    private TransactionsReportByPeriodObject buildTransactionsReportByPeriodObject() {
        return new TransactionsReportByPeriodObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, DATE_FROM);
    }

    private SpiTransactionReportParameters buildSpiTransactionReportParameters() {
        return new SpiTransactionReportParameters(MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS, ENTRY_REFERENCE_FROM, DELTA_LIST, null, null);
    }

    private SpiTransactionReportParameters buildSpiTransactionReportParametersBookingStatusInfo() {
        return new SpiTransactionReportParameters(MediaType.APPLICATION_JSON_VALUE, WITH_BALANCE, DATE_FROM, DATE_TO, BOOKING_STATUS_INFORMATION, ENTRY_REFERENCE_FROM, DELTA_LIST, null, null);
    }
}
