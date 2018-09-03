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
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
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
    private final static String TPP_ID = "This is a test TppId"; //TODO v1.1 add corresponding request header Task #149 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/149

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
        List<Xs2aAccountDetails> accountDetails = getAccountDetailsFromReferences(withBalance, allowedAccountData.getBody());
        ResponseObject<Map<String, List<Xs2aAccountDetails>>> response = accountDetails.isEmpty()
                                                                         ? ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                                                                               .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                                                                         : ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                                                                               .body(Collections.singletonMap("accountList", accountDetails)).build();
        aisConsentService.consentActionLog(TPP_ID, consentId, withBalance, TypeAccess.ACCOUNT, response);
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
        aisConsentService.consentActionLog(TPP_ID, consentId, withBalance, TypeAccess.ACCOUNT, builder.build());
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

        aisConsentService.consentActionLog(TPP_ID, consentId, false, TypeAccess.BALANCE, response);
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
                                                              BookingStatus bookingStatus, boolean withBalance, boolean deltaList) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<Xs2aAccountReport> report = getAccountReport(accountId, dateFrom, dateTo, transactionId, bookingStatus);

        ResponseObject<Xs2aAccountReport> response = isValid && report.isPresent()
                                                     ? ResponseObject.<Xs2aAccountReport>builder().body(report.get()).build()
                                                     : ResponseObject.<Xs2aAccountReport>builder()
                                                           .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, withBalance, TypeAccess.TRANSACTION, response);
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
     * @param bookingStatus ENUM representing either one of BOOKED/PENDING or BOTH transaction statuses
     * @param withBalance   boolean representing if the responded AccountDetails should contain. Not applicable since v1.1
     * @return AccountReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances sections is added
     */
    public ResponseObject<Xs2aAccountReport> getAccountReportByPeriod(String consentId, String accountId, LocalDate dateFrom,
                                                                      LocalDate dateTo, BookingStatus bookingStatus, boolean withBalance) {
        ResponseObject<Xs2aAccountAccess> allowedAccountData = consentService.getValidatedConsent(consentId);
        if (allowedAccountData.hasError()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                       .fail(allowedAccountData.getError()).build();
        }

        Xs2aAccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId, new AspspConsentData()).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (accountDetails == null) {
            return ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<Xs2aAccountReport> report = getAccountReportByPeriod(accountId, dateFrom, dateTo)
                                             .map(r -> filterByBookingStatus(r, bookingStatus));

        ResponseObject<Xs2aAccountReport> response = isValid && report.isPresent()
                                                     ? ResponseObject.<Xs2aAccountReport>builder().body(report.get()).build()
                                                     : ResponseObject.<Xs2aAccountReport>builder()
                                                           .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, withBalance, TypeAccess.TRANSACTION, response);
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
            return ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        }

        boolean isValid = consentService.isValidAccountByAccess(accountDetails.getIban(), accountDetails.getCurrency(), allowedAccountData.getBody().getTransactions());
        Optional<Xs2aAccountReport> report = getAccountReportByTransaction(transactionId, accountId);

        ResponseObject<Xs2aAccountReport> response = isValid && report.isPresent()
                                                     ? ResponseObject.<Xs2aAccountReport>builder().body(report.get()).build()
                                                     : ResponseObject.<Xs2aAccountReport>builder()
                                                           .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build();

        aisConsentService.consentActionLog(TPP_ID, consentId, false, TypeAccess.TRANSACTION, response);
        return response;
    }

    private List<Xs2aAccountDetails> getAccountDetailsFromReferences(boolean withBalance, Xs2aAccountAccess accountAccess) {
        List<AccountReference> references = withBalance
                                                ? accountAccess.getBalances()
                                                : accountAccess.getAccounts();
        List<Xs2aAccountDetails> details = getAccountDetailsFromReferences(references);
        return withBalance
                   ? details
                   : accountMapper.mapToAccountDetailsNoBalances(details);
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
                                                         BookingStatus bookingStatus) {
        return StringUtils.isNotBlank(transactionId)
                   ? getAccountReportByTransaction(transactionId, accountId)
                   : getAccountReportByPeriod(accountId, dateFrom, dateTo)
                         .map(r -> filterByBookingStatus(r, bookingStatus));
    }

    private Xs2aAccountReport filterByBookingStatus(Xs2aAccountReport report, BookingStatus bookingStatus) {
        return new Xs2aAccountReport(
            bookingStatus == BookingStatus.BOOKED || bookingStatus == BookingStatus.BOTH
                ? report.getBooked() : new Transactions[]{},
            bookingStatus == BookingStatus.PENDING || bookingStatus == BookingStatus.BOTH
                ? report.getPending() : new Transactions[]{});
    }

    private Optional<Xs2aAccountReport> getAccountReportByTransaction(String transactionId, String accountId) {
        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        Optional<SpiTransaction> transaction = accountSpi.readTransactionById(transactionId, accountId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload(); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        return accountMapper.mapToAccountReport(transaction
                                                    .map(Collections::singletonList)
                                                    .orElseGet(Collections::emptyList));
    }

    private Optional<Xs2aAccountReport> getAccountReportByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) { //TODO to be reviewed upon change to v1.1
        LocalDate dateToChecked = Optional.ofNullable(dateTo)
                                      .orElseGet(LocalDate::now);
        validatorService.validateAccountIdPeriod(accountId, dateFrom, dateToChecked);
        return accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
    }

    public Optional<Xs2aAccountDetails> getAccountDetailsByAccountReference(AccountReference reference) {
        return Optional.ofNullable(reference) // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Refactor to procedure style - we read data inside the stream here
                   .map(ref -> accountSpi.readAccountDetailsByIban(ref.getIban(), new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()) // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
                   .map(Collection::stream)
                   .flatMap(accDts -> accDts
                                          .filter(spiAcc -> spiAcc.getCurrency() == reference.getCurrency())
                                          .findFirst())
                   .map(accountMapper::mapToAccountDetails);
    }
}
