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

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.Xs2aBookingStatus;
import de.adorsys.aspsp.xs2a.domain.account.*;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.*;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_404;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

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
    private final SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;

    private final ValueValidatorService validatorService;
    private final ConsentService consentService;
    private final AisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final AisConsentDataService aisConsentDataService;

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @return List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Map<String, List<Xs2aAccountDetails>>> getAccountList(String consentId, boolean withBalance) {
        ResponseObject<AccountConsent> accountConsentResponse = consentService.getValidatedConsent(consentId, withBalance);
        if (accountConsentResponse.hasError()) {
            return ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                .fail(accountConsentResponse.getError())
                .build();
        }

        AccountConsent accountConsent = accountConsentResponse.getBody();

        SpiResponse<List<SpiAccountDetails>> spiResponse = accountSpi.requestAccountList(withBalance,
            consentMapper.mapToSpiAccountConsent(accountConsent),
            aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Map<String, List<Xs2aAccountDetails>>>builder()
                .fail(new MessageError(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus())))
                .build();
        }

        List<Xs2aAccountDetails> accountDetails = accountDetailsMapper.mapToXs2aAccountDetailsList(spiResponse.getPayload());
        updateResourceId(accountConsent.getAccess(), accountDetails);

        aisConsentDataService.updateAccountAccess(consentId, consentMapper.mapToAisAccountAccessInfo(accountConsent.getAccess()));

        ResponseObject<Map<String, List<Xs2aAccountDetails>>> response = ResponseObject.<Map<String,
            List<Xs2aAccountDetails>>>builder()
            .body(Collections.singletonMap("accountList", accountDetails))
            .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId, createActionStatus(withBalance,
            TypeAccess.ACCOUNT, response));
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
     * @return AccountDetails based on accountId with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountDetails> getAccountDetails(String consentId, String accountId, boolean withBalance) {
        ResponseObject<AccountConsent> accountConsentResponse = consentService.getValidatedConsent(consentId, withBalance);
        if (accountConsentResponse.hasError()) {
            return ResponseObject.<Xs2aAccountDetails>builder()
                .fail(accountConsentResponse.getError()).build();
        }

        AccountConsent accountConsent = accountConsentResponse.getBody();

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getAccounts(), accountId);

        if (isNotPermittedAccountReference(requestedAccountReference, accountConsent.getAccess(), withBalance)) {
            return ResponseObject.<Xs2aAccountDetails>builder()
                .fail(new MessageError(RESOURCE_UNKNOWN_404))
                .build();
        }

        SpiResponse<SpiAccountDetails> spiResponse = accountSpi.requestAccountDetailForAccount(withBalance, requestedAccountReference.get(),
            consentMapper.mapToSpiAccountConsent(accountConsent),
            aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aAccountDetails>builder()
                .fail(new MessageError(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus())))
                .build();
        }

        SpiAccountDetails spiAccountDetails = spiResponse.getPayload();

        //noinspection ConstantConditions - although @NotNull on paylod inside SpiResponse is set, but it couldn't be guaranteed by SPI implementation
        if (spiAccountDetails == null) {
            return ResponseObject.<Xs2aAccountDetails>builder()
                .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404)))
                .build();
        }

        Xs2aAccountDetails accountDetails = accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails);

        ResponseObject<Xs2aAccountDetails> response =
            ResponseObject.<Xs2aAccountDetails>builder().body(accountDetails).build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId, createActionStatus(withBalance,
            TypeAccess.ACCOUNT, response));
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
        ResponseObject<AccountConsent> accountConsentResponse = consentService.getValidatedConsent(consentId);

        if (accountConsentResponse.hasError()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                .fail(accountConsentResponse.getError())
                .build();
        }

        AccountConsent accountConsent = accountConsentResponse.getBody();

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getBalances(), accountId);

        if (!requestedAccountReference.isPresent()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                .fail(new MessageError(RESOURCE_UNKNOWN_404))
                .build();
        }

        SpiResponse<List<SpiAccountBalance>> spiResponse = accountSpi.requestBalancesForAccount(requestedAccountReference.get(),
            consentMapper.mapToSpiAccountConsent(accountConsent),
            aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                .fail(new MessageError(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus())))
                .build();
        }

        //noinspection ConstantConditions - although @NotNull on paylod inside SpiResponse is set, but it couldn't beguaranteed by SPI implementation
        if (spiResponse.getPayload() == null) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                .fail(new MessageError(RESOURCE_UNKNOWN_404))
                .build();
        }

        Xs2aBalancesReport balancesReport = balanceReportMapper.mapToXs2aBalancesReport(spiResponse.getPayload(),
            requestedAccountReference.get());

        ResponseObject<Xs2aBalancesReport> response =
            ResponseObject.<Xs2aBalancesReport>builder().body(balancesReport).build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId, createActionStatus(false,
            TypeAccess.BALANCE, response));
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
     * @param consentId     String representing an AccountConsent identification
     * @param dateFrom      ISO Date representing the value of desired start date of AccountReport
     * @param dateTo        ISO Date representing the value of desired end date of AccountReport (if omitted is set
     *                      to current date)
     * @param bookingStatus ENUM representing either one of BOOKED/PENDING or BOTH transaction statuses
     * @return TransactionsReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances
     * sections is added
     */
    public ResponseObject<Xs2aTransactionsReport> getTransactionsReportByPeriod(String consentId, String accountId,
                                                                                boolean withBalance, LocalDate dateFrom,
                                                                                LocalDate dateTo,
                                                                                Xs2aBookingStatus bookingStatus) {
        ResponseObject<AccountConsent> accountConsentResponse = consentService.getValidatedConsent(consentId,
            withBalance);
        if (accountConsentResponse.hasError()) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                .fail(accountConsentResponse.getError()).build();
        }

        AccountConsent accountConsent = accountConsentResponse.getBody();

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getTransactions(), accountId);

        if (isNotPermittedAccountReference(requestedAccountReference, accountConsent.getAccess(), withBalance)) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                .fail(new MessageError(RESOURCE_UNKNOWN_404))
                .build();
        }

        LocalDate dateToChecked = Optional.ofNullable(dateTo)
            .orElseGet(LocalDate::now);
        validatorService.validateAccountIdPeriod(accountId, dateFrom, dateToChecked);

        boolean isTransactionsShouldContainBalances =
            !aspspProfileService.isTransactionsWithoutBalancesSupported() || withBalance;

        SpiResponse<SpiTransactionReport> spiResponse = accountSpi.requestTransactionsForAccount(
            isTransactionsShouldContainBalances, dateFrom, dateToChecked,
            requestedAccountReference.get(),
            consentMapper.mapToSpiAccountConsent(accountConsent),
            aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                .fail(new MessageError(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus())))
                .build();
        }

        SpiTransactionReport spiTransactionReport = spiResponse.getPayload();

        //noinspection ConstantConditions - although @NotNull on paylod inside SpiResponse is set, but it couldn't be guaranteed by SPI implementation
        if (spiTransactionReport == null) {
            return ResponseObject.<Xs2aTransactionsReport>builder()
                .fail(new MessageError(RESOURCE_UNKNOWN_404))
                .build();
        }

        Optional<Xs2aAccountReport> report =
            transactionsToAccountReportMapper.mapToXs2aAccountReport(spiTransactionReport.getTransactions())
                .map(r -> filterByBookingStatus(r, bookingStatus));

        Xs2aTransactionsReport transactionsReport = new Xs2aTransactionsReport();
        transactionsReport.setAccountReport(report.orElseGet(() -> new Xs2aAccountReport(Collections.emptyList(),
            Collections.emptyList())));
        transactionsReport.setXs2aAccountReference(referenceMapper.mapToXs2aAccountReference(requestedAccountReference.get()).orElse(null));
        transactionsReport.setBalances(balanceMapper.mapToXs2aBalanceList(spiTransactionReport.getBalances()));

        ResponseObject<Xs2aTransactionsReport> response =
            ResponseObject.<Xs2aTransactionsReport>builder().body(transactionsReport).build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId, createActionStatus(withBalance,
            TypeAccess.TRANSACTION, response));
        return response;
    }

    /**
     * Gets AccountReport with Booked/Pending or both transactions dependent on request.
     * Uses one of two ways to get transaction from ASPSP: 1. By transactionId, 2. By time period limited with
     * dateFrom/dateTo variables
     * Checks if all transactions are related to accounts set in AccountConsent Transactions section
     *
     * @param consentId     String representing an AccountConsent identification
     * @param accountId     String representing a PSU`s Account at
     * @param transactionId String representing the ASPSP identification of transaction
     * @return AccountReport filled with appropriate transaction arrays Booked and Pending. For v1.1 balances
     * sections is added
     */
    public ResponseObject<Xs2aAccountReport> getAccountReportByTransactionId(String consentId, String accountId,
                                                                             String transactionId) {
        ResponseObject<AccountConsent> accountConsentResponse = consentService.getValidatedConsent(consentId);
        if (accountConsentResponse.hasError()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                .fail(accountConsentResponse.getError()).build();
        }

        AccountConsent accountConsent = accountConsentResponse.getBody();

        Optional<SpiAccountReference> requestedAccountReference = findAccountReference(accountConsent.getAccess().getTransactions(), accountId);

        if (!requestedAccountReference.isPresent()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                .fail(new MessageError(RESOURCE_UNKNOWN_404))
                .build();
        }

        validatorService.validateAccountIdTransactionId(accountId, transactionId);

        SpiResponse<SpiTransaction> spiResponse = accountSpi.requestTransactionForAccountByTransactionId(transactionId, requestedAccountReference.get(), consentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));

        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<Xs2aAccountReport>builder()
                .fail(new MessageError(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus())))
                .build();
        }

        SpiTransaction payload = spiResponse.getPayload();

        //noinspection ConstantConditions - although @NotNull on paylod inside SpiResponse is set, but it couldn't be guaranteed by SPI implementation
        if (payload == null) {
            return ResponseObject.<Xs2aAccountReport>builder().fail(new MessageError(RESOURCE_UNKNOWN_404)).build();
        }

        List<SpiTransaction> transactions = Collections.singletonList(payload);
        Optional<Xs2aAccountReport> report = transactionsToAccountReportMapper.mapToXs2aAccountReport(transactions);

        ResponseObject<Xs2aAccountReport> response =
            ResponseObject.<Xs2aAccountReport>builder().body(report.get()).build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId, createActionStatus(false,
            TypeAccess.TRANSACTION, response));
        return response;
    }

    private ActionStatus createActionStatus(boolean withBalance, TypeAccess access, ResponseObject response) {
        return response.hasError()
            ? consentMapper.mapActionStatusError(response.getError().getTppMessage().getMessageErrorCode(),
            withBalance, access)
            : ActionStatus.SUCCESS;
    }

    private Xs2aAccountReport filterByBookingStatus(Xs2aAccountReport report, Xs2aBookingStatus bookingStatus) {
        return new Xs2aAccountReport(
            EnumSet.of(Xs2aBookingStatus.BOOKED, Xs2aBookingStatus.BOTH).contains(bookingStatus)
                ? report.getBooked() : Collections.emptyList(),
            EnumSet.of(Xs2aBookingStatus.PENDING, Xs2aBookingStatus.BOTH).contains(bookingStatus)
                ? report.getPending() : Collections.emptyList()
        );
    }

    private boolean isNotPermittedAccountReference(Optional<SpiAccountReference> requestedAccountReference, Xs2aAccountAccess consentAccountAccess, boolean withBalance) {
        return requestedAccountReference.map(accountReference -> {
            List<Xs2aAccountReference> accountReferences;
            if (withBalance) {
                accountReferences = consentAccountAccess.getBalances();
            } else {
                accountReferences = consentAccountAccess.getAccounts();
            }
            return !consentService.isValidAccountByAccess(accountReference.getResourceId(), accountReferences);
        }).orElse(true);
    }

    private void updateResourceId(Xs2aAccountAccess accountAccess, List<Xs2aAccountDetails> accountDetailsList) {
        for (Xs2aAccountDetails accountDetails: accountDetailsList) {
            if (CollectionUtils.isNotEmpty(accountAccess.getAccounts())) {
                updateResourceId(accountAccess.getAccounts(), accountDetails, accountDetails.getResourceId());
            }
            if (CollectionUtils.isNotEmpty(accountAccess.getBalances())) {
                updateResourceId(accountAccess.getBalances(), accountDetails, accountDetails.getResourceId());
            }
            if (CollectionUtils.isNotEmpty(accountAccess.getTransactions())) {
                updateResourceId(accountAccess.getTransactions(), accountDetails,
                    accountDetails.getResourceId());
            }
        }
    }

    private void updateResourceId(List<Xs2aAccountReference> consentAccountReferences, Xs2aAccountDetails spiAccountReference, String resourceId) {
        consentAccountReferences.stream()
            .filter(xs2aAccountReference -> isSameAccountReference(xs2aAccountReference, spiAccountReference))
            .findFirst()
            .ifPresent(xs2aAccountReference -> xs2aAccountReference.setResourceId(resourceId));
    }

    private boolean isSameAccountReference(Xs2aAccountReference accountReference, Xs2aAccountDetails accountDetails) {
        boolean same = Optional.ofNullable(accountReference.getIban())
            .map(iban -> StringUtils.equals(iban, accountDetails.getIban()))
            .orElse(false);

        if (!same) {
            same = Optional.ofNullable(accountReference.getBban())
                .map(bban -> StringUtils.equals(bban, accountDetails.getBban()))
                .orElse(false);
        }
        if (!same) {
            same = Optional.ofNullable(accountReference.getMaskedPan())
                .map(maskedpan -> StringUtils.equals(maskedpan, accountDetails.getMaskedPan()))
                .orElse(false);
        }
        if (!same) {
            same = Optional.ofNullable(accountReference.getMsisdn())
                .map(msisdn -> StringUtils.equals(msisdn, accountDetails.getMsisdn()))
                .orElse(false);
        }
        if (!same) {
            same = Optional.ofNullable(accountReference.getPan())
                .map(pan -> StringUtils.equals(pan, accountDetails.getPan()))
                .orElse(false);
        }
        return same;
    }

    private Optional<SpiAccountReference> findAccountReference(List<Xs2aAccountReference> references, String resourceId) {
        return references.stream()
            .filter(xs2aAccountReference -> StringUtils.equals(xs2aAccountReference.getResourceId(), resourceId))
            .findFirst()
            .map(xs2aToSpiAccountReferenceMapper::mapToSpiAccountReference);
    }
}
