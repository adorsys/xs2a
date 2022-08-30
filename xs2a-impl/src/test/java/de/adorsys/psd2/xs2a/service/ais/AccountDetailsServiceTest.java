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
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
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
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetAccountDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.jetbrains.annotations.NotNull;
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
class AccountDetailsServiceTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final boolean WITH_BALANCE = false;
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
    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));

    private SpiAccountReference spiAccountReference;
    private AisConsent aisConsent;
    private CommonAccountRequestObject commonAccountRequestObject;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private AccountDetailsService accountDetailsService;

    @Mock
    private AccountSpi accountSpi;
    @Mock
    private SpiToXs2aAccountDetailsMapper accountDetailsMapper;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private SpiAccountDetails spiAccountDetails;
    @Mock
    private Xs2aAccountDetails xs2aAccountDetails;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetAccountDetailsValidator getAccountDetailsValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        aisConsent.setTppAccountAccesses(createAccountAccess());
        aisConsent.setConsentData(new AisConsentData(null, null, null, false));
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        commonAccountRequestObject = buildCommonAccountRequestObject();
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
    }

    @Test
    void getAccountDetails_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getAccountDetails_Failure_AllowedAccountDataHasError() {
        // Given
        when(getAccountDetailsValidator.validate(commonAccountRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountDetails_Failure_SpiResponseHasError() {
        // Given
        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse(spiAccountDetails));
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(spiAccountDetails), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getAccountDetails_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getAccountDetailsValidator.validate(commonAccountRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountDetails_Success() {
        // Given
        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));
        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails))
            .thenReturn(xs2aAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aAccountDetails body = actualResponse.getBody().getAccountDetails();

        assertThat(body).isNotNull().isEqualTo(xs2aAccountDetails);
    }

    @Test
    void getAccountDetailsForGlobalConsent_Success() {
        // Given
        //global consent
        aisConsent.setConsentData(new AisConsentData(null, AccountAccessType.ALL_ACCOUNTS, null, false));
        ArgumentCaptor<SpiAccountReference> spiAccountReferenceCaptor = ArgumentCaptor.forClass(SpiAccountReference.class);

        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class))).thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any())).thenReturn(ActionStatus.SUCCESS);
        when(consentMapper.mapToSpiAccountConsent(any())).thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountDetailForAccount(eq(SPI_CONTEXT_DATA), eq(WITH_BALANCE), spiAccountReferenceCaptor.capture(), eq(SPI_ACCOUNT_CONSENT), eq(spiAspspConsentDataProvider)))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));
        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails)).thenReturn(xs2aAccountDetails);


        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aAccountDetails body = actualResponse.getBody().getAccountDetails();

        assertThat(body).isNotNull().isEqualTo(xs2aAccountDetails);

        verify(accountHelperService, never()).findAccountReference(any(), any());
        assertThat(spiAccountReferenceCaptor.getValue().getResourceId()).isEqualTo(ACCOUNT_ID);
    }

    @Test
    void getAccountDetails_Success_ShouldRecordEvent() {
        // Given
        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));
        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails))
            .thenReturn(xs2aAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_ACCOUNT_DETAILS_REQUEST_RECEIVED);
    }

    @Test
    void getAccountDetails_shouldRecordStatusIntoLoggingContext() {
        // Given
        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(accountSpi.requestAccountDetailForAccount(SPI_CONTEXT_DATA, WITH_BALANCE, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetails));
        when(accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails))
            .thenReturn(xs2aAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    @Test
    void getAccountDetails_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountDetailsValidator.validate(any(CommonAccountRequestObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAccountDetailsHolder> actualResponse = accountDetailsService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(getAccountDetailsValidator).validate(commonAccountRequestObject);
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    private void assertResponseHasNoErrors(ResponseObject actualResponse) {
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isFalse();
    }

    private void assertThatErrorIs(ResponseObject actualResponse, MessageErrorCode messageErrorCode) {
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

    private AccountAccess createAccountAccess() {
        return new AccountAccess(Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), Collections.singletonList(XS2A_ACCOUNT_REFERENCE), null);
    }

    @NotNull
    private CommonAccountRequestObject buildCommonAccountRequestObject() {
        return new CommonAccountRequestObject(aisConsent, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);
    }
}
