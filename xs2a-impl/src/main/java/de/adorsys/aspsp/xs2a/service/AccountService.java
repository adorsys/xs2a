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

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class AccountService {
    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;
    private final ValueValidatorService validatorService;
    private final ConsentService consentService;
    private final AisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final static String TPP_ID = "This is a test TppId"; //TODO v1.1 add corresponding request header Task #149 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/149
    public final AspspProfileServiceWrapper aspspProfileService;
    private final AccountModelMapper accountModelMapper;

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @return List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Map<String, List<Xs2aAccountDetails>>> getAccountDetailsList(String consentId, boolean withBalance) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        List<Xs2aAccountDetails> accountDetails;

        if (isBankOfferedConsent(allowedAccountData.getBody())) {
            accountDetails = getAccountDetailsByConsentId(withBalance, consentId);
        } else {
            accountDetails = getAccountDetailsFromReferences(withBalance, allowedAccountData.getBody());
        }

        ResponseObject<Map<String, List<Xs2aAccountDetails>>> response = ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                                                                             .body(Collections.singletonMap("accountList", accountDetails)).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, createActionStatus(withBalance, TypeAccess.ACCOUNT, response));
        return response;
    }

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param accountId   String representing a PSU`s Account at ASPSP
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @return AccountDetails based on accountId with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Xs2aAccountDetails>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<Xs2aAccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        boolean isValid = withBalance
                              ? consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())
                              : consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getAccounts());

        ResponseObject.ResponseBuilder<Xs2aAccountDetails> builder = ResponseObject.builder();
        if (isValid) {
            builder = withBalance
                          ? builder.body(accountDetails)
                          : builder.body(accountMapper.mapToAccountDetailNoBalances(accountDetails));
        } else {
            builder = builder
                          .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID)));
        }

        aisConsentService.consentActionLog(TPP_ID, consentId, createActionStatus(withBalance, TypeAccess.ACCOUNT, builder.build()));
        return builder.build();
    }

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent Balances section
     *
     * @param consentId String representing an AccountConsent identification
     * @param accountId String representing a PSU`s Account at ASPSP
     * @return List of AccountBalances based on accountId if granted by consent
     */
    public ResponseObject<List<Xs2aBalance>> getBalances(String consentId, String accountId) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<List<Xs2aBalance>>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<List<Xs2aBalance>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances());
        ResponseObject<List<Xs2aBalance>> response = isValid
                                                         ? ResponseObject.<List<Xs2aBalance>>builder().body(accountDetails.getBalances()).build()
                                                         : ResponseObject.<List<Xs2aBalance>>builder()
                                                               .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, createActionStatus(false, TypeAccess.BALANCE, response));
        return response;
    }

    /**
     * Gets AccountReport with Booked/Pending or both transactions dependent on request.
     * Uses one of two ways to get transaction from ASPSP: 1. By transactionId, 2. By time period limited with dateFrom/dateTo variables
     * Checks if all transactions are related to accounts set in AccountConsent Transactions section
     *
     * @param consentId     String representing an AccountConsent identification
     * @param accountId     String representing a PSU`s Account at ASPSP
     * @param dateFrom      ISO Date representing the value of desired start date of AccountReport
     * @param dateTo        ISO Date representing the value of desired end date of AccountReport (if omitted is set to current date)
     * @param transactionId String representing the ASPSP identification of transaction
     * @param psuInvolved   Not applicable since v1.1
     * @param bookingStatus ENUM representing either one of BOOKED/PENDING or BOTH transaction statuses
     * @param withBalance   boolean representing if the responded AccountDetails should contain. Not applicable since v1.1
     * @param deltaList     boolean  indicating that the AISP is in favour to get all transactions after the last report access for this PSU on the addressed account
     * @return AccountReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances sections is added
     */
    public ResponseObject<Xs2aAccountReport> getAccountReport(String consentId, String accountId, LocalDate dateFrom,
                                                              LocalDate dateTo, String transactionId, boolean psuInvolved,
                                                              Xs2aBookingStatus bookingStatus, boolean withBalance, boolean deltaList) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(RESOURCE_UNKNOWN_404)).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<Xs2aAccountReport> report = getAccountReport(accountId, dateFrom, dateTo, transactionId, bookingStatus);

        ResponseObject<Xs2aAccountReport> response = isValid && report.isPresent()
                                                         ? ResponseObject.<Xs2aAccountReport>builder().body(report.get()).build()
                                                         : ResponseObject.<Xs2aAccountReport>builder()
                                                               .fail(new MessageError(CONSENT_INVALID)).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, createActionStatus(withBalance, TypeAccess.TRANSACTION, response));
        return response;
    }

    /**
     * Read Transaction reports of a given account adressed by "account-id", depending on the steering parameter  "bookingStatus" together with balances.  For a given account, additional parameters are e.g. the attributes "dateFrom" and "dateTo".  The ASPSP might add balance information, if transaction lists without balances are not supported.     *
     *
     * @param accountId     String representing a PSU`s Account at ASPSP
     * @param withBalance   boolean representing if the responded AccountDetails should contain. Not applicable since v1.1
     * @param consentId     String representing an AccountConsent identification
     * @param dateFrom      ISO Date representing the value of desired start date of AccountReport
     * @param dateTo        ISO Date representing the value of desired end date of AccountReport (if omitted is set to current date)
     * @param bookingStatus ENUM representing either one of BOOKED/PENDING or BOTH transaction statuses
     * @return TransactionsReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances sections is added
     */
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(String accountId, boolean withBalance, String consentId, LocalDate dateFrom,
                                                                                LocalDate dateTo, Xs2aBookingStatus bookingStatus) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<Xs2aTransactionsReport>builder().fail(new MessageError(RESOURCE_UNKNOWN_404)).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<Xs2aAccountReport> report = getAccountReportByPeriod(accountId, dateFrom, dateTo)
                                                 .map(r -> filterByBookingStatus(r, bookingStatus));

        if (!(isValid && report.isPresent())) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                       .fail(new MessageError(CONSENT_INVALID)).build();
        }

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(report.get());
        transactionsReport.setAccountReference(accountModelMapper.mapToAccountReference(accountDetails));

        if (!aspspProfileService.isTransactionsWithoutBalancesSupported()
                && consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())) {
            transactionsReport.setBalances(accountDetails.getBalances());
        }
        ResponseObject<Xs2aTransactionsReport> response = ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, createActionStatus(withBalance, TypeAccess.TRANSACTION, response));
        return response;
    }

    /**
     * Gets AccountReport with Booked/Pending or both transactions dependent on request.
     * Uses one of two ways to get transaction from ASPSP: 1. By transactionId, 2. By time period limited with dateFrom/dateTo variables
     * Checks if all transactions are related to accounts set in AccountConsent Transactions section
     *
     * @param consentId     String representing an AccountConsent identification
     * @param accountId     String representing a PSU`s Account at
     * @param transactionId String representing the ASPSP identification of transaction
     * @return AccountReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances sections is added
     */
    public ResponseObject<Xs2aAccountReport> getAccountReportByTransactionId(String consentId, String accountId,
                                                                             String transactionId) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(RESOURCE_UNKNOWN_404)).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<Xs2aAccountReport> report = getAccountReportByTransaction(transactionId, accountId);

        ResponseObject<Xs2aAccountReport> response = isValid && report.isPresent()
                                                         ? ResponseObject.<Xs2aAccountReport>builder().body(report.get()).build()
                                                         : ResponseObject.<Xs2aAccountReport>builder()
                                                               .fail(new MessageError(CONSENT_INVALID)).build();
        if (!report.isPresent()) {
            response = ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(RESOURCE_UNKNOWN_403)).build();
        }

        aisConsentService.consentActionLog(TPP_ID, consentId, createActionStatus(false, TypeAccess.TRANSACTION, response));
        return response;
    }

    Optional<Xs2aAccountDetails> getAccountDetailsByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference) // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Refactor to procedure style - we read data inside the stream here
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban(), new AspspConsentData()).getPayload()) // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
                   .map(Collection::stream)
                   .flatMap(accDts -> accDts
                                          .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                          .findFirst())
                   .map(accountMapper::mapToAccountDetails);
    }

    private List<Xs2aAccountDetails> getAccountDetailsFromReferences(boolean withBalance, Xs2aAccountAccess accountAccess) {
        List<AccountReference> references = withBalance
                                                ? accountAccess.getBalances()
                                                : accountAccess.getAccounts();
        List<Xs2aAccountDetails> details = getAccountDetailsFromReferences(references);
        return filterAccountDetailsByWithBalance(withBalance, details);
    }

    private List<Xs2aAccountDetails> getAccountDetailsByConsentId(boolean withBalance, String consentId) {
        SpiAccountConsent spiAccountConsent = aisConsentService.getAccountConsentById(consentId);
        List<SpiAccountDetails> spiAccountDetails = accountSpi.readAccountsByPsuId(spiAccountConsent.getPsuId(), new AspspConsentData()).getPayload();
        List<Xs2aAccountDetails> details = spiAccountDetails.stream()
                                               .map(accountMapper::mapToAccountDetails)
                                               .collect(Collectors.toList());

        return filterAccountDetailsByWithBalance(withBalance, details);
    }

    private List<Xs2aAccountDetails> filterAccountDetailsByWithBalance(boolean withBalance, List<Xs2aAccountDetails> details) {
        return withBalance
                   ? details
                   : accountMapper.mapToAccountDetailsListNoBalances(details);
    }

    private List<Xs2aAccountDetails> getAccountDetailsFromReferences(List<AccountReference> references) {
        return CollectionUtils.isEmpty(references)
                   ? Collections.emptyList()
                   : references.stream()
                         .map(this::getAccountDetailsByAccountReference)
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .collect(Collectors.toList());
    }

    private Optional<Xs2aAccountReport> getAccountReport(String accountId, LocalDate dateFrom, LocalDate dateTo, String transactionId,
                                                         Xs2aBookingStatus bookingStatus) {
        return StringUtils.isNotBlank(transactionId)
                   ? getAccountReportByTransaction(transactionId, accountId)
                   : getAccountReportByPeriod(accountId, dateFrom, dateTo)
                         .map(r -> filterByBookingStatus(r, bookingStatus));
    }

    private Optional<Xs2aAccountReport> getAccountReportByTransaction(String transactionId, String accountId) {
        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        Optional<SpiTransaction> transaction = accountSpi.readTransactionById(transactionId, accountId, new AspspConsentData()).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return accountMapper.mapToAccountReport(transaction
                                                    .map(Collections::singletonList)
                                                    .orElseGet(Collections::emptyList));
    }

    public Optional<Xs2aAccountReport> getAccountReportByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) { //TODO to be reviewed upon change to v1.1
        LocalDate dateToChecked = Optional.ofNullable(dateTo)
                                      .orElseGet(LocalDate::now);
        validatorService.validateAccountIdPeriod(accountId, dateFrom, dateToChecked);
        return accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateToChecked, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    private ActionStatus createActionStatus(boolean withBalance, TypeAccess access, ResponseObject response) {
        return response.hasError()
                   ? consentMapper.mapActionStatusError(response.getError().getTppMessage().getMessageErrorCode(), withBalance, access)
                   : ActionStatus.SUCCESS;
    }

    private Xs2aAccountReport filterByBookingStatus(Xs2aAccountReport report, Xs2aBookingStatus bookingStatus) {
        return new Xs2aAccountReport(
            EnumSet.of(Xs2aBookingStatus.BOOKED, Xs2aBookingStatus.BOTH).contains(bookingStatus)
                ? report.getBooked() : new Transactions[]{},
            EnumSet.of(Xs2aBookingStatus.PENDING, Xs2aBookingStatus.BOTH).contains(bookingStatus)
                ? report.getPending() : new Transactions[]{});
    }

    private boolean isBankOfferedConsent(Xs2aAccountAccess accountAccess) {
        return CollectionUtils.isEmpty(accountAccess.getBalances())
                   && CollectionUtils.isEmpty(accountAccess.getTransactions())
                   && CollectionUtils.isEmpty(accountAccess.getAccounts());
    }
}
