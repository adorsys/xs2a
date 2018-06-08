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
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBookingStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
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
        AccountConsent consent = Optional.ofNullable(consentService.getAccountConsentById(consentId))
                                     .map(ResponseObject::getBody)
                                     .orElse(null);

        List<AccountDetails> detailsFromConsent = consent != null
                                                      ? getAccountDetailsFromReferences(consent.getAccess().getAccounts())
                                                      : Collections.emptyList();
        Map<String, Set<AccessAccountInfo>> allowedAccountData = consentService.checkValidityByConsent(consentId, detailsFromConsent, TypeAccess.ACCOUNT, withBalance);

        List<AccountDetails> accountDetails = getAccountDetailsVerifiedByAccess(withBalance, detailsFromConsent, allowedAccountData);

        return accountDetails.isEmpty()
                   ? ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .body(Collections.singletonMap("accountList", accountDetails)).build();
    }

    public ResponseObject<AccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        Map<String, Set<AccessAccountInfo>> allowedAccountData = consentService.checkValidityByConsent(consentId, Collections.singletonList(accountDetails), TypeAccess.ACCOUNT, withBalance);

        AccountDetails details = null;
        if (withBalance && consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), TypeAccess.BALANCE, allowedAccountData)) {
            details = accountDetails;
        } else if (consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), TypeAccess.ACCOUNT, allowedAccountData)) {
            details = getAccountDetailsNoBalances(accountDetails);
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
        Map<String, Set<AccessAccountInfo>> allowedAccountData = consentService.checkValidityByConsent(consentId, Collections.singletonList(accountDetails), TypeAccess.BALANCE, false);

        return MapUtils.isNotEmpty(allowedAccountData)
                   && consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), TypeAccess.BALANCE, allowedAccountData)
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
        Map<String, Set<AccessAccountInfo>> allowedAccountData = consentService.checkValidityByConsent(consentId, Collections.singletonList(accountDetails), TypeAccess.TRANSACTION, withBalance);

        AccountReport accountReport = allowedAccountData != null && !allowedAccountData.isEmpty()
                                          ? getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, withBalance, bookingStatus, allowedAccountData)
                                          : null;
        return accountReport != null
                   ? ResponseObject.<AccountReport>builder()
                         .body(accountReport).build()
                   : ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();
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

    private List<AccountDetails> getAccountDetailsVerifiedByAccess(boolean withBalance, List<AccountDetails> detailsFromConsent, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        return detailsFromConsent.stream().map(ad ->
                                                   withBalance
                                                       && consentService.isValidAccountByAccess(ad.getIban(), ad.getCurrency(), TypeAccess.BALANCE, allowedAccountData)
                                                       ? ad
                                                       : getAccountDetailsNoBalances(ad)
        )
                   .collect(Collectors.toList());
    }

    private List<AccountDetails> getAccountDetailsFromReferences(List<AccountReference> references) {
        return CollectionUtils.isEmpty(references)
                   ? Collections.emptyList()
                   : references.stream()
                         .map(this::getAccountDetailsByAccountReference)
                         .filter(Optional::isPresent)
                         .collect(Collectors.mapping(Optional::get,Collectors.toList()));
    }

    private AccountDetails getAccountDetailsNoBalances(AccountDetails details) {
        return new AccountDetails(details.getId(), details.getIban(), details.getBban(), details.getPan(),
            details.getMaskedPan(), details.getMsisdn(), details.getCurrency(), details.getName(),
            details.getAccountType(), details.getCashAccountType(), details.getBic(), null);
    }

    private AccountReport getAccountReport(String accountId, Date dateFrom, Date dateTo, String transactionId, boolean psuInvolved, boolean withBalance, BookingStatus bookingStatus, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        Date dateToChecked = dateTo == null ? new Date() : dateTo; //TODO Migrate Date to Instant. Task #126 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/126
        return StringUtils.isBlank(transactionId)
                   ? getAccountReportByPeriod(accountId, dateFrom, dateToChecked, psuInvolved, withBalance, bookingStatus, allowedAccountData)
                   : getAccountReportByTransaction(accountId, transactionId, psuInvolved, withBalance, allowedAccountData);
    }

    private AccountReport getAccountReportByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved, boolean withBalance, BookingStatus bookingStatus, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        validate_accountId_period(accountId, dateFrom, dateTo);
        return getAllowedTransactionsByAccess(readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved, withBalance, bookingStatus), allowedAccountData);
    }

    private AccountReport getAccountReportByTransaction(String accountId, String transactionId, boolean psuInvolved, boolean withBalance, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        validate_accountId_transactionId(accountId, transactionId);
        return getAllowedTransactionsByAccess(readTransactionsById(accountId, transactionId, psuInvolved, withBalance), allowedAccountData);
    }

    private AccountReport getAllowedTransactionsByAccess(AccountReport accountReport, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        Transactions[] booked = filterTransactions(accountReport.getBooked(), allowedAccountData);
        Transactions[] pending = filterTransactions(accountReport.getPending(), allowedAccountData);
        return ArrayUtils.isEmpty(booked) && ArrayUtils.isEmpty(pending)
                   ? null
                   : new AccountReport(booked, pending);
    }

    private Transactions[] filterTransactions(Transactions[] transactions, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        return Arrays.stream(transactions)
                   .filter(t -> isAllowedTransaction(t, allowedAccountData))
                   .toArray(Transactions[]::new);
    }

    private boolean isAllowedTransaction(Transactions transaction, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        return allowedAccountData.containsKey(transaction.getCreditorAccount().getIban())
                   && allowedAccountData.get(transaction.getCreditorAccount().getIban()).contains(new AccessAccountInfo(transaction.getAmount().getCurrency().getCurrencyCode(), TypeAccess.TRANSACTION))
                   || allowedAccountData.containsKey(transaction.getDebtorAccount().getIban())
                          && allowedAccountData.get(transaction.getDebtorAccount().getIban()).contains(new AccessAccountInfo(transaction.getAmount().getCurrency().getCurrencyCode(), TypeAccess.TRANSACTION));
    }

    private AccountReport readTransactionsByPeriod(String accountId, Date dateFrom,
                                                   Date dateTo, boolean psuInvolved, boolean withBalance, BookingStatus bookingStatus) { //NOPMD TODO to be reviewed upon change to v1.1
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, SpiBookingStatus.valueOf(bookingStatus.name())));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}));
    }

    private AccountReport readTransactionsById(String accountId, String transactionId, boolean psuInvolved, boolean withBalance) { //NOPMD TODO to be reviewed upon change to v1.1
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
