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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetAccountListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountListServiceTest {
    private static final JsonReader jsonReader = new JsonReader();
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final boolean WITH_BALANCE = false;
    private static final String CONSENT_ID = "Test consentId";
    private static final String ACCOUNT_ID = "Test accountId";
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final String REQUEST_URI = "request/uri";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = buildSpiAccountConsent();
    private static final List<SpiAccountDetails> EMPTY_ACCOUNT_DETAILS_LIST = Collections.emptyList();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE = buildXs2aAccountReference();
    private static final AccountReference XS2A_ACCOUNT_REFERENCE_WITHOUT_ASPSP_IDS = buildXs2aAccountReferenceWithoutAspspIds();
    private static final SpiContextData SPI_CONTEXT_DATA = buildSpiContextData();
    private static final MessageError VALIDATION_ERROR = buildMessageError();

    private AccountConsent accountConsent;
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
    private RequestProviderService requestProviderService;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;

    @Before
    public void setUp() {
        accountConsent = createConsent(createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
        getAccountListConsentObject = buildGetAccountListConsentObject();

        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any())).thenReturn(ActionStatus.SUCCESS);
    }

    @Test
    public void getAccountDetailsList_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    public void getAccountDetailsList_Failure_AllowedAccountDataHasError() {
        when(getAccountListValidator.validate(getAccountListConsentObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    public void getAccountDetailsList_Failure_SpiResponseHasError() {
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
    public void getAccountDetailsList_Failure_AccountConsentUpdatedIsEmpty() {
        // Given
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList())).thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    public void getAccountDetailsList_Success() {
        // Given
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList())).thenReturn(Optional.of(accountConsent));

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
    public void getAccountDetailsList_shouldUpdateAccountReferences() {
        // Given
        AccountConsent accountConsent = createConsent(createAccountAccess(XS2A_ACCOUNT_REFERENCE_WITHOUT_ASPSP_IDS));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        AccountConsent updatedAccountConsent = createConsent(createAccountAccess(XS2A_ACCOUNT_REFERENCE));
        when(accountReferenceUpdater.updateAccountReferences(CONSENT_ID, accountConsent.getAccess(), xs2aAccountDetailsList))
            .thenReturn(Optional.of(updatedAccountConsent));

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        Xs2aAccountListHolder responseBody = actualResponse.getBody();
        assertThat(responseBody.getAccountDetails()).isEqualTo(xs2aAccountDetailsList);

        verify(accountReferenceUpdater).updateAccountReferences(CONSENT_ID, accountConsent.getAccess(), xs2aAccountDetailsList);
        assertThat(responseBody.getAccountConsent()).isEqualTo(updatedAccountConsent);
    }

    @Test
    public void getAccountList_Success_ShouldRecordEvent() {
        // Given
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));
        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(Optional.of(accountConsent));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.READ_ACCOUNT_LIST_REQUEST_RECEIVED);
    }

    @Test
    public void getAccountList_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountListValidator.validate(any(GetAccountListConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAccountListHolder> actualResponse = accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(getAccountListValidator).validate(getAccountListConsentObject);

        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    public void consentActionLog_recurringConsentWithIpAddress_needsToUpdateUsageFalse() {
        // Given
        AccountConsent accountConsent = createConsent(true);
        prepationForGetAccountListRequest(accountConsent);
        when(accountHelperService.needsToUpdateUsage(accountConsent)).thenReturn(false);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(aisConsentService, atLeastOnce()).consentActionLog(null, CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, false);
    }

    @Test
    public void consentActionLog_recurringConsentWithoutIpAddress_needsToUpdateUsageTrue() {
        // Given
        AccountConsent accountConsent = createConsent(true);
        prepationForGetAccountListRequest(accountConsent);
        when(accountHelperService.needsToUpdateUsage(accountConsent)).thenReturn(true);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(aisConsentService, atLeastOnce()).consentActionLog(null, CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, true);
    }

    @Test
    public void consentActionLog_oneOffConsentWithIpAddress_needsToUpdateUsageTrue() {
        // Given
        AccountConsent accountConsent = createConsent(false);
        prepationForGetAccountListRequest(accountConsent);
        when(accountHelperService.needsToUpdateUsage(accountConsent)).thenReturn(true);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(aisConsentService, atLeastOnce()).consentActionLog(null, CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, true);
    }

    @Test
    public void consentActionLog_oneOffConsentWithoutIpAddress_needsToUpdateUsageTrue() {
        // Given
        AccountConsent accountConsent = createConsent(false);
        prepationForGetAccountListRequest(accountConsent);
        when(accountHelperService.needsToUpdateUsage(accountConsent)).thenReturn(true);

        // When
        accountListService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);

        // Then
        verify(aisConsentService, atLeastOnce()).consentActionLog(null, CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, true);
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

    private void prepationForGetAccountListRequest(AccountConsent accountConsent) {
        List<SpiAccountDetails> spiAccountDetailsList = Collections.singletonList(spiAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(accountSpi.requestAccountList(SPI_CONTEXT_DATA, WITH_BALANCE, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));
        List<Xs2aAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);
        when(accountReferenceUpdater.updateAccountReferences(eq(CONSENT_ID), any(), anyList())).thenReturn(Optional.of(accountConsent));
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse() {
        return SpiResponse.<T>builder()
                   .payload((T) AccountListServiceTest.EMPTY_ACCOUNT_DETAILS_LIST)
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    private static AccountReference buildXs2aAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    private static AccountReference buildXs2aAccountReferenceWithoutAspspIds() {
        return new AccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    private static AccountConsent createConsent(Xs2aAccountAccess access) {
        return new AccountConsent(CONSENT_ID, access, access, false, LocalDate.now(), 4, null, ConsentStatus.VALID, false, false, null, createTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.now(), Collections.emptyMap());
    }

    private static AccountConsent createConsent(boolean recurringIndicator) {
        String fileName = recurringIndicator
                              ? "json/AccountConsentRecurringIndicatorTrue.json"
                              : "json/AccountConsentRecurringIndicatorFalse.json";
        return jsonReader.getObjectFromFile(fileName, AccountConsent.class);
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static Xs2aAccountAccess createAccountAccess(AccountReference accountReference) {
        return new Xs2aAccountAccess(Collections.singletonList(accountReference), Collections.singletonList(accountReference), Collections.singletonList(accountReference), null, null, null, null);
    }

    private static SpiContextData buildSpiContextData() {
        return new SpiContextData(new SpiPsuData(null, null, null, null, null), new TppInfo(), UUID.randomUUID(), UUID.randomUUID());
    }

    @NotNull
    private static MessageError buildMessageError() {
        return new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    }

    @NotNull
    private static SpiAccountConsent buildSpiAccountConsent() {
        return new SpiAccountConsent();
    }

    @NotNull
    private GetAccountListConsentObject buildGetAccountListConsentObject() {
        return new GetAccountListConsentObject(accountConsent, WITH_BALANCE, REQUEST_URI);
    }
}
