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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.core.data.AccountAccess;
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
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardTransactionsReportByPeriodRequest;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.CardAccountHandler;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAccountService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiCardTransactionListToXs2aAccountReportMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetCardTransactionsReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CardTransactionsReportByPeriodObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CardAccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardTransactionServiceTest {

    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String CONSENT_ID = "Test consentId";
    private static final String ACCOUNT_ID = "Test accountId";
    private static final String IBAN = "DE69760700240340283600";
    private static final String BBAN = "DE80760700240271232400";
    private static final String PAN = "4937023494670836";
    private static final String MASKED_PAN = "493702******0836";
    private static final String MSISDN = "821012345678";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final LocalDate DATE_FROM = LocalDate.of(2018, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.now();
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final SpiAccountReference SPI_ACCOUNT_REFERENCE_GLOBAL = buildSpiAccountReferenceGlobal();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiCardTransactionReport SPI_CARD_TRANSACTION_REPORT = buildSpiTransactionReport();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOTH;
    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    private static final String ENTRY_REFERENCE_FROM = "777";
    private static final Boolean DELTA_LIST = Boolean.TRUE;
    private static final Xs2aCardTransactionsReportByPeriodRequest XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST = buildXs2aTransactionsReportByPeriodRequest();
    private static final String BASE64_STRING_EXAMPLE = "dGVzdA==";

    private SpiAccountReference spiAccountReference;
    private AisConsent accountConsent;
    private CardTransactionsReportByPeriodObject cardTransactionsReportByPeriodObject;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    private final JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private CardTransactionService cardTransactionService;

    @Mock
    private CardAccountSpi cardAccountSpi;
    @Mock
    private SpiToXs2aBalanceMapper balanceMapper;
    @Mock
    private SpiCardTransactionListToXs2aAccountReportMapper spiCardTransactionListToXs2aAccountReportMapper;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetCardTransactionsReportValidator getCardTransactionsReportValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private Xs2aAccountService xs2aAccountService;
    @Mock
    private CardAccountHandler cardAccountHandler;

    @BeforeEach
    void setUp() {
        accountConsent = createConsent(createAccountAccess());
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        cardTransactionsReportByPeriodObject = buildTransactionsReportByPeriodObject();
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

    }

    @Test
    void getCardTransactionsReportByPeriod_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());
        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getCardTransactionsReportByPeriod_Failure_AllowedAccountDataHasError() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getCardTransactionsReportValidator.validate(cardTransactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_Failure_SpiResponseHasError() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(SPI_CARD_TRANSACTION_REPORT));
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(SPI_CARD_TRANSACTION_REPORT), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getCardTransactionsReportByPeriod_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getCardTransactionsReportValidator.validate(cardTransactionsReportByPeriodObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_With406ErrorInSpiTransactionReport() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorServiceNotSupportedSpiResponse());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertThatErrorIs(actualResponse, REQUESTED_FORMATS_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_Success() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));

        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<SpiTransactionReportParameters> argumentCaptor = ArgumentCaptor.forClass(SpiTransactionReportParameters.class);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getCardAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertThat(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList())).isTrue();

        verify(cardAccountSpi).requestCardTransactionsForAccount(any(SpiContextData.class), argumentCaptor.capture(), any(SpiAccountReference.class), any(SpiAccountConsent.class), eq(null));
        checkPassingParametersWithoutAnyChanges(argumentCaptor.getValue());
    }

    @Test
    void getCardTransactionsReportByPeriod_WhenConsentIsGlobal_Success() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(SPI_ACCOUNT_REFERENCE_GLOBAL);

        AisConsent aisConsent = createConsent(createAccountAccess());

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), SPI_ACCOUNT_REFERENCE_GLOBAL, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));

        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getCardAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
        assertTrue(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList()));
    }

    @Test
    void getCardTransactionsReportByPeriod_WhenConsentHasNoTransactions_Success() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(SPI_ACCOUNT_REFERENCE_GLOBAL);

        AisConsent aisConsent = createConsent(createAccountAccessWithoutTransactions());

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), SPI_ACCOUNT_REFERENCE_GLOBAL, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));

        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardTransactionsReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getCardAccountReport()).isEqualTo(xs2aAccountReport);
        assertThat(body.getAccountReference()).isNull();
        assertTrue(CollectionUtils.isEqualCollection(body.getBalances(), Collections.emptyList()));
    }

    @Test
    void getCardTransactionsReportByPeriod_Success_ShouldRecordEvent() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));
        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_CARD_TRANSACTION_LIST_REQUEST_RECEIVED);
    }

    @Test
    void getCardTransactionsReportByPeriod_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardTransactionsReport> actualResponse = cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

        // Then
        verify(getCardTransactionsReportValidator).validate(cardTransactionsReportByPeriodObject);
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getCardTransactionsReportByPeriod_shouldRecordStatusInLoggingContext() {
        // Given
        when(getCardTransactionsReportValidator.validate(any(CardTransactionsReportByPeriodObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(cardAccountSpi.requestCardTransactionsForAccount(SPI_CONTEXT_DATA, buildSpiTransactionReportParameters(), spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(SPI_CARD_TRANSACTION_REPORT));
        Xs2aCardAccountReport xs2aAccountReport = new Xs2aCardAccountReport(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);
        when(spiCardTransactionListToXs2aAccountReportMapper.mapToXs2aCardAccountReport(BookingStatus.BOTH, Collections.emptyList(), null))
            .thenReturn(Optional.of(xs2aAccountReport));
        when(balanceMapper.mapToXs2aBalanceList(Collections.emptyList()))
            .thenReturn(Collections.emptyList());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        cardTransactionService.getCardTransactionsReportByPeriod(XS2A_TRANSACTIONS_REPORT_BY_PERIOD_REQUEST);

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
                   .payload((T) SPI_CARD_TRANSACTION_REPORT)
                   .error(new TppMessage(SERVICE_NOT_SUPPORTED))
                   .build();
    }

    private void assertThatErrorIs(ResponseObject actualResponse, MessageErrorCode messageErrorCode) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(messageErrorCode);
    }

    private void assertResponseHasNoErrors(ResponseObject actualResponse) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();
    }

    private static AisConsent createConsent(AccountAccess accountAccess) {
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
        aisConsent.setTppAccountAccesses(accountAccess);
        aisConsent.setAspspAccountAccesses(accountAccess);
        return aisConsent;
    }

    private static AisConsentData buildAisConsentData() {
        return new AisConsentData(null, null, null, false);
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

    private static AccountAccess createAccountAccess() {
        return new AccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null);
    }

    private static AccountAccess createAccountAccessWithoutTransactions() {
        return new AccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null, null);
    }

    private static SpiAccountReference buildSpiAccountReferenceGlobal() {
        return SpiAccountReference.builder().resourceId(ACCOUNT_ID).build();
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY, null);
    }

    // Needed because SpiCardTransactionReport is final, so it's impossible to mock it
    private static SpiCardTransactionReport buildSpiTransactionReport() {
        return new SpiCardTransactionReport(BASE64_STRING_EXAMPLE, Collections.emptyList(), Collections.emptyList(), SpiTransactionReport.RESPONSE_TYPE_JSON, null);
    }

    private CardTransactionsReportByPeriodObject buildTransactionsReportByPeriodObject() {
        return new CardTransactionsReportByPeriodObject(accountConsent, ACCOUNT_ID, REQUEST_URI, DELTA_LIST, MediaType.APPLICATION_JSON_VALUE, BOOKING_STATUS, DATE_FROM, DATE_TO);
    }

    private static Xs2aCardTransactionsReportByPeriodRequest buildXs2aTransactionsReportByPeriodRequest() {
        return new Xs2aCardTransactionsReportByPeriodRequest(CONSENT_ID, ACCOUNT_ID, MediaType.APPLICATION_JSON_VALUE, DATE_FROM, DATE_TO, BOOKING_STATUS, REQUEST_URI, ENTRY_REFERENCE_FROM, DELTA_LIST, null, null);
    }

    private SpiTransactionReportParameters buildSpiTransactionReportParameters() {
        return new SpiTransactionReportParameters(MediaType.APPLICATION_JSON_VALUE, false, DATE_FROM, DATE_TO, BOOKING_STATUS, ENTRY_REFERENCE_FROM, DELTA_LIST, null, null);
    }


}
