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

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.AccountMappersHolder;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetBalancesReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountBalanceRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String ACCOUNT_ID = "Test accountId";
    private static final String IBAN = "DE52500105173911841934";
    private static final String BBAN = "89370400440532010000";
    private static final String PAN = "2356 5746 3217 1234";
    private static final String MASKED_PAN = "235657******1234";
    private static final String MSISDN = "+49(0)911 360698-0";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final MessageError CONSENT_INVALID_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));

    private SpiAccountReference spiAccountReference;
    private AisConsent aisConsent;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    private GetAccountBalanceRequestObject getAccountBalanceRequestObject;
    private final JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private BalanceService balanceService;

    @Mock
    private AccountSpi accountSpi;
    @Mock
    AccountMappersHolder accountMappersHolder;
    @Mock
    AccountServicesHolder accountServicesHolder;
    @Mock
    private Xs2aBalancesReport xs2aBalancesReport;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private GetBalancesReportValidator getBalancesReportValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private LoggingContextService loggingContextService;

    @BeforeEach
    void setUp() {
        aisConsent = createConsent();
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
        getAccountBalanceRequestObject = buildCommonAccountBalanceRequestObject();
    }

    @Test
    void getBalancesReport_Failure_NoAccountConsent() {
        // Given
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getBalancesReport_Failure_AllowedAccountDataHasError() {
        // Given
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getBalancesReportValidator.validate(getAccountBalanceRequestObject))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_ERROR));

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getBalancesReport_Failure_SpiResponseHasError() {
        // Given
        when(getBalancesReportValidator.validate(any(GetAccountBalanceRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountServicesHolder.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountServicesHolder.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(Collections.emptyList()));
        when(accountMappersHolder.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountMappersHolder.mapToErrorHolder(buildErrorSpiResponse(Collections.EMPTY_LIST), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getBalancesReport_Failure_ConsentNotContainsAccountReference() {
        // Given
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getBalancesReportValidator.validate(getAccountBalanceRequestObject))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_ERROR));

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getBalancesReport_Success() {
        // Given
        when(getBalancesReportValidator.validate(any(GetAccountBalanceRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountServicesHolder.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountServicesHolder.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountServicesHolder.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(Collections.emptyList()));
        when(accountMappersHolder.mapToXs2aBalancesReportSpi(spiAccountReference, Collections.emptyList()))
            .thenReturn(xs2aBalancesReport);
        when(xs2aBalancesReport.getXs2aAccountReference())
            .thenReturn(XS2A_ACCOUNT_REFERENCE);
        when(accountMappersHolder.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);
        Xs2aBalancesReport body = actualResponse.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getXs2aAccountReference()).isEqualTo(XS2A_ACCOUNT_REFERENCE);
    }

    @Test
    void getBalancesReport_Success_ShouldRecordEvent() {
        // Given
        when(getBalancesReportValidator.validate(any(GetAccountBalanceRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountServicesHolder.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountServicesHolder.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountServicesHolder.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(Collections.emptyList()));
        when(accountMappersHolder.mapToXs2aBalancesReportSpi(spiAccountReference, Collections.emptyList()))
            .thenReturn(xs2aBalancesReport);
        when(accountMappersHolder.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_BALANCE_REQUEST_RECEIVED);
    }

    @Test
    void getBalancesReport_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getBalancesReportValidator.validate(any(GetAccountBalanceRequestObject.class)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_ERROR));

        // When
        ResponseObject<Xs2aBalancesReport> actualResponse = balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        verify(getBalancesReportValidator).validate(getAccountBalanceRequestObject);
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getBalancesReport_shouldRecordStatusIntoLoggingContext() {
        // Given
        when(getBalancesReportValidator.validate(any(GetAccountBalanceRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountServicesHolder.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountServicesHolder.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountServicesHolder.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountServicesHolder.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(accountSpi.requestBalancesForAccount(SPI_CONTEXT_DATA, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(Collections.emptyList()));
        when(accountMappersHolder.mapToXs2aBalancesReportSpi(spiAccountReference, Collections.emptyList()))
            .thenReturn(xs2aBalancesReport);
        when(accountMappersHolder.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        balanceService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    private void assertResponseHasNoErrors(ResponseObject<Xs2aBalancesReport> actualResponse) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();
    }

    private void assertThatErrorIs(ResponseObject<Xs2aBalancesReport> actualResponse, MessageErrorCode messageErrorCode) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();

        TppMessageInformation tppMessage = actualResponse.getError().getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(messageErrorCode);
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

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY, null);
    }

    private AisConsent createConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        AccountAccess accountAccess = createAccountAccess();
        aisConsent.setTppAccountAccesses(accountAccess);
        aisConsent.setAspspAccountAccesses(accountAccess);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        return aisConsent;
    }

    private static AccountAccess createAccountAccess() {
        return new AccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null);
    }

    private GetAccountBalanceRequestObject buildCommonAccountBalanceRequestObject() {
        return new GetAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI);
    }
}
