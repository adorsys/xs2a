package de.adorsys.aspsp.xs2a.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.entityValidator.impl.TransactionByIdRequestValidator;
import de.adorsys.aspsp.xs2a.domain.entityValidator.impl.TransactionByPeriodRequestValidator;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
@Validated
public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private int maxNumberOfCharInTransactionJson;
    private AccountSpi accountSpi;
    private AccountMapper accountMapper;
    private ValueValidatorService validatorService;

    @Autowired
    public AccountService(AccountSpi accountSpi, int maxNumberOfCharInTransactionJson, AccountMapper accountMapper, ValueValidatorService validatorService) {
        this.accountSpi = accountSpi;
        this.maxNumberOfCharInTransactionJson = maxNumberOfCharInTransactionJson;
        this.accountMapper = accountMapper;
        this.validatorService = validatorService;
    }

    public List<AccountDetails> getAccountDetailsList(boolean withBalance, boolean psuInvolved) {

        String urlToAccount = linkTo(AccountController.class).toUriComponentsBuilder().build().getPath();

        List<AccountDetails> accountDetails =
        Optional.ofNullable(accountSpi.readAccounts(withBalance, psuInvolved))
        .map(accountDetailsList ->
             accountDetailsList
             .stream()
             .map(accountDetail -> accountMapper.mapSpiAccountDetailsToXs2aAccountDetails(accountDetail))
             .collect(Collectors.toList())
        )
        .orElse(Collections.emptyList());

        accountDetails.forEach(account -> account.setBalanceAndTransactionLinksDyDefault(urlToAccount));

        return accountDetails;
    }

    public Balances getBalances(@NotEmpty String accountId, boolean psuInvolved) {
        return accountMapper.mapSpiBalances(accountSpi.readBalances(accountId, psuInvolved));
    }

    public AccountReport getAccountReport(@NotEmpty String accountId, Date dateFrom, Date dateTo, String transactionId,
                                          boolean psuInvolved) {
        AccountReport accountReport;

        if (transactionId == null || transactionId.isEmpty()) {
            validatorService.validate(new TransactionByPeriodRequestValidator(accountId, dateFrom, dateTo));
            accountReport = readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
        } else {
            validatorService.validate(new TransactionByIdRequestValidator(accountId, transactionId));
            accountReport = readTransactionsById(accountId, transactionId, psuInvolved);
        }

        return getReportAccordingMaxSize(accountReport, accountId);
    }

    private AccountReport getReportAccordingMaxSize(AccountReport accountReport, String accountId) {

        String jsonReport = getJsonStringFromObject(accountReport);

        if (jsonReport.length() > maxNumberOfCharInTransactionJson) {
            return getAccountReportWithDownloadLink(accountId);
        }

        String urlToAccount = linkTo(AccountController.class).slash(accountId).toString();
        accountReport.get_links().setViewAccount(urlToAccount);
        return accountReport;
    }

    private String getJsonStringFromObject(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error converting object {} to json", obj);
            return "";
        }
    }

    private AccountReport readTransactionsByPeriod(String accountId, Date dateFrom,
                                                   Date dateTo, boolean psuInvolved) {
        return accountMapper.mapAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved));
    }

    private AccountReport readTransactionsById(String accountId, String transactionId,
                                               boolean psuInvolved) {
        return accountMapper.mapAccountReport(accountSpi.readTransactionsById(accountId, transactionId, psuInvolved));
    }

    public AccountReport getAccountReportWithDownloadLink(@NotNull String accountId) {
        // todo further we should implement real flow for downloading file
        String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
        Links downloadLink = new Links();
        downloadLink.setDownload(urlToDownload);
        return new AccountReport(null, null, downloadLink);
    }
}
