package de.adorsys.aspsp.xs2a.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.spi.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReport;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
@Validated
public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    
    private int maxLengthTransactionJson;
    private AccountSpi accountSpi;
    
    @Autowired
    public AccountService(AccountSpi accountSpi, int maxLengthTransactionJson) {
        this.accountSpi = accountSpi;
        this.maxLengthTransactionJson = maxLengthTransactionJson;
    }
    
    public List<AccountDetails> getAccountDetailsList(boolean withBalance, boolean psuInvolved) {
        
        String urlToAccount = linkTo(AccountController.class).toUriComponentsBuilder().build().getPath();
        
        List<AccountDetails> accountDetails = accountSpi.readAccounts(withBalance, psuInvolved);
        
        accountDetails.forEach(account -> account.setBalanceAndTransactionLinksDyDefault(urlToAccount));
        
        return accountDetails;
    }
    
    public Balances getBalances(@NotEmpty String accountId, boolean psuInvolved) {
        return accountSpi.readBalances(accountId, psuInvolved);
    }
    
    public AccountReport getAccountReport(@NotEmpty String accountId, @NotNull Date dateFrom, @NotNull Date dateTo, String transactionId,
                                          boolean psuInvolved) {
        AccountReport accountReport;
        
        if (transactionId == null || transactionId.isEmpty()) {
            accountReport = readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
        } else {
            accountReport = readTransactionsById(accountId, transactionId, psuInvolved);
        }
        
        return getReportAccordingMaxSize(accountReport, accountId);
    }
    
    private AccountReport getReportAccordingMaxSize(AccountReport accountReport, String accountId) {
        
        String jsonReport = getJsonStringFromObject(accountReport);
        
        if (jsonReport.length() > maxLengthTransactionJson) {
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
                                                   @NotNull Date dateTo, boolean psuInvolved) {
        return accountSpi.readTransactionsByPeriod(accountId, dateFrom, dateTo, psuInvolved);
    }
    
    private AccountReport readTransactionsById(@NotEmpty String accountId, @NotEmpty String transactionId,
                                               boolean psuInvolved) {
        return accountSpi.readTransactionsById(accountId, transactionId, psuInvolved);
    }
    
    public AccountReport getAccountReportWithDownloadLink(@NotEmpty String accountId) {
        // todo further we should implement real flow for downloading file
        String urlToDownload = linkTo(AccountController.class).slash(accountId).slash("transactions/download").toString();
        Links downloadLink = new Links();
        downloadLink.setDownload(urlToDownload);
        return new AccountReport(null, null, downloadLink);
    }
}
