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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.*;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class AccountService {

    private final int maxNumberOfCharInTransactionJson;
    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;
    private final ValueValidatorService validatorService;
    private final JsonConverter jsonConverter;
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
                       .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_EXPIRED))).build(); //TODO review with PO and Team. Subject to Task #71
        }

        List<AccountReference> refsFromConsent = getReferencesFromAccessByWithBalance(consent, withBalance);
        Set<String> ibansFromConsent = getIbansFromConsentDependantOnWithBalanceFlag(withBalance, consent);
        List<AccountDetails> accountDetails = getAccountDetailsListAccordingToWithBalance(withBalance, refsFromConsent, ibansFromConsent);

        return accountDetails.isEmpty()
                   ? ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, CONSENT_INVALID))).build()
                   : ResponseObject.<Map<String, List<AccountDetails>>>builder()
                         .body(Collections.singletonMap("accountList", accountDetails)).build();
    }

    private List<AccountReference> getReferencesFromAccessByWithBalance(AccountConsent consent, boolean withBalance) {
        return withBalance
                   ? Arrays.asList(consent.getAccess().getBalances())
                   : Arrays.asList(consent.getAccess().getAccounts());
    }

    private Set<String> getIbansFromConsentDependantOnWithBalanceFlag(boolean withBalance, AccountConsent consent) {
        return withBalance
                   ? consentService.getIbansFromAccountReference(consent.getAccess().getBalances())
                   : consentService.getIbansFromAccountReference(consent.getAccess().getAccounts());
    }

    private List<AccountDetails> getAccountDetailsListAccordingToWithBalance(boolean withBalance, List<AccountReference> refsFromConsent, Set<String> ibansFromConsent) {
        return withBalance
                   ? getAccountDetailsFilteredByReferences(refsFromConsent, ibansFromConsent)
                   : getAccountDetailsWithoutBalances(getAccountDetailsFilteredByReferences(refsFromConsent, ibansFromConsent));
    }

    public ResponseObject<AccountDetails> getAccountDetails(String accountId, boolean withBalance, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapToAccountDetails(accountSpi.readAccountDetails(accountId));

        return accountDetails != null
                   ? ResponseObject.<AccountDetails>builder()
                         .body(accountDetails).build()
                   : ResponseObject.<AccountDetails>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
    }

    public ResponseObject<List<Balances>> getBalances(String accountId, boolean psuInvolved) {
        List<Balances> balances = accountMapper.mapToBalancesList(accountSpi.readBalances(accountId));

        return balances.isEmpty()
                   ? ResponseObject.<List<Balances>>builder()
                         .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<List<Balances>>builder().body(balances).build();
    }

    public ResponseObject<AccountReport> getAccountReport(String accountId, Date dateFrom,
                                                          Date dateTo, String transactionId,
                                                          boolean psuInvolved, String bookingStatus, boolean withBalance, boolean deltaList) {

        if (accountSpi.readAccountDetails(accountId) == null) {
            return ResponseObject.<AccountReport>builder()
                       .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        } else {
            AccountReport accountReport = getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, withBalance);
            return ResponseObject.<AccountReport>builder()
                       .body(getReportAccordingMaxSize(accountReport, accountId)).build();
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

    private List<AccountDetails> getAccountDetailsFilteredByReferences(List<AccountReference> references, Set<String> ibans) {
        return getAccountDetailsListByIbans(ibans).stream()
                   .filter(aD -> getFilteredDetailsByIbanAndCurrency(aD.getIban(), aD.getCurrency(), references))
                   .collect(Collectors.toList());
    }

    private List<AccountDetails> getAccountDetailsWithoutBalances(List<AccountDetails> accountDetailsList) {
        return accountDetailsList.stream()
                   .map(this::getAccountDetailsNoBalances)
                   .collect(Collectors.toList());
    }

    private AccountDetails getAccountDetailsNoBalances(AccountDetails details) {
        return new AccountDetails(details.getId(), details.getIban(), details.getBban(), details.getPan(),
            details.getMaskedPan(), details.getMsisdn(), details.getCurrency(), details.getName(),
            details.getAccountType(), details.getCashAccountType(), details.getBic(), null);
    }

    private AccountReport getAccountReport(String accountId, Date dateFrom, Date dateTo, String transactionId, boolean psuInvolved, boolean withBalance) {
        return StringUtils.isBlank(transactionId)
                   ? getAccountReportByPeriod(accountId, dateFrom, dateTo, psuInvolved, withBalance)
                   : getAccountReportByTransaction(accountId, transactionId, psuInvolved, withBalance);
    }

    private AccountReport getAccountReportByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved, boolean withBalance) {
        validate_accountId_period(accountId, dateFrom, dateTo);
        return readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved, withBalance);
    }

    private AccountReport getAccountReportByTransaction(String accountId, String transactionId, boolean psuInvolved, boolean withBalance) {
        validate_accountId_transactionId(accountId, transactionId);
        return readTransactionsById(accountId, transactionId, psuInvolved, withBalance);
    }

    private AccountReport getReportAccordingMaxSize(AccountReport accountReport, String accountId) {
        Optional<String> optionalAccount = jsonConverter.toJson(accountReport);
        String jsonReport = optionalAccount.orElse("");

        if (jsonReport.length() > maxNumberOfCharInTransactionJson) {
            return getAccountReportWithDownloadLink(accountId);
        }

        String urlToAccount = linkTo(AccountController.class).slash(accountId).toString();
        accountReport.getLinks().setViewAccount(urlToAccount);
        return accountReport;
    }

    private AccountReport readTransactionsByPeriod(String accountId, Date dateFrom,
                                                   Date dateTo, boolean psuInvolved, boolean withBalance) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/74
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}, new Links()));
    }

    private AccountReport readTransactionsById(String accountId, String transactionId,
                                               boolean psuInvolved, boolean withBalance) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/74
        Optional<AccountReport> result = accountMapper.mapToAccountReport(accountSpi.readTransactionsById(accountId, transactionId));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{},
            new Transactions[]{},
            new Links()
        ));
    }

    private AccountReport getAccountReportWithDownloadLink(String accountId) {
        // todo further we should implement real flow for downloading file
        String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
        Links downloadLink = new Links();
        downloadLink.setDownload(urlToDownload);
        return new AccountReport(null, null, downloadLink);
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

    private List<AccountDetails> getAccountDetailsListByIbans(Set<String> ibans) {
        return accountMapper.mapToAccountDetailsList(accountSpi.readAccountDetailsByIbans(ibans));
    }

    private boolean getFilteredDetailsByIbanAndCurrency(String iban, Currency currency, Collection<AccountReference> references) {
        return references.stream()
                   .filter(acc -> acc.getIban().equals(iban))
                   .anyMatch(acc -> currency == null || acc.getCurrency() == currency);
    }
}
