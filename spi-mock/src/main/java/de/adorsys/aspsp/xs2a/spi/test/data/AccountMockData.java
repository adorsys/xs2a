package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.*;
import de.adorsys.aspsp.xs2a.spi.domain.codes.BankTransactionCode;
import de.adorsys.aspsp.xs2a.spi.domain.codes.PurposeCode;

import java.util.*;

public class AccountMockData {

    private static List<AccountDetails> accountDetails = new ArrayList<>();
    private static List<Transactions> transactions = new ArrayList<>();
    private static List<Amount> amounts = new ArrayList<>();
    private static List<SingleBalance> singleBalances = new ArrayList<>();
    private static List<Balances> balances = new ArrayList<>();
    private static HashMap<String, AccountDetails> accounts_hashmap = new HashMap<String, AccountDetails>();

    public static void createAmount(String content, Currency currency) {
        Amount amount = new Amount();
        amount.setContent(content);
        amount.setCurrency(currency);
        amounts.add(amount);
    }

    public static void createAccountsHashMap() {
        for (AccountDetails accountDetails : AccountMockData.accountDetails) {
            accounts_hashmap.put(accountDetails.getId(), accountDetails);
        }
    }

    public static void createSingleBalances(Amount amount, int days, String when) {
        SingleBalance sb = new SingleBalance();
        sb.setDate(new Date());
        sb.setAmount(amount);

        Date Last_manipulation_date = createDate(days, when);
        sb.setLastActionDateTime(Last_manipulation_date);
        singleBalances.add(sb);

    }

    public static void createBalances(SingleBalance sb, TransactionsArt art) {

        Balances balance = new Balances();
        switch (art) {
            case booked:
                balance.setBooked(sb);
                break;
            case expected:
                balance.setExpected(sb);
                break;
            case authorised:
                balance.setAuthorised(sb);
                break;
            case opening_booked:
                balance.setOpening_booked(sb);
                break;
            case closing_booked:
                balance.setClosing_booked(sb);
                break;
            case interim_available:
                balance.setInterim_available(sb);
                break;
            default:
                break;
        }
        balance.setBooked(sb);
        balances.add(balance);
    }

    public static void addAccount(String ID, Currency currency, Balances balance, String iban, String BIC, String name, String account_type) {
        AccountDetails accountDetails = createAccount(ID, currency, balance, iban, BIC, name, account_type);
        AccountMockData.accountDetails.add(accountDetails);
    }

    public static AccountDetails createAccount(String ID, Currency currency, Balances balance, String iban, String BIC, String name, String account_type) {
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setIban(iban);
        accountDetails.setId(ID);
        accountDetails.setBic(BIC);
        accountDetails.setCurrency(currency);
        accountDetails.setAccountType(account_type);
        accountDetails.setName(name);
        accountDetails.setBalances(balance);
        accountDetails.set_links(createEmptyLinks());
        return accountDetails;
    }

    public static Links createEmptyLinks() {
        Links links = new Links();

        links.setRedirect(null);
        links.setOAuth(null);
        links.setUpdatePsuAuthentication(null);
        links.setUpdatePsuIdentification(null);
        links.setUpdateProprietaryData(null);
        links.setSelectAuthenticationMethod(null);
        links.setSelf(null);
        links.setStatus(null);
        links.setViewBalances(null);
        links.setViewAccount(null);
        links.setViewTransactions(null);
        links.setFirst(null);
        links.setNext(null);
        links.setPrevious(null);
        links.setLast(null);
        links.setDownload(null);

        return links;
    }

    public static void createTransactions(Amount amount, String transactionId,
                                          int nrDaysToValue, int nrDaysToBooking, String when,
                                          String creditDebit, String creditorName,
                                          AccountReference creditorAccount, String ultimateCreditor,
                                          String debtorName, AccountReference debtorAccount,
                                          String ultimateDebtor, String remittanceInformation) {

        Transactions t = new Transactions();
        t.setTransactionId(transactionId);
        t.setEndToEndId("EndToEndId");
        t.setMandateId("MandateId");
        t.setCreditorId("CreditorId");
        t.setBookingDate(createDate(nrDaysToBooking, when));
        t.setValueDate(createDate(nrDaysToValue, when));
        t.setAmount(amount);
        t.setCreditorName(creditorName);
        t.setCreditorAccount(creditorAccount);
        t.setUltimateCreditor(ultimateCreditor);
        t.setDebtorName(debtorName);
        t.setDebtorAccount(debtorAccount);
        t.setUltimateDebtor(ultimateDebtor);
        t.setRemittanceInformationStructured("Ref Number Merchant");
        t.setRemittanceInformationUnstructured("Ref Number Merchant");
        t.setPurposeCode(new PurposeCode("PurposeCode"));
        t.setBankTransactionCodeCode(new BankTransactionCode("BankTransactionCode"));

        transactions.add(t);
    }

    public static List<Amount> getAmounts() {
        return amounts;
    }

    public static List<AccountDetails> getAccountDetails() {
        return accountDetails;
    }

    public static List<SingleBalance> getSingleBalances() {
        return singleBalances;
    }

    public static List<Balances> getBalances() {
        return balances;
    }

    public static List<Transactions> getTransactions() {
        return transactions;
    }

    public static HashMap<String, AccountDetails> getAccountsHashMap() {
        return accounts_hashmap;
    }

    public static Date createDate(int nrDays, String when) {

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
