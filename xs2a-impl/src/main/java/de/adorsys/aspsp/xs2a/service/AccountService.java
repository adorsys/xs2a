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

import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
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
    private final AisConsentService aisConsentService;
    private final String tppId = "This is a test TppId"; //TODO v1.1 add corresponding request header Task #149 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/149

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @param psuInvolved Not applicable since v1.1
     * @return List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsList(String consentId, boolean withBalance, boolean psuInvolved) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Map<String, List<AccountDetails>>>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        List<AccountDetails> accountDetails = getAccountDetailsFromReferences(withBalance, allowedAccountData.getBody());
        ResponseObject<Map<String, List<AccountDetails>>> response = accountDetails.isEmpty()
                                                                         ? ResponseObject.<Map<String, List<AccountDetails>>>builder()
                                                                               .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                                                                         : ResponseObject.<Map<String, List<AccountDetails>>>builder()
                                                                               .body(Collections.singletonMap("accountList", accountDetails)).build();
        aisConsentService.consentActionLog(tppId, consentId, withBalance, TypeAccess.ACCOUNT, response);
        return response;
    }

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param accountId   String representing a PSU`s Account at ASPSP
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @param psuInvolved Not applicable since v1.1
     * @return AccountDetails based on accountId with Balances if requested and granted by consent
     */
    public ResponseObject<AccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance, boolean psuInvolved) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        boolean isValid = withBalance
                              ? consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())
                              : consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getAccounts());

        if (isValid) {
            return withBalance
                       ? ResponseObject.<AccountDetails>builder().body(accountDetails).build()
                       : ResponseObject.<AccountDetails>builder().body(getAccountDetailNoBalances(accountDetails)).build();
        }
        ResponseObject<AccountDetails> response = ResponseObject.<AccountDetails>builder()
                                                      .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();
        aisConsentService.consentActionLog(tppId, consentId, withBalance, TypeAccess.ACCOUNT, response);
        return response;
    }

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent Balances section
     *
     * @param consentId   String representing an AccountConsent identification
     * @param accountId   String representing a PSU`s Account at ASPSP
     * @param psuInvolved Not applicable since v1.1
     * @return List of AccountBalances based on accountId if granted by consent
     */
    public ResponseObject<List<Balances>> getBalances(String consentId, String accountId, boolean psuInvolved) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<List<Balances>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances());
        if (isValid) {
            return ResponseObject.<List<Balances>>builder().body(accountDetails.getBalances()).build();
        }
        ResponseObject<List<Balances>> response = ResponseObject.<List<Balances>>builder()
                                                      .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();
        aisConsentService.consentActionLog(tppId, consentId, false, TypeAccess.BALANCE, response);
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
    public ResponseObject<AccountReport> getAccountReport(String consentId, String accountId, LocalDate dateFrom,
                                                          LocalDate dateTo, String transactionId, boolean psuInvolved,
                                                          BookingStatus bookingStatus, boolean withBalance, boolean deltaList) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));
        if (accountDetails == null) {
            return ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<AccountReport> report = getAccountReport(accountId, dateFrom, dateTo, transactionId, bookingStatus);

        if (isValid && report.isPresent()) {
            return ResponseObject.<AccountReport>builder()
                       .body(report.get())
                       .build();
        }
        ResponseObject<AccountReport> response = ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();
        aisConsentService.consentActionLog(tppId, consentId, withBalance, TypeAccess.TRANSACTION, response);
        return response;
    }

    List<Balances> getAccountBalancesByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(this::getAccountDetailsByAccountReference)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .map(AccountDetails::getBalances)
                   .orElse(Collections.emptyList());
    }

    boolean isAccountExists(AccountReference reference) {
        return getAccountDetailsByAccountReference(reference).isPresent();
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

    private Optional<AccountReport> getAccountReport(String accountId, LocalDate dateFrom, LocalDate dateTo, String transactionId,
                                                     BookingStatus bookingStatus) {
        LocalDate dateToChecked = Optional.ofNullable(dateTo)
                                      .orElse(LocalDate.now());

        Optional<AccountReport> report;
        if (StringUtils.isBlank(transactionId)) {
            report = getAccountReportByPeriod(accountId, dateFrom, dateToChecked)
                         .map(r -> filterByBookingStatus(r, bookingStatus));
        } else {
            report = getAccountReportByTransaction(transactionId, accountId);
        }

        return report;
    }

    private AccountReport filterByBookingStatus(AccountReport report, BookingStatus bookingStatus) {
        return new AccountReport(
            bookingStatus == BookingStatus.BOOKED || bookingStatus == BookingStatus.BOTH
                ? report.getBooked() : new Transactions[]{},
            bookingStatus == BookingStatus.PENDING || bookingStatus == BookingStatus.BOTH
                ? report.getPending() : new Transactions[]{});
    }

    private Optional<AccountReport> getAccountReportByTransaction(String transactionId, String accountId) {
        validateAccountIdTransactionId(accountId, transactionId);

        Optional<SpiTransaction> transaction = accountSpi.readTransactionsById(transactionId, accountId);
        return accountMapper.mapToAccountReport(transaction
                                                    .map(Collections::singletonList)
                                                    .orElse(Collections.emptyList()));
    }

    private Optional<AccountReport> getAccountReportByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) { //TODO to be reviewed upon change to v1.1
        validateAccountIdPeriod(accountId, dateFrom, dateTo);
        return accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo));
    }

    private Optional<AccountDetails> getAccountDetailsByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban()))
                   .map(Collection::stream)
                   .flatMap(accDts -> accDts
                                          .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                          .findFirst())
                   .map(accountMapper::mapToAccountDetails);
    }

    // Validation
    private void validateAccountIdPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setDateFrom(dateFrom);
        fieldValidator.setDateTo(dateTo);

        validatorService.validate(fieldValidator, ValidationGroup.AccountIdAndPeriodIsValid.class);
    }

    private void validateAccountIdTransactionId(String accountId, String transactionId) {
        ValidationGroup fieldValidator = new ValidationGroup();
        fieldValidator.setAccountId(accountId);
        fieldValidator.setTransactionId(transactionId);

        validatorService.validate(fieldValidator, ValidationGroup.AccountIdAndTransactionIdIsValid.class);
    }
}
