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
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
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
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetAccountListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountListServiceTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String ASPSP_ACCOUNT_ID = "0000921mxl-n2131-13nw";
    private static final boolean WITH_BALANCE = false;
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String IBAN = "DE52500105173911841934";
    private static final String BBAN = "89370400440532010000";
    private static final String PAN = "2356 5746 3217 1234";
    private static final String MASKED_PAN = "235657******1234";
    private static final String MSISDN = "+49(0)911 360698-0";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final List<SpiAccountDetails> EMPTY_ACCOUNT_DETAILS_LIST = Collections.emptyList();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final MessageError CONSENT_INVALID_401_ERROR = new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));

    private AisConsent aisConsent;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    private GetAccountListConsentObject getAccountListConsentObject;

    @InjectMocks
    private AccountListService accountListService;

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
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetAccountListValidator getAccountListValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;

    @BeforeEach
    void setUp() {
        aisConsent = createConsent(false);
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
        getAccountListConsentObject = buildGetAccountListConsentObject();
    }

    @Test
    void getAccountDetailsList_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getAccountDetailsList_Failure_AllowedAccountDataHasError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getAccountListValidator.validate(getAccountListConsentObject))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));

        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountDetailsList_Failure_SpiResponseHasError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse());
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_400)
                            .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                            .build());

        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getAccountDetailsList_Failure_AccountConsentUpdatedIsEmpty() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder().error(CmsError.LOGICAL_ERROR).build());

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getAccountDetailsList_Success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder().payload(aisConsent).build());

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aAccountListHolder body = actualResponse.getBody();
        assertThat(CollectionUtils.isNotEmpty(body.getAccountDetails())).isTrue();
        List<Xs2aAccountDetails> accountDetailsList = body.getAccountDetails();
        assertThat(CollectionUtils.isNotEmpty(accountDetailsList)).isTrue();
        assertThat(CollectionUtils.isEqualCollection(accountDetailsList, xs2aAccountDetailsList)).isTrue();
    }

    @Test
    void getAccountDetailsList_shouldUpdateAccountReferences() {
        // Given
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);

        AisConsent aisConsent = createConsent(false);

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        AisConsent updatedAccountConsent = createConsent(false);
        when(accountReferenceUpdater.updateAccountReferences(CONSENT_ID, aisConsent, xs2aAccountDetailsList))
            .thenReturn(CmsResponse.<AisConsent>builder().payload(updatedAccountConsent).build());

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        Xs2aAccountListHolder responseBody = actualResponse.getBody();
        assertThat(responseBody.getAccountDetails()).isEqualTo(xs2aAccountDetailsList);

        verify(accountReferenceUpdater).updateAccountReferences(CONSENT_ID, aisConsent, xs2aAccountDetailsList);
        assertThat(responseBody.getAisConsent()).isEqualTo(updatedAccountConsent);
    }

    @Test
    void getAccountList_Success_ShouldRecordEvent() {
        // Given
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(xs2aEventService, times(1)).recordConsentTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_ACCOUNT_LIST_REQUEST_RECEIVED);
    }

    @Test
    void getAccountList_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.invalid(CONSENT_INVALID_401_ERROR));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(getAccountListValidator).validate(getAccountListConsentObject);

        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountList_shouldRecordStatusIntoLoggingContext() {
        // Given
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));
        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder().payload(aisConsent).build());
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(loggingContextService).storeConsentStatus(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(ConsentStatus.VALID);
    }

    private static Stream<Arguments> params() {
        return Stream.of(Arguments.arguments(true, false, false),
                         Arguments.arguments(true, true, true),
                         Arguments.arguments(false, true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    void consentActionLog(boolean recurringIndicator, boolean needsToUpdateUsage, boolean updateUsage) {
        // Given
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        AisConsent accountConsent = createConsent(recurringIndicator);
        prepationForGetAccountListRequest(accountConsent);
        when(accountHelperService.needsToUpdateUsage(accountConsent))
            .thenReturn(needsToUpdateUsage);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(aisConsentService, atLeastOnce()).consentActionLog(null, CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, updateUsage, null, null);
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

    private void prepationForGetAccountListRequest(AisConsent aisConsent) {
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));
        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList())).thenReturn(CmsResponse.<AisConsent>builder().payload(aisConsent).build());
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<List<SpiAccountDetails>> buildErrorSpiResponse() {
        return SpiResponse.<List<SpiAccountDetails>>builder()
                   .payload(AccountListServiceTest.EMPTY_ACCOUNT_DETAILS_LIST)
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY, null);
    }

    private AisConsent createConsent(boolean recurringIndicator) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);

        aisConsent.setConsentData(new AisConsentData(null,
                                                     null,
                                                     null,
                                                     false));
        aisConsent.setTppAccountAccesses(createAccountAccess());
        aisConsent.setAspspAccountAccesses(createAccountAccess());

        if (recurringIndicator) {
            aisConsent.setRecurringIndicator(true);
        }

        return aisConsent;
    }

    private static AccountAccess createAccountAccess() {
        return new AccountAccess(Collections.singletonList(AccountListServiceTest.XS2A_ACCOUNT_REFERENCE),
                                 Collections.singletonList(AccountListServiceTest.XS2A_ACCOUNT_REFERENCE),
                                 Collections.singletonList(AccountListServiceTest.XS2A_ACCOUNT_REFERENCE),
                                 null);
    }

    private GetAccountListConsentObject buildGetAccountListConsentObject() {
        return new GetAccountListConsentObject(aisConsent, WITH_BALANCE, REQUEST_URI);
    }
}
