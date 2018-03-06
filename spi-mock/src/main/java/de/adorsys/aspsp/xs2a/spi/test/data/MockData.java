package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.*;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionsArt;

import java.util.*;

public class MockData {

    private static List<Account> accounts = new ArrayList<>();
    private static List<Transactions> transactions = new ArrayList<>();
    private static List<Amount> amounts = new ArrayList<>();
    private static List<SingleBalance> singleBalances = new ArrayList<>();
    private static List<Balances> balances = new ArrayList<>();
    private static HashMap<String, Account> accounts_hashmap = new HashMap<String, Account>();

    public static void createAmount(String content, Currency currency) {
        Amount amount = new Amount();
        amount.setContent(content);
        amount.setCurrency(currency);
        amounts.add(amount);
    }

    public static void createAccountsHashMap() {
        for (Account account : accounts) {
            accounts_hashmap.put(account.getId(), account);
        }
    }

    public static void createSingleBalances(Amount amount, int days, String when) {
        SingleBalance sb = new SingleBalance();
        sb.setDate(new Date());
        sb.setAmount(amount);

        Date Last_manipulation_date = createDate(days, when);
        sb.setLast_action_date_time(Last_manipulation_date);
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
        Account account = createAccount(ID, currency, balance, iban, BIC, name, account_type);
        accounts.add(account);
    }

    public static Account createAccount(String ID, Currency currency, Balances balance, String iban, String BIC, String name, String account_type) {
        Account account = new Account();
        account.setIban(iban);
        account.setId(ID);
        account.setBic(BIC);
        account.setCurrency(currency);
        account.setAccount_type(account_type);
        account.setName(name);
        account.setBalances(balance);
        account.set_links(createEmptyLinks());
        return account;
    }

    public static Links createEmptyLinks() {
        Links links = new Links();

        links.setBalances(null);
        links.setRedirect(null);
        links.setCurrent_page_link(null);
        links.setFirst_page_link(null);
        links.setLast_page_link(null);
        links.setRedirect(null);
        links.setSecond_page_link(null);
        links.setSelect_authentication_method(null);
        links.setSelf(null);
        links.setTransactions(null);
        links.setStatus(null);
        links.setUpdate_psu_authentication(null);
        links.setUpdate_psu_identification(null);

        return links;
    }

    public static void createTransactions(Amount amount, String transactionId,
                                          int nrDaysToValue, int nrDaysToBooking, String when,
                                          String credit_debit, String creditorName,
                                          Account creditor_account, String ultimate_creditor,
                                          String debtorName, Account debtor_account,
                                          String ultimate_debtor, String remittance_information) {

        Transactions t = new Transactions();
        t.setTransaction_id(transactionId);
        t.setEntry_date(new Date());
        t.setValue_date(createDate(nrDaysToValue, "when"));
        t.setBooking_date(createDate(nrDaysToBooking, "when"));
        System.out.println("booking_date" + t.getBooking_date());
        t.setAmount(amount);
        t.setCredit_debit(credit_debit);
        t.setCreditor_name(creditorName);
        t.setCreditor_account(creditor_account);
        t.setUltimate_creditor(ultimate_creditor);
        t.setDebtor(debtorName);
        t.setDebtor_account(debtor_account);
        t.setUltimate_debtor(ultimate_debtor);
        t.setRemittance_information_unstructured(remittance_information);
        transactions.add(t);

    }

    public static List<Amount> getAmounts() {
        return amounts;
    }

    public static List<Account> getAccounts() {
        return accounts;
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

    public static HashMap<String, Account> getAccountsHashMap() {
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
