package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionsArt;

import java.util.*;

public class AccountMockData {

    private static List<SpiAccountDetails> accountDetails = new ArrayList<>();
    private static List<SpiTransaction> spiTransactions = new ArrayList<>();
    private static List<SpiAmount> spiAmounts = new ArrayList<>();
    private static List<SpiAccountBalance> singleBalances = new ArrayList<>();
    private static List<SpiBalances> balances = new ArrayList<>();
    private static HashMap<String, SpiAccountDetails> accounts_hashmap = new HashMap<>();

    public static void createAmount(String content, Currency currency) {
        SpiAmount spiAmount = new SpiAmount(currency, content);
        spiAmounts.add(spiAmount);
    }

    public static void createAccountsHashMap() {
        for (SpiAccountDetails accountDetails : AccountMockData.accountDetails) {
            accounts_hashmap.put(accountDetails.getId(), accountDetails);
        }
    }

    public static void createSingleBalances(SpiAmount spiAmount, int days, String when) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setDate(new Date());
        sb.setSpiAmount(spiAmount);

        Date Last_manipulation_date = createDate(days, when);
        sb.setLastActionDateTime(Last_manipulation_date);
        singleBalances.add(sb);

    }

    public static void createBalances(SpiAccountBalance sb, TransactionsArt art) {

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
            case interim_available:
                balance.setInterimAvailable(sb);
                break;
            default:
                break;
        }
        balance.setClosingBooked(sb);
        balances.add(balance);
    }

    public static void addAccount(String ID, Currency currency, List<SpiBalances> balance, String iban, String BIC, String name, String account_type) {
        SpiAccountDetails accountDetails = createAccount(ID, currency, balance, iban, BIC, name, account_type);
        AccountMockData.accountDetails.add(accountDetails);
    }

    public static SpiAccountDetails createAccount(String id, Currency currency, List<SpiBalances> balance, String iban, String bic, String name, String accountType) {
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
            balance.toArray(new SpiBalances[balance.size()])
        );
    }

    public static void createTransactions(SpiAmount spiAmount, String transactionId,
                                          int nrDaysToValue, int nrDaysToBooking, String when,
                                          String creditDebit, String creditorName,
                                          SpiAccountReference creditorAccount, String ultimateCreditor,
                                          String debtorName, SpiAccountReference debtorAccount,
                                          String ultimateDebtor, String remittanceInformation) {

        SpiTransaction t = new SpiTransaction(transactionId,
            "EndToEndId",
            "MandateId",
            "CreditorId",
            createDate(nrDaysToBooking, when),
            createDate(nrDaysToValue, when),
        spiAmount,
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

        spiTransactions.add(t);
    }

    public static List<SpiAmount> getSpiAmounts() {
        return spiAmounts;
    }

    public static List<SpiAccountDetails> getAccountDetails() {
        return accountDetails;
    }

    public static List<SpiAccountBalance> getSingleBalances() {
        return singleBalances;
    }

    public static List<SpiBalances> getBalances() {
        return balances;
    }

    public static List<SpiTransaction> getSpiTransactions() {
        return spiTransactions;
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
