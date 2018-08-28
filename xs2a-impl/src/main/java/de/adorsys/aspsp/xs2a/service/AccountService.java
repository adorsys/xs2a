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
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
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
    private final ConsentSpi consentSpi;
    private final ConsentMapper consentMapper;
    private final static String TPP_ID = "This is a test TppId"; //TODO v1.1 add corresponding request header Task #149 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/149

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @return List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsList(String consentId, boolean withBalance) {
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

        consentSpi.consentActionLog(TPP_ID, consentId, this.createActionStatus(withBalance, TypeAccess.ACCOUNT, response));
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
    public ResponseObject<AccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<AccountDetails>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        boolean isValid = withBalance
                              ? consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances())
                              : consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getAccounts());

        ResponseObject.ResponseBuilder<AccountDetails> builder = ResponseObject.builder();
        if (isValid) {
            builder = withBalance
                          ? builder.body(accountDetails)
                          : builder.body(accountMapper.mapToAccountDetailNoBalances(accountDetails));
        } else {
            builder = builder
                          .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID)));
        }

        consentSpi.consentActionLog(TPP_ID, consentId, this.createActionStatus(withBalance, TypeAccess.ACCOUNT, builder.build()));
        return builder.build();
    }

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent Balances section
     *
     * @param consentId String representing an AccountConsent identification
     * @param accountId String representing a PSU`s Account at ASPSP
     * @return List of AccountBalances based on accountId if granted by consent
     */
    public ResponseObject<List<Balance>> getBalances(String consentId, String accountId) {
        ResponseObject<AccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<List<Balance>>builder()
                       .fail(allowedAccountData.getError()).build();
        }
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<List<Balance>>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }
        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getBalances());
        ResponseObject<List<Balance>> response = isValid
                                                     ? ResponseObject.<List<Balance>>builder().body(accountDetails.getBalances()).build()
                                                     : ResponseObject.<List<Balance>>builder()
                                                           .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();

        consentSpi.consentActionLog(TPP_ID, consentId, this.createActionStatus(false, TypeAccess.BALANCE, response));
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

        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<AccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<AccountReport> report = getAccountReport(accountId, dateFrom, dateTo, transactionId, bookingStatus);

        ResponseObject<AccountReport> response = isValid && report.isPresent()
                                                     ? ResponseObject.<AccountReport>builder().body(report.get()).build()
                                                     : ResponseObject.<AccountReport>builder()
                                                           .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();

        consentSpi.consentActionLog(TPP_ID, consentId, this.createActionStatus(withBalance, TypeAccess.TRANSACTION, response));
        return response;
    }

    private List<AccountDetails> getAccountDetailsFromReferences(boolean withBalance, AccountAccess accountAccess) {
        List<AccountReference> references = withBalance
                                                ? accountAccess.getBalances()
                                                : accountAccess.getAccounts();
        List<AccountDetails> details = getAccountDetailsFromReferences(references);
        return withBalance
                   ? details
                   : accountMapper.mapToAccountDetailsListNoBalances(details);
    }

    private List<AccountDetails> getAccountDetailsFromReferences(List<AccountReference> references) {
        return CollectionUtils.isEmpty(references)
                   ? Collections.emptyList()
                   : references.stream()
                         .map(this::getAccountDetailsByAccountReference)
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .collect(Collectors.toList());
    }


    private Optional<AccountReport> getAccountReport(String accountId, LocalDate dateFrom, LocalDate dateTo, String transactionId,
                                                     BookingStatus bookingStatus) {
        return StringUtils.isNotBlank(transactionId)
                   ? getAccountReportByTransaction(transactionId, accountId)
                   : getAccountReportByPeriod(accountId, dateFrom, dateTo)
                         .map(r -> filterByBookingStatus(r, bookingStatus));

    }

    private Optional<AccountReport> getAccountReportByTransaction(String transactionId, String accountId) {
        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        Optional<SpiTransaction> transaction = accountSpi.readTransactionById(transactionId, accountId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return accountMapper.mapToAccountReport(transaction
                                                    .map(Collections::singletonList)
                                                    .orElseGet(Collections::emptyList));
    }

    private Optional<AccountReport> getAccountReportByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) { //TODO to be reviewed upon change to v1.1
        LocalDate dateToChecked = Optional.ofNullable(dateTo)
                                      .orElseGet(LocalDate::now);
        validatorService.validateAccountIdPeriod(accountId, dateFrom, dateToChecked);
        return accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    public Optional<AccountDetails> getAccountDetailsByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference) // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Refactor to procedure style - we read data inside the stream here
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban(), new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()) // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
                   .map(Collection::stream)
                   .flatMap(accDts -> accDts
                                          .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                          .findFirst())
                   .map(accountMapper::mapToAccountDetails);
    }

    private ActionStatus createActionStatus(boolean withBalance, TypeAccess access, ResponseObject response) {
        return response.hasError()
                   ? consentMapper.mapActionStatusError(response.getError().getTppMessage().getMessageErrorCode(), withBalance, access)
                   : ActionStatus.SUCCESS;
    }

    public AccountReport filterByBookingStatus(AccountReport report, BookingStatus bookingStatus) {
        return new AccountReport(
            bookingStatus == BookingStatus.BOOKED || bookingStatus == BookingStatus.BOTH
                ? report.getBooked() : new Transactions[]{},
            bookingStatus == BookingStatus.PENDING || bookingStatus == BookingStatus.BOTH
                ? report.getPending() : new Transactions[]{});
    }
}
