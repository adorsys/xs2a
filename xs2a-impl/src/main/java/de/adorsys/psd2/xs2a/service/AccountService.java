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
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
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
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class AccountService {
    private final AccountSpi accountSpi;

    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final SpiToXs2aAccountDetailsMapper accountDetailsMapper;
    private final SpiToXs2aBalanceMapper balanceMapper;
    private final SpiToXs2aBalanceReportMapper balanceReportMapper;
    private final SpiToXs2aAccountReferenceMapper referenceMapper;
    private final SpiTransactionListToXs2aAccountReportMapper transactionsToAccountReportMapper;
    private final SpiToXs2aTransactionMapper spiToXs2aTransactionMapper;

    private final ValueValidatorService validatorService;
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final AisConsentDataService aisConsentDataService;
    private final Xs2aEventService xs2aEventService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiErrorMapper spiErrorMapper;

    private final GetAccountListValidator getAccountListValidator;
    private final GetAccountDetailsValidator getAccountDetailsValidator;
    private final GetBalancesReportValidator getBalancesReportValidator;
    private final GetTransactionsReportValidator getTransactionsReportValidator;
    private final GetTransactionDetailsValidator getTransactionDetailsValidator;

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @return response with {@link Xs2aAccountListHolder} containing the List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountListHolder> getAccountList(String consentId, boolean withBalance) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_ACCOUNT_LIST_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getAccountListValidator.validate(new GetAccountListConsentObject(accountConsent, withBalance));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiContextData contextData = getSpiContextData(accountConsent.getPsuIdDataList());

        SpiResponse<List<SpiAccountDetails>> spiResponse = accountSpi.requestAccountList(contextData, withBalance,
                                                                                         consentMapper.mapToSpiAccountConsent(accountConsent),
                                                                                         aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)))
                       .build();
        }

        List<Xs2aAccountDetails> accountDetails = accountDetailsMapper.mapToXs2aAccountDetailsList(spiResponse.getPayload());
        accountReferenceUpdater.updateAccountReferences(consentId, accountConsent.getAccess(), accountDetails);

        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetails, accountConsent);

        ResponseObject<Xs2aAccountListHolder> response =
            ResponseObject.<Xs2aAccountListHolder>builder().body(xs2aAccountListHolder).build();

        writeLogAndCheckConsent(consentId, withBalance, accountConsent, TypeAccess.ACCOUNT, response);

        return response;
    }

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent, depending on
     * withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param accountId   String representing a PSU`s Account at ASPSP
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @return response with {@link Xs2aAccountDetailsHolder} based on accountId with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountDetailsHolder> getAccountDetails(String consentId, String accountId, boolean withBalance) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_ACCOUNT_DETAILS_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getAccountDetailsValidator.validate(new CommonAccountRequestObject(accountConsent, accountId, withBalance));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getAccounts(), accountId);
        if (!requestedAccountReference.isPresent()) {
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(ErrorType.AIS_401, of(CONSENT_INVALID))
                       .build();
        }

        SpiContextData contextData = getSpiContextData(accountConsent.getPsuIdDataList());

        SpiResponse<SpiAccountDetails> spiResponse = accountSpi.requestAccountDetailForAccount(contextData, withBalance, requestedAccountReference.get(),
                                                                                               consentMapper.mapToSpiAccountConsent(accountConsent),
                                                                                               aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS))
                       .build();
        }

        SpiAccountDetails spiAccountDetails = spiResponse.getPayload();

        if (spiAccountDetails == null) {
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        Xs2aAccountDetails accountDetails = accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails);

        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = new Xs2aAccountDetailsHolder(accountDetails, accountConsent);

        ResponseObject<Xs2aAccountDetailsHolder> response =
            ResponseObject.<Xs2aAccountDetailsHolder>builder().body(xs2aAccountDetailsHolder).build();

        writeLogAndCheckConsent(consentId, withBalance, accountConsent, TypeAccess.ACCOUNT, response);

        return response;
    }

    /**
     * Gets Balances Report based on consentId and accountId
     *
     * @param consentId String representing an AccountConsent identification
     * @param accountId String representing a PSU`s Account at ASPSP
     * @return Balances Report based on consentId and accountId
     */
    public ResponseObject<Xs2aBalancesReport> getBalancesReport(String consentId, String accountId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_BALANCE_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getBalancesReportValidator.validate(new CommonConsentObject(accountConsent));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getBalances(), accountId);

        if (!requestedAccountReference.isPresent()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(ErrorType.AIS_401, of(CONSENT_INVALID))
                       .build();
        }

        SpiContextData contextData = getSpiContextData(accountConsent.getPsuIdDataList());

        SpiResponse<List<SpiAccountBalance>> spiResponse = accountSpi.requestBalancesForAccount(contextData, requestedAccountReference.get(),
                                                                                                consentMapper.mapToSpiAccountConsent(accountConsent),
                                                                                                aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)))
                       .build();
        }

        if (spiResponse.getPayload() == null) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        Xs2aBalancesReport balancesReport = balanceReportMapper.mapToXs2aBalancesReport(spiResponse.getPayload(),
                                                                                        requestedAccountReference.get());

        ResponseObject<Xs2aBalancesReport> response =
            ResponseObject.<Xs2aBalancesReport>builder().body(balancesReport).build();

        writeLogAndCheckConsent(consentId, false, accountConsent, TypeAccess.BALANCE, response);

        return response;
    }

    /**
     * Read Transaction reports of a given account adressed by "account-id", depending on the steering parameter
     * "bookingStatus" together with balances.  For a given account, additional parameters are e.g. the attributes
     * "dateFrom" and "dateTo".  The ASPSP might add balance information, if transaction lists without balances are
     * not supported.     *
     *
     * @param accountId     String representing a PSU`s Account at ASPSP
     * @param withBalance   boolean representing if the responded AccountDetails should contain. Not applicable since
     *                      v1.1
     * @param acceptHeader  String representing of requested accept header
     * @param consentId     String representing an AccountConsent identification
     * @param dateFrom      ISO Date representing the value of desired start date of AccountReport
     * @param dateTo        ISO Date representing the value of desired end date of AccountReport (if omitted is set
     *                      to current date)
     * @param bookingStatus ENUM representing either one of BOOKED/PENDING or BOTH transaction statuses
     * @return TransactionsReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances
     * sections is added
     */
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(String consentId, String accountId,
                                                                                String acceptHeader,
                                                                                boolean withBalance, LocalDate dateFrom,
                                                                                LocalDate dateTo,
                                                                                BookingStatus bookingStatus) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_TRANSACTION_LIST_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getTransactionsReportValidator.validate(new CommonAccountRequestObject(accountConsent, accountId, withBalance));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getTransactions(), accountId);
        if (!requestedAccountReference.isPresent()) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(ErrorType.AIS_401, of(CONSENT_INVALID))
                       .build();
        }

        LocalDate dateToChecked = Optional.ofNullable(dateTo)
                                      .orElseGet(LocalDate::now);
        validatorService.validateAccountIdPeriod(accountId, dateFrom, dateToChecked);

        boolean isTransactionsShouldContainBalances =
            !aspspProfileService.isTransactionsWithoutBalancesSupported() || withBalance;

        SpiContextData contextData = getSpiContextData(accountConsent.getPsuIdDataList());

        SpiResponse<SpiTransactionReport> spiResponse = accountSpi.requestTransactionsForAccount(
            contextData,
            acceptHeader,
            isTransactionsShouldContainBalances, dateFrom, dateToChecked,
            bookingStatus,
            requestedAccountReference.get(),
            consentMapper.mapToSpiAccountConsent(accountConsent),
            aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            // in this particular call we use NOT_SUPPORTED to indicate that requested Content-type is not ok for us
            if (spiResponse.getResponseStatus() == SpiResponseStatus.NOT_SUPPORTED) {
                return ResponseObject.<Xs2aTransactionsReport>builder()
                           .fail(ErrorType.AIS_406, of(REQUESTED_FORMATS_INVALID))
                           .build();
            }
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS))
                       .build();
        }

        SpiTransactionReport spiTransactionReport = spiResponse.getPayload();

        if (spiTransactionReport == null) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        Optional<Xs2aAccountReport> report = transactionsToAccountReportMapper.mapToXs2aAccountReport(spiTransactionReport.getTransactions(), spiTransactionReport.getTransactionsRaw());

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(report.orElseGet(() -> new Xs2aAccountReport(Collections.emptyList(),
                                                                                         Collections.emptyList(), null)));
        transactionsReport.setAccountReference(referenceMapper.mapToXs2aAccountReference(requestedAccountReference.get()).orElse(null));
        transactionsReport.setBalances(balanceMapper.mapToXs2aBalanceList(spiTransactionReport.getBalances()));
        transactionsReport.setResponseContentType(spiTransactionReport.getResponseContentType());

        ResponseObject<Xs2aTransactionsReport> response =
            ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build();

        writeLogAndCheckConsent(consentId, withBalance, accountConsent, TypeAccess.TRANSACTION, response);

        return response;
    }

    /**
     * Gets transaction details by transaction id
     *
     * @param consentId     String representing an AccountConsent identification
     * @param accountId     String representing a PSU`s Account at
     * @param transactionId String representing the ASPSP identification of transaction
     * @return Transactions based on transaction id.
     */
    public ResponseObject<Transactions> getTransactionDetails(String consentId, String accountId,
                                                              String transactionId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_TRANSACTION_DETAILS_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            return ResponseObject.<Transactions>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getTransactionDetailsValidator.validate(new CommonConsentObject(accountConsent));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Transactions>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getTransactions(), accountId);

        if (!requestedAccountReference.isPresent()) {
            return ResponseObject.<Transactions>builder()
                       .fail(ErrorType.AIS_401, of(CONSENT_INVALID))
                       .build();
        }

        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        SpiContextData contextData = getSpiContextData(accountConsent.getPsuIdDataList());

        SpiResponse<SpiTransaction> spiResponse = accountSpi.requestTransactionForAccountByTransactionId(contextData, transactionId, requestedAccountReference.get(), consentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Transactions>builder()
                       .fail(new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)))
                       .build();
        }

        SpiTransaction payload = spiResponse.getPayload();

        if (payload == null) {
            return ResponseObject.<Transactions>builder()
                       .fail(ErrorType.AIS_404, of(RESOURCE_UNKNOWN_404))
                       .build();
        }

        Transactions transactions = spiToXs2aTransactionMapper.mapToXs2aTransaction(payload);
        checkAndExpireConsentIfOneAccessType(accountConsent, consentId);
        return ResponseObject.<Transactions>builder()
                   .body(transactions)
                   .build();
    }

    private void writeLogAndCheckConsent(String consentId, boolean withBalance, AccountConsent accountConsent, TypeAccess typeAccess, ResponseObject response) {
        aisConsentService.consentActionLog(tppService.getTppId(), consentId, createActionStatus(withBalance, typeAccess, response));
        checkAndExpireConsentIfOneAccessType(accountConsent, consentId);
    }

    private ActionStatus createActionStatus(boolean withBalance, TypeAccess access, ResponseObject response) {
        return response.hasError()
                   ? consentMapper.mapActionStatusError(response.getError().getTppMessage().getMessageErrorCode(),
                                                        withBalance, access)
                   : ActionStatus.SUCCESS;
    }

    private Optional<SpiAccountReference> findAccountReference(List<AccountReference> references, String resourceId) {
        return references.stream()
                   .filter(accountReference -> StringUtils.equals(accountReference.getResourceId(), resourceId))
                   .findFirst()
                   .map(xs2aToSpiAccountReferenceMapper::mapToSpiAccountReference);
    }

    private void checkAndExpireConsentIfOneAccessType(AccountConsent accountConsent, String encryptedConsentId) {
        if (accountConsent.isOneAccessType()) {
            aisConsentService.updateConsentStatus(encryptedConsentId, ConsentStatus.EXPIRED);
        }
    }

    private SpiContextData getSpiContextData(List<PsuIdData> psuIdDataList) {
        //TODO provide correct PSU Data to the SPI https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/701
        return spiContextDataProvider.provideWithPsuIdData(CollectionUtils.isNotEmpty(psuIdDataList)
                                                               ? psuIdDataList.get(0)
                                                               : null);
    }
}
