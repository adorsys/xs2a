package de.adorsys.aspsp.xs2a.spi.config;

import de.adorsys.aspsp.xs2a.spi.domain.*;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;
import de.adorsys.aspsp.xs2a.spi.utils.DateUtil;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Currency;

@Configuration
public class SpiMockConfig {
    
    public SpiMockConfig() {
        fillAccounts();
        fillConsents();
    }
    
    private void fillAccounts() {
        Currency euro = Currency.getInstance("EUR");
        
        AccountMockData.createAmount("-1000.34", euro);
        AccountMockData.createAmount("2000.56", euro);
        AccountMockData.createAmount("3000.45", euro);
        AccountMockData.createAmount("4000.43", euro);
        AccountMockData.createAmount("-500.14", euro);
        
        int i = 0;
        
        for (Amount amount : AccountMockData.getAmounts()) {
            AccountMockData.createSingleBalances(amount, i, "future");
            i++;
        }
        i = 0;
        
        for (SingleBalance singleBalance : AccountMockData.getSingleBalances()) {
            switch (i) {
                case 0:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.booked);
                    break;
                case 1:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.opening_booked);
                    break;
                case 2:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.closing_booked);
                    break;
                case 3:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.expected);
                    break;
                case 4:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.interim_available);
                    break;
            }
            i++;
        }
        
        AccountMockData.addAccount("11111-999999999", euro, AccountMockData.getBalances().get(0), "DE371234599999", "GENODEF1N02", "Müller", "SCT");
        AccountMockData.addAccount("22222-999999999", euro, AccountMockData.getBalances().get(1), "DE371234599998", "GENODEF1N02", "Albert", "SCT");
        AccountMockData.addAccount("33333-999999999", euro, AccountMockData.getBalances().get(2), "DE371234599997", "GENODEF1N02", "Schmidt", "SCT");
        AccountMockData.addAccount("44444-999999999", euro, AccountMockData.getBalances().get(3), "DE371234599996", "GENODEF1N02", "Telekom", "SCT");
        AccountMockData.addAccount("55555-999999999", euro, AccountMockData.getBalances().get(4), "DE371234599995", "GENODEF1N02", "Bauer", "SCT");
        
        AccountMockData.createAccountsHashMap();
        
        AccountMockData.createTransactions(
        AccountMockData.getAmounts().get(0), "12345",
        7, 4, "future",
        "debit", AccountMockData.getAccountDetails().get(3).getName(), convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(3)), "",
        "", null, "", "Example for remittance information");
        
        AccountMockData.createTransactions(
        AccountMockData.getAmounts().get(4), "123456",
        4, 6, "future",
        "debit", AccountMockData.getAccountDetails().get(4).getName(), convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(4)), "",
        "", null, "", "Another Example for remittance information");
        
        AccountMockData.createTransactions(
        AccountMockData.getAmounts().get(1), "123457",
        6, 10, "past",
        "credit", "", null, "",
        "AccountMockData.getAccounts().get(2)", convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(2)), "", "remittance information");
        
        AccountMockData.createTransactions(
        AccountMockData.getAmounts().get(2), "1234578",
        17, 20, "past",
        "credit", "", null, "",
        "AccountMockData.getAccounts().get(2)", convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(2)), "", "remittance information");
        AccountMockData.createTransactions(
        AccountMockData.getAmounts().get(3), "1234578",
        5, 3, "future",
        "credit", "", null, "",
        "AccountMockData.getAccounts().get(1)", convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(1)), "", "remittance information");
        
    }
    
    private void fillConsents() {
        ConsentMockData.createAccountConsent(getAicRequest_1(), true, false);
        ConsentMockData.createAccountConsent(getAicRequest_2(), true, false);
    }
    
    private AccountReference convertAccountDetailsToAccountReference(AccountDetails accountDetails) {
        AccountReference accountReference = new AccountReference();
        
        accountReference.setAccountId(accountDetails.getId());
        accountReference.setIban(accountDetails.getIban());
        accountReference.setBban(accountDetails.getBban());
        accountReference.setMaskedPan(accountDetails.getMaskedPan());
        accountReference.setMsisdn(accountDetails.getMsisdn());
        accountReference.setCurrency(accountDetails.getCurrency());
        
        return accountReference;
    }
    
    private CreateConsentReq getAicRequest_1() {
        AccountReference iban1 = new AccountReference();
        iban1.setIban("DE8710010010653456712");
        
        AccountReference iban2 = new AccountReference();
        iban2.setIban("DE8710010010653456723");
        iban2.setCurrency(Currency.getInstance("USD"));
        
        AccountReference iban3 = new AccountReference();
        iban3.setIban("DE8710010010653456734");
        
        AccountReference iban4 = new AccountReference();
        iban4.setIban("DE870010010165456745");
        
        AccountReference maskedPan = new AccountReference();
        maskedPan.setMaskedPan("873456xxxxxx1245");
        
        AccountReference[] balances = (AccountReference[]) Arrays.asList(iban1, iban2, iban3).toArray();
        AccountReference[] transactions = (AccountReference[]) Arrays.asList(iban4, maskedPan).toArray();
        
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setBalances(balances);
        accountAccess.setTransactions(transactions);
        
        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(DateUtil.getDateFromDateStringNoTimeZone("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);
        return aicRequestObj;
    }
    
    private CreateConsentReq getAicRequest_2() {
        AccountReference iban1 = new AccountReference();
        iban1.setIban("DE5410010010165456787");
        
        AccountReference iban2 = new AccountReference();
        iban2.setIban("DE650010010123456743");
        iban2.setCurrency(Currency.getInstance("USD"));
        
        AccountReference iban3 = new AccountReference();
        iban3.setIban("DE430010010123456534");
        
        AccountReference iban4 = new AccountReference();
        iban4.setIban("DE9780010010123452356");
        
        AccountReference maskedPan = new AccountReference();
        maskedPan.setMaskedPan("553456xxxxxx12397");
        
        AccountReference[] balances = (AccountReference[]) Arrays.asList(iban1, iban2, iban3).toArray();
        AccountReference[] transactions = (AccountReference[]) Arrays.asList(iban4, maskedPan).toArray();
        
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setBalances(balances);
        accountAccess.setTransactions(transactions);
        
        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(DateUtil.getDateFromDateStringNoTimeZone("2017-04-25"));
        aicRequestObj.setFrequencyPerDay(4);
        return aicRequestObj;
    }
    
}
