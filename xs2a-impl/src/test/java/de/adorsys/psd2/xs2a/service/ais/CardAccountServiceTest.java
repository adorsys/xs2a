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
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
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
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountListHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetCardAccountDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetCardAccountListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountDetailsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CardAccountSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardAccountServiceTest {

    private static final JsonReader jsonReader = new JsonReader();
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String ACCOUNT_ID = "Test accountId";

    private static final String REQUEST_URI = "request/uri";
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final AccountReference ACCOUNT_REFERENCE = jsonReader.getObjectFromFile("json/service/account/xs2a-account-reference-full.json", AccountReference.class);
    private static final AccountReference ACCOUNT_REFERENCE_WITHOUT_ASPSP_IDS = jsonReader.getObjectFromFile("json/service/account/xs2a-account-reference-without-bank_ids.json", AccountReference.class);
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final MessageError VALIDATION_ERROR = buildMessageError();

    private AisConsent aisConsent;
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    private GetCardAccountListConsentObject getCardAccountListConsentObject;
    private SpiAccountReference spiAccountReference;
    private GetCardAccountDetailsRequestObject getCardAccountDetailsRequestObject;

    @InjectMocks
    private CardAccountService cardAccountService;

    @Mock
    private CardAccountSpi cardAccountSpi;
    @Mock
    private SpiToXs2aAccountDetailsMapper accountDetailsMapper;
    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;
    @Mock
    private TppService tppService;
    @Mock
    private SpiCardAccountDetails spiCardAccountDetails;
    @Mock
    private Xs2aCardAccountDetails xs2aAccountDetails;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private GetCardAccountListValidator getCardAccountListValidator;
    @Mock
    private GetCardAccountDetailsValidator getCardAccountDetailsValidator;
    @Mock
    private SpiAspspConsentDataProviderFactory spiAspspConsentDataProviderFactory;
    @Mock
    private AccountHelperService accountHelperService;
    @Mock
    private LoggingContextService loggingContextService;

    @BeforeEach
    void setUp() {
        aisConsent = createConsent(createAccountAccess(ACCOUNT_REFERENCE));
        spiAspspConsentDataProvider = spiAspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID);
        getCardAccountListConsentObject = buildGetAccountListConsentObject();
        spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json", SpiAccountReference.class);
        getCardAccountDetailsRequestObject = buildCommonAccountRequestObject();

        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(aisConsent));
    }

    @Test
    void getAccountDetailsList_Failure_AccountConsentUpdatedHasChecksumError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        List<SpiCardAccountDetails> spiAccountDetailsList = Collections.singletonList(spiCardAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aCardAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        when(accountReferenceUpdater.updateCardAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder()
                            .error(CmsError.CHECKSUM_ERROR)
                            .build());
        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_VALIDATION_FAILED);
    }

    @Test
    void getAccountDetailsList_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getAccountDetailsList_Failure_AllowedAccountDataHasError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        when(getCardAccountListValidator.validate(getCardAccountListConsentObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountDetailsList_Failure_SpiResponseHasError() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponse());

        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponse(), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_400)
                            .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                            .build());
        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getAccountDetailsList_Failure_AccountConsentUpdatedIsEmpty() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);

        List<SpiCardAccountDetails> spiAccountDetailsList = Collections.singletonList(spiCardAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aCardAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        when(accountReferenceUpdater.updateCardAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());
        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getAccountDetailsList_Success() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);

        List<SpiCardAccountDetails> spiAccountDetailsList = Collections.singletonList(spiCardAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aCardAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        when(accountReferenceUpdater.updateCardAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder()
                            .payload(aisConsent)
                            .build());

        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardAccountListHolder body = actualResponse.getBody();

        assertThat(CollectionUtils.isNotEmpty(body.getCardAccountDetails())).isTrue();

        List<Xs2aCardAccountDetails> accountDetailsList = body.getCardAccountDetails();

        assertThat(CollectionUtils.isNotEmpty(accountDetailsList)).isTrue();
        assertThat(CollectionUtils.isEqualCollection(accountDetailsList, xs2aAccountDetailsList)).isTrue();
    }

    @Test
    void getAccountDetailsList_shouldUpdateAccountReferences() {
        // Given
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);

        AisConsent accountConsent = createConsent(createAccountAccess(ACCOUNT_REFERENCE_WITHOUT_ASPSP_IDS));

        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(accountConsent));

        List<SpiCardAccountDetails> spiAccountDetailsList = Collections.singletonList(spiCardAccountDetails);

        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aCardAccountDetails> xs2aAccountDetailsList = Collections.singletonList(xs2aAccountDetails);

        when(accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aAccountDetailsList);

        AisConsent updatedAccountConsent = createConsent(createAccountAccess(ACCOUNT_REFERENCE));
        when(accountReferenceUpdater.updateCardAccountReferences(CONSENT_ID, accountConsent, xs2aAccountDetailsList))
            .thenReturn(CmsResponse.<AisConsent>builder()
                            .payload(updatedAccountConsent)
                            .build());
        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);
        Xs2aCardAccountListHolder responseBody = actualResponse.getBody();
        assertThat(responseBody.getCardAccountDetails()).isEqualTo(xs2aAccountDetailsList);

        verify(accountReferenceUpdater).updateCardAccountReferences(CONSENT_ID, accountConsent, xs2aAccountDetailsList);
        assertThat(responseBody.getAisConsent()).isEqualTo(updatedAccountConsent);
    }

    @Test
    void getAccountList_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));

        // When
        ResponseObject<Xs2aCardAccountListHolder> actualResponse = cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        verify(getCardAccountListValidator).validate(getCardAccountListConsentObject);

        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountList_shouldRecordStatusIntoLoggingContext() {
        // Given
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(aisConsentService.getAccountConsentById(CONSENT_ID))
            .thenReturn(Optional.of(aisConsent));
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        List<SpiCardAccountDetails> spiAccountDetailsList = Collections.singletonList(spiCardAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));
        when(accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiAccountDetailsList))
            .thenReturn(Collections.singletonList(xs2aAccountDetails));
        when(accountReferenceUpdater.updateCardAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder()
                            .payload(aisConsent)
                            .build());
        ArgumentCaptor<ConsentStatus> argumentCaptor = ArgumentCaptor.forClass(ConsentStatus.class);

        // When
        cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

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
        when(getCardAccountListValidator.validate(any(GetCardAccountListConsentObject.class)))
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
        cardAccountService.getCardAccountList(CONSENT_ID, REQUEST_URI);

        // Then
        verify(aisConsentService, atLeastOnce()).consentActionLog(null, CONSENT_ID, ActionStatus.SUCCESS, REQUEST_URI, updateUsage, null, null);
    }

    @Test
    void getAccountDetails_Failure_NoAccountConsent() {
        // Given
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.empty());
        // When
        ResponseObject<Xs2aCardAccountDetailsHolder> actualResponse = cardAccountService.getCardAccountDetails(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);
        // Then
        assertThatErrorIs(actualResponse, CONSENT_UNKNOWN_400);
    }

    @Test
    void getAccountDetails_Failure_AllowedAccountDataHasError() {
        // Given
        when(getCardAccountDetailsValidator.validate(getCardAccountDetailsRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardAccountDetailsHolder> actualResponse = cardAccountService.getCardAccountDetails(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountDetails_Failure_SpiResponseHasError() {
        // Given
        when(getCardAccountDetailsValidator.validate(any(GetCardAccountDetailsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(cardAccountSpi.requestCardAccountDetailsForAccount(SPI_CONTEXT_DATA, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildErrorSpiResponseDetails());
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(spiErrorMapper.mapToErrorHolder(buildErrorSpiResponseDetails(), ServiceType.AIS))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.AIS_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                            .build());
        // When
        ResponseObject<Xs2aCardAccountDetailsHolder> actualResponse = cardAccountService.getCardAccountDetails(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, FORMAT_ERROR);
    }

    @Test
    void getAccountDetails_failure_accountReferenceNotFoundInAccountAccess() {
        // Given
        when(getCardAccountDetailsValidator.validate(getCardAccountDetailsRequestObject))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCardAccountDetailsHolder> actualResponse = cardAccountService.getCardAccountDetails(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertThatErrorIs(actualResponse, CONSENT_INVALID);
    }

    @Test
    void getAccountDetails_Success() {
        // Given
        when(getCardAccountDetailsValidator.validate(any(GetCardAccountDetailsRequestObject.class)))
            .thenReturn(ValidationResult.valid());
        when(accountHelperService.findAccountReference(any(), any()))
            .thenReturn(spiAccountReference);
        when(accountHelperService.getSpiContextData())
            .thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any()))
            .thenReturn(ActionStatus.SUCCESS);
        when(cardAccountSpi.requestCardAccountDetailsForAccount(SPI_CONTEXT_DATA, spiAccountReference, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiCardAccountDetails));
        when(accountDetailsMapper.mapToXs2aCardAccountDetails(spiCardAccountDetails))
            .thenReturn(xs2aAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        // When
        ResponseObject<Xs2aCardAccountDetailsHolder> actualResponse = cardAccountService.getCardAccountDetails(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardAccountDetails body = actualResponse.getBody().getCardAccountDetails();

        assertThat(body).isNotNull().isEqualTo(xs2aAccountDetails);
    }

    @Test
    void getAccountDetailsForGlobalConsent_Success() {
        // Given
        //global consent
        aisConsent.setConsentData(new AisConsentData(null, AccountAccessType.ALL_ACCOUNTS, null, false));
        ArgumentCaptor<SpiAccountReference> spiAccountReferenceCaptor = ArgumentCaptor.forClass(SpiAccountReference.class);

        when(getCardAccountDetailsValidator.validate(any(GetCardAccountDetailsRequestObject.class))).thenReturn(ValidationResult.valid());
        when(accountHelperService.getSpiContextData()).thenReturn(SPI_CONTEXT_DATA);
        when(accountHelperService.createActionStatus(anyBoolean(), any(), any())).thenReturn(ActionStatus.SUCCESS);
        when(consentMapper.mapToSpiAccountConsent(any())).thenReturn(SPI_ACCOUNT_CONSENT);
        when(cardAccountSpi.requestCardAccountDetailsForAccount(eq(SPI_CONTEXT_DATA), spiAccountReferenceCaptor.capture(), eq(SPI_ACCOUNT_CONSENT), eq(spiAspspConsentDataProvider)))
            .thenReturn(buildSuccessSpiResponse(spiCardAccountDetails));
        when(accountDetailsMapper.mapToXs2aCardAccountDetails(spiCardAccountDetails)).thenReturn(xs2aAccountDetails);


        // When
        ResponseObject<Xs2aCardAccountDetailsHolder> actualResponse = cardAccountService.getCardAccountDetails(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);

        // Then
        assertResponseHasNoErrors(actualResponse);

        Xs2aCardAccountDetails body = actualResponse.getBody().getCardAccountDetails();

        assertThat(body).isNotNull().isEqualTo(xs2aAccountDetails);

        verify(accountHelperService, never()).findAccountReference(any(), any());
        assertThat(spiAccountReferenceCaptor.getValue().getResourceId()).isEqualTo(ACCOUNT_ID);
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

    private void prepationForGetAccountListRequest(AisConsent accountConsent) {
        List<SpiCardAccountDetails> spiAccountDetailsList = Collections.singletonList(spiCardAccountDetails);
        when(consentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);
        when(cardAccountSpi.requestCardAccountList(SPI_CONTEXT_DATA, SPI_ACCOUNT_CONSENT, spiAspspConsentDataProvider))
            .thenReturn(buildSuccessSpiResponse(spiAccountDetailsList));

        List<Xs2aCardAccountDetails> xs2aCardAccountDetailsList = Collections.singletonList(xs2aAccountDetails);
        when(accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiAccountDetailsList))
            .thenReturn(xs2aCardAccountDetailsList);
        when(accountReferenceUpdater.updateCardAccountReferences(eq(CONSENT_ID), any(), anyList()))
            .thenReturn(CmsResponse.<AisConsent>builder()
                            .payload(accountConsent)
                            .build());
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildSuccessSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<List<SpiCardAccountDetails>> buildErrorSpiResponse() {
        return SpiResponse.<List<SpiCardAccountDetails>>builder()
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiCardAccountDetails> buildErrorSpiResponseDetails() {
        return SpiResponse.<SpiCardAccountDetails>builder()
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }

    private static AisConsent createConsent(AccountAccess access) {
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
        aisConsent.setTppAccountAccesses(createAccountAccess(ACCOUNT_REFERENCE));
        aisConsent.setAspspAccountAccesses(createAccountAccess(ACCOUNT_REFERENCE));
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

    private static AisConsent createConsent(boolean recurringIndicator) {
        String fileName = recurringIndicator
                              ? "json/AccountConsentRecurringIndicatorTrue.json"
                              : "json/AccountConsentRecurringIndicatorFalse.json";
        return jsonReader.getObjectFromFile(fileName, AisConsent.class);
    }

    private static TppInfo createTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(UUID.randomUUID().toString());
        return tppInfo;
    }

    private static AccountAccess createAccountAccess(AccountReference accountReference) {
        return new AccountAccess(Collections.singletonList(accountReference), Collections.singletonList(accountReference), Collections.singletonList(accountReference), null);
    }

    @NotNull
    private static MessageError buildMessageError() {
        return new MessageError(ErrorType.AIS_401, of(CONSENT_INVALID));
    }

    @NotNull
    private GetCardAccountListConsentObject buildGetAccountListConsentObject() {
        return new GetCardAccountListConsentObject(aisConsent, REQUEST_URI);
    }

    @NotNull
    private GetCardAccountDetailsRequestObject buildCommonAccountRequestObject() {
        return new GetCardAccountDetailsRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI);
    }

}
