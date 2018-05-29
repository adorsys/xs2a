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
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.*;
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
        if (consent == null) {
            return ResponseObject.<Map<String, List<AccountDetails>>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_UNKNOWN_403))).build();
        }

        if (psuInvolved && consent.getConsentStatus() != ConsentStatus.VALID) {
            return ResponseObject.<Map<String, List<AccountDetails>>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_EXPIRED))).build();
        }

        List<AccountDetails> accountDetails = getAccountDetailsWithBalanceByReferences(
            getAccountDetailsFromReferences(consent.getAccess().getAccounts()),
            withBalance ? consent.getAccess().getBalances() : new AccountReference[]{});

        return accountDetails.isEmpty()
                   ? ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .body(Collections.singletonMap("accountList", accountDetails)).build();
    }

    public ResponseObject<AccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance, boolean psuInvolved) {
        AccountConsent consent = Optional.ofNullable(consentService.getAccountConsentById(consentId))
                                     .map(ResponseObject::getBody)
                                     .orElse(null);
        if (consent == null) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_UNKNOWN_403))).build();
        }

        if (psuInvolved && consent.getConsentStatus() != ConsentStatus.VALID) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_EXPIRED))).build();
        }
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));

        return accountDetails != null && accountReferenceContainsAccount(consent.getAccess().getAccounts(), accountDetails)
                   ? withBalance && accountReferenceContainsAccount(consent.getAccess().getBalances(), accountDetails)
                         ? ResponseObject.<AccountDetails>builder().body(accountDetails).build()
                         : ResponseObject.<AccountDetails>builder()
                               .body(getAccountDetailsNoBalances(accountDetails)).build()
                   : ResponseObject.<AccountDetails>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
    }

    public ResponseObject<List<Balances>> getBalances(String consentId, String accountId, boolean psuInvolved) {
        AccountConsent consent = Optional.ofNullable(consentService.getAccountConsentById(consentId))
                                     .map(ResponseObject::getBody)
                                     .orElse(null);
        if (consent == null) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_UNKNOWN_403))).build();
        }

        if (psuInvolved && consent.getConsentStatus() != ConsentStatus.VALID) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_EXPIRED))).build();
        }
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        return accountDetails != null && accountReferenceContainsAccount(consent.getAccess().getBalances(), accountDetails)
                   ? ResponseObject.<List<Balances>>builder().body(accountDetails.getBalances()).build()
                   : ResponseObject.<List<Balances>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
    }

    public ResponseObject<AccountReport> getAccountReport(String consentId, String accountId, Date dateFrom,
                                                          Date dateTo, String transactionId,
                                                          boolean psuInvolved, String bookingStatus, boolean withBalance, boolean deltaList) {
        AccountConsent consent = Optional.ofNullable(consentService.getAccountConsentById(consentId))
                                     .map(ResponseObject::getBody)
                                     .orElse(null);
        if (consent == null) {
            return ResponseObject.<AccountReport>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_UNKNOWN_403))).build();
        }
        AccountDetails details = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (details == null || !accountReferenceContainsAccount(consent.getAccess().getTransactions(), details)) {
            return ResponseObject.<AccountReport>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        } else {
            AccountReport accountReport = getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, withBalance, bookingStatus);
            return ResponseObject.<AccountReport>builder()
                       .body(accountReport).build();
        }
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

    private boolean accountReferenceContainsAccount(AccountReference[] references, AccountDetails details) {
        return Optional.ofNullable(references)
                   .map(Arrays::stream)
                   .map(rStream -> rStream.anyMatch(
                       ref -> ref.getIban().equals(details.getIban())
                                  && ref.getCurrency().equals(details.getCurrency())))
                   .orElse(false);
    }

    private List<AccountDetails> getAccountDetailsWithBalanceByReferences(List<AccountDetails> details, AccountReference[] references) {
        return details.stream()
                   .map(det -> accountReferenceContainsAccount(references, det)
                                   ? det
                                   : getAccountDetailsNoBalances(det))
                   .collect(Collectors.toList());
    }

    private List<AccountDetails> getAccountDetailsFromReferences(AccountReference[] references) {
        return Optional.ofNullable(references)
                   .map(Arrays::stream)
                   .map(refStream -> refStream.map(this::getAccountDetailsByAccountReference)
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    private AccountDetails getAccountDetailsNoBalances(AccountDetails details) {
        return new AccountDetails(details.getId(), details.getIban(), details.getBban(), details.getPan(),
            details.getMaskedPan(), details.getMsisdn(), details.getCurrency(), details.getName(),
            details.getAccountType(), details.getCashAccountType(), details.getBic(), null);
    }

    private AccountReport getAccountReport(String accountId, Date dateFrom, Date dateTo, String transactionId, boolean psuInvolved, boolean withBalance, String bookingStatus) {
        Date dateToChecked = dateTo == null ? new Date() : dateTo;
        return StringUtils.isBlank(transactionId)
                   ? getAccountReportByPeriod(accountId, dateFrom, dateToChecked, psuInvolved, withBalance, bookingStatus)
                   : getAccountReportByTransaction(accountId, transactionId, psuInvolved, withBalance);
    }

    private AccountReport getAccountReportByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved, boolean withBalance, String bookingStatus) {
        validate_accountId_period(accountId, dateFrom, dateTo);
        return readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved, withBalance, bookingStatus);
    }

    private AccountReport getAccountReportByTransaction(String accountId, String transactionId, boolean psuInvolved, boolean withBalance) {
        validate_accountId_transactionId(accountId, transactionId);
        return readTransactionsById(accountId, transactionId, psuInvolved, withBalance);
    }

    private AccountReport readTransactionsByPeriod(String accountId, Date dateFrom,
                                                   Date dateTo, boolean psuInvolved, boolean withBalance, String bookingStatus) { //NOPMD TODO to be reviewed upon change to v1.1
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, bookingStatus));

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
