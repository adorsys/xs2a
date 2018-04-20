package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.validator.ValidationGroup;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.RESOURCE_UNKNOWN_404;
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

    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsList(boolean withBalance, boolean psuInvolved) {
        List<AccountDetails> accountDetailsList = accountMapper.mapFromSpiAccountDetailsList(accountSpi.readAccounts(withBalance, psuInvolved));
        Map<String, List<AccountDetails>> accountDetailsMap = new HashMap<>();
        accountDetailsMap.put("accountList", accountDetailsList);

        return ResponseObject.builder()
               .body(accountDetailsMap).build();
    }

    public ResponseObject<List<Balances>> getBalances(String accountId, boolean psuInvolved) {
        List<SpiBalances> spiBalances = accountSpi.readBalances(accountId, psuInvolved);

        return Optional.ofNullable(spiBalances)
                       .map(sb -> ResponseObject.builder().body(accountMapper.mapFromSpiBalancesList(sb)).build())
                       .orElse(ResponseObject.builder().fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404)
                                                                            .text("Wrong account ID"))).build());
    }

    public ResponseObject<AccountReport> getAccountReport(String accountId, Date dateFrom,
                                                          Date dateTo, String transactionId,
                                                          boolean psuInvolved, String bookingStatus, boolean withBalance, boolean deltaList) {

        if (accountSpi.readAccountDetails(accountId, false, false) == null) {
            return ResponseObject.builder()
                   .fail(new MessageError(new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_404))).build();
        } else {

            AccountReport accountReport = getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, withBalance);
            return ResponseObject.builder()
                           .body(getReportAccordingMaxSize(accountReport, accountId)).build();
        }
    }

    private AccountReport getAccountReport(String accountId, Date dateFrom, Date dateTo, String transactionId, boolean psuInvolved, boolean withBalance) {
        return StringUtils.isEmpty(transactionId)
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
        accountReport.get_links().setViewAccount(urlToAccount);
        return accountReport;
    }

    private AccountReport readTransactionsByPeriod(String accountId, Date dateFrom,
                                                   Date dateTo, boolean psuInvolved, boolean withBalance) { //NOPMD TODO review and check PMD assertion
        Optional<AccountReport> result = accountMapper.mapFromSpiAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}, new Links()));
    }

    private AccountReport readTransactionsById(String accountId, String transactionId,
                                               boolean psuInvolved, boolean withBalance) { //NOPMD TODO review and check PMD assertion
        Optional<AccountReport> result = accountMapper.mapFromSpiAccountReport(accountSpi.readTransactionsById(accountId, transactionId, psuInvolved));

        return result.orElseGet(() -> new AccountReport(new Transactions[]{},
                                                        new Transactions[]{},
                                                        new Links()
        ));

    }

    public AccountReport getAccountReportWithDownloadLink(String accountId) {
        // todo further we should implement real flow for downloading file
        String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
        Links downloadLink = new Links();
        downloadLink.setDownload(urlToDownload);
        return new AccountReport(null, null, downloadLink);
    }

    public ResponseObject<AccountDetails> getAccountDetails(String accountId, boolean withBalance, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapFromSpiAccountDetails(accountSpi.readAccountDetails(accountId, withBalance, psuInvolved));

        return ResponseObject.builder()
               .body(accountDetails).build();
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
}
