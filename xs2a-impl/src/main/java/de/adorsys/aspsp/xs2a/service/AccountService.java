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

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBookingStatus;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_404;
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

    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsList(String consentId, boolean withBalance, boolean psuInvolved) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Map<String, List<AccountDetails>>>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        List<AccountDetails> accountDetails = getAccountDetailsFromReferences(withBalance, allowedAccountData.getBody());

        return accountDetails.isEmpty()
                   ? ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .body(Collections.singletonMap("accountList", accountDetails)).build();
    }

    private List<AccountDetails> getAccountDetailsFromReferences(boolean withBalance, AccountAccess accountAccess) {
        List<AccountReference> references = withBalance
                                                ? accountAccess.getBalances()
                                                : accountAccess.getAccounts();
        List<AccountDetails> details = getAccountDetailsFromReferences(references);
        return withBalance
                   ? details
                   : getAccountDetailsNoBalances(details);
    }

    public ResponseObject<AccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountDetails details = null;
        if (withBalance && consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())) {
            details = accountDetails;
        } else if (consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getAccounts())) {
            details = getAccountDetailNoBalances(accountDetails);
        }

        return details == null
                   ? ResponseObject.<AccountDetails>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<AccountDetails>builder()
                         .body(details).build();
    }

    public ResponseObject<List<Balances>> getBalances(String consentId, String accountId, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        return consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())
                   ? ResponseObject.<List<Balances>>builder().body(accountDetails.getBalances()).build()
                   : ResponseObject.<List<Balances>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();
    }

    public ResponseObject<AccountReport> getAccountReport(String consentId, String accountId, Date dateFrom,
                                                          Date dateTo, String transactionId,
                                                          boolean psuInvolved, BookingStatus bookingStatus, boolean withBalance, boolean deltaList) {

        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountReport accountReport = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions())
                                          ? getAccountReport(accountDetails, dateFrom, dateTo, transactionId, bookingStatus, allowedAccountData.getBody().getTransactions())
                                          : null;

        return accountReport == null
                   ? ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<AccountReport>builder().body(accountReport).build();
    }

    public List<Balances> getAccountBalancesByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban()))
                   .map(Collection::stream)
                   .flatMap(accDets -> accDets
                                           .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                           .findFirst())
                   .map(spiAcc -> accountMapper.mapToBalancesList(spiAcc.getBalances()))
                   .orElse(Collections.emptyList());
    }

    public boolean isAccountExists(AccountReference reference) {
        return getAccountDetailsByAccountReference(reference).isPresent();
    }

    private List<AccountDetails> getAccountDetailsFromReferences(List<AccountReference> references) {
        return CollectionUtils.isEmpty(references)
                   ? Collections.emptyList()
                   : references.stream()
                         .map(this::getAccountDetailsByAccountReference)
                         .filter(Optional::isPresent)
                         .collect(Collectors.mapping(Optional::get, Collectors.toList()));
    }

    private List<AccountDetails> getAccountDetailsNoBalances(List<AccountDetails> details) {
        return details.stream()
                   .map(this::getAccountDetailNoBalances)
                   .collect(Collectors.toList());
    }

    private AccountDetails getAccountDetailNoBalances(AccountDetails detail) {
        return new AccountDetails(detail.getId(), detail.getIban(), detail.getBban(), detail.getPan(),
            detail.getMaskedPan(), detail.getMsisdn(), detail.getCurrency(), detail.getName(),
            detail.getAccountType(), detail.getCashAccountType(), detail.getBic(), null);
    }

    private AccountReport getAccountReport(AccountDetails details, Date dateFrom, Date dateTo, String transactionId, BookingStatus bookingStatus, List<AccountReference> allowedAccountData) {
        Date dateToChecked = dateTo == null ? new Date() : dateTo; //TODO Migrate Date to Instant. Task #126 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/126
        return StringUtils.isBlank(transactionId)
                   ? getAccountReportByPeriod(details, dateFrom, dateToChecked, bookingStatus, allowedAccountData)
                   : getAccountReportByTransaction(details, transactionId, allowedAccountData);
    }

    private AccountReport getAccountReportByPeriod(AccountDetails details, Date dateFrom, Date dateTo, BookingStatus bookingStatus, List<AccountReference> allowedAccountData) {
        validate_accountId_period(details.getIban(), dateFrom, dateTo);
        return getAllowedTransactionsByAccess(readTransactionsByPeriod(details, dateFrom, dateTo, bookingStatus), allowedAccountData);
    }

    private AccountReport getAccountReportByTransaction(AccountDetails details, String transactionId, List<AccountReference> allowedAccountData) {
        validate_accountId_transactionId(details.getIban(), transactionId);
        return getAllowedTransactionsByAccess(readTransactionsById(transactionId), allowedAccountData);
    }

    private AccountReport getAllowedTransactionsByAccess(AccountReport accountReport, List<AccountReference> allowedAccountData) {
        Transactions[] booked = filterTransactions(accountReport.getBooked(), allowedAccountData);
        Transactions[] pending = filterTransactions(accountReport.getPending(), allowedAccountData);
        return new AccountReport(booked, pending);
    }

    private Transactions[] filterTransactions(Transactions[] transactions, List<AccountReference> allowedAccountData) {
        return Optional.ofNullable(transactions)
                   .map(tr -> Arrays.stream(tr)
                                  .filter(t -> isAllowedTransaction(t, allowedAccountData))
                                  .toArray(Transactions[]::new))
                   .orElse(new Transactions[]{});
    }

    private boolean isAllowedTransaction(Transactions transaction, List<AccountReference> allowedAccountData) {
        return consentService.isValidAccountByAccess(transaction.getCreditorAccount().getIban(), transaction.getCreditorAccount().getCurrency(), allowedAccountData)
                   || consentService.isValidAccountByAccess(transaction.getDebtorAccount().getIban(), transaction.getDebtorAccount().getCurrency(), allowedAccountData);
    }

    private AccountReport readTransactionsByPeriod(AccountDetails details, Date dateFrom,
                                                   Date dateTo, BookingStatus bookingStatus) { //NOPMD TODO to be reviewed upon change to v1.1
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(details.getIban(), details.getCurrency(), dateFrom, dateTo, SpiBookingStatus.valueOf(bookingStatus.name())));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}));
    }

    private AccountReport readTransactionsById(String transactionId) { //NOPMD TODO to be reviewed upon change to v1.1
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsById(transactionId));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{},
            new Transactions[]{}
        ));
    }

    // Validation
    private void validate_accountId_period(String accountId, Date dateFrom, Date dateTo) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setDateFrom(dateFrom);
        fieldValidator.setDateTo(dateTo);

        validatorService.validate(fieldValidator, ValidationGroup.AccountIdAndPeriodIsValid.class);
    }

    private void validate_accountId_transactionId(String accountId, String transactionId) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setTransactionId(transactionId);

        validatorService.validate(fieldValidator, ValidationGroup.AccountIdAndTransactionIdIsValid.class);
    }

    private Optional<AccountDetails> getAccountDetailsByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban()))
                   .map(Collection::stream)
                   .flatMap(accDets -> accDets
                                           .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                           .findFirst())
                   .map(accountMapper::mapToAccountDetails);
    }
}
