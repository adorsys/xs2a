package de.adorsys.aspsp.xs2a.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private int maxNumberOfCharInTransactionJson;
    private AccountSpi accountSpi;
    private AccountMapper accountMapper;

    @Autowired
    public AccountService(AccountSpi accountSpi, int maxNumberOfCharInTransactionJson, AccountMapper accountMapper) {
        this.accountSpi = accountSpi;
        this.maxNumberOfCharInTransactionJson = maxNumberOfCharInTransactionJson;
        this.accountMapper = accountMapper;
    }

    public ResponseObject<Map<String, List<AccountDetails>>> getAccountDetailsList(boolean withBalance, boolean psuInvolved) {
        List<AccountDetails> accountDetailsList = accountMapper.mapFromSpiAccountDetailsList(accountSpi.readAccounts(withBalance, psuInvolved));
        Map<String, List<AccountDetails>> accountDetailsMap = new HashMap<>();
        accountDetailsMap.put("accountList", accountDetailsList);

        return accountDetailsList != null
               ? new ResponseObject<>(accountDetailsMap)
               : new ResponseObject(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404)));
    }

    public ResponseObject<List<Balances>> getBalancesList(@NotEmpty String accountId, boolean psuInvolved) {
        List<Balances> result = accountMapper.mapFromSpiBalancesList(accountSpi.readBalances(accountId, psuInvolved));
        return result != null
               ? new ResponseObject<>(result)
               : new ResponseObject<>(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404)));
    }

    public ResponseObject<AccountReport> getAccountReport(@NotEmpty String accountId, @NotNull Date dateFrom,
                                                          @NotNull Date dateTo, String transactionId,
                                                          boolean psuInvolved, String bookingStatus, boolean withBalance, boolean deltaList) {
        AccountReport accountReport = StringUtils.isEmpty(transactionId)
                                      ? readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved, withBalance)
                                      : readTransactionsById(accountId, transactionId, psuInvolved, withBalance);

        return accountReport == null
               ? new ResponseObject<>(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404)))
               : new ResponseObject<>(getReportAccordingMaxSize(accountReport, accountId));
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

    private AccountReport readTransactionsByPeriod(@NotEmpty String accountId, @NotNull Date dateFrom,
                                                   @NotNull Date dateTo, boolean psuInvolved, boolean withBalance) {
        return accountMapper.mapFromSpiAccountReport(accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved));
    }

    private AccountReport readTransactionsById(@NotEmpty String accountId, @NotEmpty String transactionId,
                                               boolean psuInvolved, boolean withBalance) {
        return accountMapper.mapFromSpiAccountReport(accountSpi.readTransactionsById(accountId, transactionId, psuInvolved));
    }

    public AccountReport getAccountReportWithDownloadLink(@NotEmpty String accountId) {
        // todo further we should implement real flow for downloading file
        String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
        Links downloadLink = new Links();
        downloadLink.setDownload(urlToDownload);
        return new AccountReport(null, null, downloadLink);
    }

    public ResponseObject<AccountDetails> getAccountDetails(@NotEmpty String accountId, boolean withBalance, boolean psuInvolved) {
        AccountDetails accountDetails = accountMapper.mapFromSpiAccountDetails(accountSpi.readAccountDetails(accountId, withBalance, psuInvolved));

        return accountDetails == null
               ? new ResponseObject<>(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404)))
               : new ResponseObject<>(accountDetails);
    }
}
