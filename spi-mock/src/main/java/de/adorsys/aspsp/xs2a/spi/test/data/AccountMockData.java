package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.account.AccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.Transaction;
import de.adorsys.aspsp.xs2a.spi.domain.common.Amount;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionsArt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class AccountMockData {

    private static List<SpiAccountDetails> accountDetails = new ArrayList<>();
    private static List<Transaction> transactions = new ArrayList<>();
    private static List<Amount> amounts = new ArrayList<>();
    private static List<AccountBalance> singleBalances = new ArrayList<>();
    private static List<SpiBalances> balances = new ArrayList<>();
    private static HashMap<String, SpiAccountDetails> accounts_hashmap = new HashMap<>();

    public static void createAmount(String content, Currency currency) {
        Amount amount = new Amount(currency, content);
        amounts.add(amount);
    }

    public static void createAccountsHashMap() {
        for (SpiAccountDetails accountDetails : AccountMockData.accountDetails) {
            accounts_hashmap.put(accountDetails.getId(), accountDetails);
        }
    }

    public static void createSingleBalances(Amount amount, int days, String when) {
        AccountBalance sb = new AccountBalance();
        sb.setDate(new Date());
        sb.setAmount(amount);

        Date Last_manipulation_date = createDate(days, when);
        sb.setLastActionDateTime(Last_manipulation_date);
        singleBalances.add(sb);

    }

    public static void createBalances(AccountBalance sb, TransactionsArt art) {

        SpiBalances balance = new SpiBalances();
        switch (art) {
            case booked:
                balance.setClosingBooked(sb);
                break;
            case expected:
                balance.setExpected(sb);
                break;
            case authorised:
                balance.setAuthorised(sb);
                break;
            case opening_booked:
                balance.setOpeningBooked(sb);
                break;
            case closing_booked:
                balance.setClosing_booked(sb);
                break;
            case interim_available:
                balance.setInterimAvailable(sb);
                break;
            default:
                break;
        }
        balance.setClosingBooked(sb);
        balances.add(balance);
    }

    public static void addAccount(String ID, Currency currency, SpiBalances balance, String iban, String BIC, String name, String account_type) {
        SpiAccountDetails accountDetails = createAccount(ID, currency, balance, iban, BIC, name, account_type);
        AccountMockData.accountDetails.add(accountDetails);
    }

    public static SpiAccountDetails createAccount(String id, Currency currency, SpiBalances balance, String iban, String bic, String name, String accountType) {
        return new SpiAccountDetails(
            id,
            iban,
            null,
            null,
            null,
            null,
            currency,
            name,
            accountType,
            null,
            bic,
            balance
        );
    }

    public static void createTransactions(Amount amount, String transactionId,
                                          int nrDaysToValue, int nrDaysToBooking, String when,
                                          String creditDebit, String creditorName,
                                          AccountReference creditorAccount, String ultimateCreditor,
                                          String debtorName, AccountReference debtorAccount,
                                          String ultimateDebtor, String remittanceInformation) {

        Transaction t = new Transaction(transactionId,
            "EndToEndId",
            "MandateId",
            "CreditorId",
            createDate(nrDaysToBooking, when),
            createDate(nrDaysToValue, when),
            amount,
            creditorName,
            creditorAccount,
            ultimateCreditor,
            debtorName,
            debtorAccount,
            ultimateDebtor,
            "Ref Number Merchant",
            "Ref Number Merchant",
            "PurposeCode",
            "BankTransactionCode"
        );

        transactions.add(t);
    }

    public static List<Amount> getAmounts() {
        return amounts;
    }

    public static List<SpiAccountDetails> getAccountDetails() {
        return accountDetails;
    }

    public static List<AccountBalance> getSingleBalances() {
        return singleBalances;
    }

    public static List<SpiBalances> getBalances() {
        return balances;
    }

    public static List<Transaction> getTransactions() {
        return transactions;
    }

    public static HashMap<String, SpiAccountDetails> getAccountsHashMap() {
        return accounts_hashmap;
    }

    private static Date createDate(int nrDays, String when) {

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        if (when.equals("future")) {
            calendar.add(Calendar.DAY_OF_MONTH, nrDays);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -nrDays);
        }
        return calendar.getTime();

    }
}
