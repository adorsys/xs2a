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

package de.adorsys.aspsp.aspspmockserver.data.test;

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.aspspmockserver.repository.TanRepository;
import de.adorsys.aspsp.aspspmockserver.repository.TransactionRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Tan;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

/**
 * AccountMockServerData is used to create test data in DB.
 * To fill DB with test data 'aspsp-mock-server' app should be running with profile "data_test"
 * <p>
 * AFTER TESTING THIS CLASS MUST BE DELETED todo https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/87
 */

@Component
@Profile("data_test")
public class AccountMockServerData {
    private PsuRepository psuRepository;
    private TransactionRepository transactionRepository;
    private TanRepository tanRepository;
    private List<SpiAccountDetails> accountDetails;
    private List<Psu> psus;
    private final List<String> ALLOWED_PAYMENTS = Collections.singletonList("sepa-credit-transfers");
    private final Currency EUR = Currency.getInstance("EUR");
    private final Currency USD = Currency.getInstance("USD");

    // Allowed Payments for Cucumber Test User
    private final List<String> ALLOWED_PAYMENTS_CUCUMBER_TESTUSER = Arrays.asList("sepa-credit-transfers", "instant-sepa-credit-transfers");

    public AccountMockServerData(PsuRepository psuRepository, TransactionRepository transactionRepository, TanRepository tanRepository) {
        this.psuRepository = psuRepository;
        this.transactionRepository = transactionRepository;
        this.tanRepository = tanRepository;
        this.accountDetails = fillAccounts();
        this.psus = fillPsu();
        fillTransactions();
        fillTanRepository();
    }

    private void fillTransactions() {
        transactionRepository.save(getTransaction("0001", psus.get(0), psus.get(1), BigDecimal.valueOf(200), EUR, LocalDate.parse("2018-01-02"), LocalDate.parse("2018-01-02"), ""));
        transactionRepository.save(getTransaction("0002", psus.get(0), psus.get(1), BigDecimal.valueOf(150), USD, null, LocalDate.parse("2018-01-02"), ""));
        transactionRepository.save(getTransaction("0003", psus.get(1), psus.get(0), BigDecimal.valueOf(250), EUR, LocalDate.parse("2018-02-02"), LocalDate.parse("2018-02-02"), ""));
        transactionRepository.save(getTransaction("0004", psus.get(1), psus.get(0), BigDecimal.valueOf(20), USD, null, LocalDate.parse("2018-02-02"), ""));
        transactionRepository.save(getTransaction("0005", psus.get(2), psus.get(0), BigDecimal.valueOf(40), EUR, LocalDate.parse("2018-03-02"), LocalDate.parse("2018-03-02"), ""));
        transactionRepository.save(getTransaction("0006", psus.get(2), psus.get(1), BigDecimal.valueOf(50), USD, null, LocalDate.parse("2018-01-02"), ""));
        transactionRepository.save(getTransaction("0007", psus.get(2), psus.get(1), BigDecimal.valueOf(120), EUR, LocalDate.parse("2018-01-02"), LocalDate.parse("2018-01-02"), ""));
        transactionRepository.save(getTransaction("0008", psus.get(1), psus.get(2), BigDecimal.valueOf(30), USD, null, LocalDate.parse("2018-02-02"), ""));
        transactionRepository.save(getTransaction("0009", psus.get(1), psus.get(2), BigDecimal.valueOf(80), EUR, LocalDate.parse("2018-02-02"), LocalDate.parse("2018-02-02"), ""));

        // Transaction data for Cucumber test
        transactionRepository.save(getTransaction("ba8f7012-bdaf-4ada-bbf7-4c004d046ffe", psus.get(4), psus.get(3), BigDecimal.valueOf(50), EUR, LocalDate.parse("2018-07-05"), LocalDate.parse("2018-07-05"), "Spende Greenpeace"));
        transactionRepository.save(getTransaction("7d12ff85-8ace-4124-877a-6bc3f125e98b", psus.get(5), psus.get(3), BigDecimal.valueOf(45.99), EUR, LocalDate.parse("2018-01-01"), LocalDate.parse("2018-01-01"), "Internet Rechnung Januar 2018 - MC-13058247-00000002"));
        transactionRepository.save(getTransaction("bb0962ca-f6db-4ba7-b187-2b2e1af25845", psus.get(6), psus.get(3), BigDecimal.valueOf(200), EUR, LocalDate.parse("2018-05-15"), LocalDate.parse("2018-05-15"), "Alles Gute zum Geburtstag"));
    }

    private SpiTransaction getTransaction(String transactionId, Psu creditor, Psu debtor, BigDecimal amount, Currency currency, LocalDate bookingDate, LocalDate valueDate, String purposeCode) {
        return new SpiTransaction(
            transactionId, "", "", creditor.getId(), bookingDate, valueDate,
            new SpiAmount(currency, amount), getFirstElementName(creditor), getRef(creditor, currency), getFirstElementName(creditor),
            getFirstElementName(debtor), getRef(debtor, currency), getFirstElementName(debtor), "",
            "", purposeCode, "");
    }

    private String getFirstElementName(Psu creditor) {
        return creditor.getAccountDetailsList().get(0).getName();
    }

    private SpiAccountReference getRef(Psu psu, Currency currency) {
        return psu.getAccountDetailsList().stream()
            .filter(det -> det.getCurrency() == currency)
            .map(this::mapToReferenceFromDetails).findFirst().get();
    }

    private List<Psu> fillPsu() {
        return Arrays.asList(
            psuRepository.save(new Psu("PSU_001", "test1@gmail.com", Arrays.asList(accountDetails.get(0), accountDetails.get(1), accountDetails.get(2)), ALLOWED_PAYMENTS)),
            psuRepository.save(new Psu("PSU_002", "test2@gmail.com", Arrays.asList(accountDetails.get(3), accountDetails.get(4)), ALLOWED_PAYMENTS)),
            psuRepository.save(new Psu("PSU_003", "test3@gmail.com", Arrays.asList(accountDetails.get(5), accountDetails.get(6)), ALLOWED_PAYMENTS)),

            // Test User for Cucumber tests
            psuRepository.save(new Psu("d9e71419-24e4-4c5a-8d93-fcc23153aaff", "mueller.alex@web.de", Arrays.asList(accountDetails.get(7)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER)),
            psuRepository.save(new Psu("PSU_CucumberGreenpeace", "greenpeace@web.de", Arrays.asList(accountDetails.get(8)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER)),
            psuRepository.save(new Psu("PSU_CucumberTelekom", "telekom@telekom.de", Arrays.asList(accountDetails.get(9)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER)),
            psuRepository.save(new Psu("PSU_CucumberJochen", "jochen.mueller@web.de", Arrays.asList(accountDetails.get(10)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER))
        );

    }

    private List<SpiAccountDetails> fillAccounts() {

        return Arrays.asList(
            getNewAccount("11111-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(1000), BigDecimal.valueOf(200)), "DE89370400440532013000", "AEYPM5403H", "DEUTDE8EXXX", "Müller", "SCT"),
            getNewAccount("77777-999999999", getNewBalanceList(USD, BigDecimal.valueOf(350), BigDecimal.valueOf(100)), "DE89370400440532013000", "FFGHPM5403H", "DEUTDE8EXXX", "Müller", "SCT"),
            getNewAccount("22222-999999999", getNewBalanceList(USD, BigDecimal.valueOf(2500), BigDecimal.valueOf(300)), "DE89370400440532013001", "QWEPM6427U", "DEUTDE8EXXX", "Müller", "SCT"),
            getNewAccount("33333-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(3000), BigDecimal.valueOf(400)), "DE89370400440532013002", "EWQPS8534R", "DEUTDE8EXXX", "Schmidt", "SCT"),
            getNewAccount("44444-999999999", getNewBalanceList(USD, BigDecimal.valueOf(3500), BigDecimal.valueOf(500)), "DE89370400440532013003", "ASDPS9547Z", "DEUTDE8EXXX", "Schmidt", "SCT"),
            getNewAccount("55555-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(4000), BigDecimal.valueOf(600)), "DE89370400440532013004", "DSACC1876N", "DEUTDE8EXXX", "Company AG", "SCT"),
            getNewAccount("66666-999999999", getNewBalanceList(USD, BigDecimal.valueOf(1400), BigDecimal.valueOf(700)), "DE89370400440532013005", "CXZCC6427T", "DEUTDE8EXXX", "Company AG", "SCT"),

            // account Test User for Cucumber
            getNewAccountCucumberTest("42fb4cc3-91cb-45ba-9159-b87acf6d8add", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(50000)), "DE81432100000004111111", null, null, "Alexander Mueller", "GIRO"),
            getNewAccountCucumberTest("88888-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(1000000)), "DE56432100000005999999", null, null, "Greenpeace", "GIRO"),
            getNewAccountCucumberTest("99999-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(500000)), "DE37123400000005765499", null, null, "Telekom", "GIRO"),
            getNewAccountCucumberTest("12345-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(20000)), "DE56234891038492849284", null, null, "Jochen Mueller", "GIRO")
        );
    }

    private SpiAccountDetails getNewAccount(String id, List<SpiBalances> balance, String iban, String pan, String bic, String name, String accountType) {
        return new SpiAccountDetails(
            id,
            iban,
            iban.substring(3),
            pan,
            pan.substring(3) + "****",
            null,
            balance.get(0).getOpeningBooked().getSpiAmount().getCurrency(),
            name,
            accountType,
            null,
            bic,
            balance
        );
    }

    private List<SpiBalances> getNewBalanceList(Currency currency, BigDecimal amount1, BigDecimal amount2) {
        SpiBalances spiBalances = new SpiBalances();
        spiBalances.setOpeningBooked(getBalance(currency, amount1, LocalDate.now(), LocalDateTime.now()));
        spiBalances.setInterimAvailable(getBalance(currency, amount1, LocalDate.now(), LocalDateTime.now()));
        spiBalances.setAuthorised(getBalance(currency, amount2, LocalDate.now(), LocalDateTime.now()));
        return Collections.singletonList(spiBalances);
    }

    private SpiAccountBalance getBalance(Currency currency, BigDecimal amount, LocalDate date, LocalDateTime dateTime) {
        SpiAccountBalance balance = new SpiAccountBalance();
        balance.setSpiAmount(new SpiAmount(currency, amount));
        balance.setDate(date);
        balance.setLastActionDateTime(dateTime);
        return balance;
    }

    // Custom Methods to create Test account for Cucumber tests
    private SpiAccountDetails getNewAccountCucumberTest(String id, List<SpiBalances> balance, String iban, String pan, String bic, String name, String accountType) {
        return new SpiAccountDetails(
            id,
            iban,
            iban.substring(3),
            pan,
            null,
            null,
            balance.get(0).getInterimAvailable().getSpiAmount().getCurrency(),
            name,
            accountType,
            null,
            bic,
            balance
        );
    }

    private List<SpiBalances> getNewBalanceListCucumberTests(Currency currency, BigDecimal amount1) {
        SpiBalances spiBalances = new SpiBalances();
        spiBalances.setInterimAvailable(getBalance(currency, amount1, LocalDate.now(), LocalDateTime.now()));
        return Collections.singletonList(spiBalances);
    }

    private SpiAccountReference mapToReferenceFromDetails(SpiAccountDetails details) {
        return new SpiAccountReference(details.getIban(), details.getBban(), details.getPan(), details.getMaskedPan(), details.getMsisdn(), details.getCurrency());
    }

    private void fillTanRepository() {
        tanRepository.save(new Tan("PSU_001", "111111"));
        tanRepository.save(new Tan("PSU_002", "222222"));
        tanRepository.save(new Tan("PSU_003", "333333"));
    }
}
