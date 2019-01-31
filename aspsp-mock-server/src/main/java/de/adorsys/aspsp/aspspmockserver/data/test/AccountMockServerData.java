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

import de.adorsys.aspsp.aspspmockserver.domain.pis.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType;
import de.adorsys.aspsp.aspspmockserver.repository.*;
import de.adorsys.psd2.aspsp.mock.api.account.*;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspDayOfExecution;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspAuthenticationObject;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspPsuData;
import de.adorsys.psd2.aspsp.mock.api.psu.Psu;
import de.adorsys.psd2.aspsp.mock.api.psu.Tan;
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
 */

@Component
@Profile("data_test")
public class AccountMockServerData {
    private PsuRepository psuRepository;
    private TransactionRepository transactionRepository;
    private TanRepository tanRepository;
    private PaymentRepository paymentRepository;
    private List<AspspAccountDetails> accountDetails;
    private List<Psu> psus;
    private final List<String> ALLOWED_PAYMENTS = Collections.singletonList("sepa-credit-transfers");
    private final Currency EUR = Currency.getInstance("EUR");
    private final Currency USD = Currency.getInstance("USD");
    private final AspspBalanceType BALANCE_TYPE = AspspBalanceType.INTERIM_AVAILABLE;

    // Allowed Payments for Cucumber Test User
    private final List<String> ALLOWED_PAYMENTS_CUCUMBER_TESTUSER = Arrays.asList("sepa-credit-transfers");

    public AccountMockServerData(PsuRepository psuRepository, TransactionRepository transactionRepository, TanRepository tanRepository, PaymentRepository paymentRepository, AccountDetailsRepository accountDetailsRepository) {
        this.psuRepository = psuRepository;
        this.transactionRepository = transactionRepository;
        this.tanRepository = tanRepository;
        this.paymentRepository = paymentRepository;
        this.accountDetails = fillAccounts();
        this.psus = fillPsu();
        fillTransactions();
        fillTanRepository();
        fillPayments();
        accountDetailsRepository.save(accountDetails);
    }

    private void fillPayments() {
        // Payment data for Cucumber Test
        paymentRepository.save(getPayment("a9115f14-4f72-4e4e-8798-202808e85238", psus.get(3), EUR, BigDecimal.valueOf(150), psus.get(7),
                                          "Online-Shoppping Amazon", LocalDate.parse("2018-07-15"), LocalDateTime.parse("2018-07-15T18:30:35.035"), AspspTransactionStatus.RCVD, PisPaymentType.SINGLE, AspspDayOfExecution._15));
        paymentRepository.save(getPayment("68147b90-e4ef-41c6-9c8b-c848c1e93700", psus.get(3), EUR, BigDecimal.valueOf(1030), psus.get(8),
                                          "Holidays", LocalDate.parse("2018-07-31"), LocalDateTime.parse("2018-07-31T18:30:35.035"), AspspTransactionStatus.PDNG, PisPaymentType.SINGLE, AspspDayOfExecution._31));
        paymentRepository.save(getPayment("97694f0d-32e2-43a4-9e8d-261f2fc28236", psus.get(3), EUR, BigDecimal.valueOf(70), psus.get(9),
                                          "Concert Tickets", LocalDate.parse("2018-07-08"), LocalDateTime.parse("2018-07-08T18:30:35.035"), AspspTransactionStatus.RJCT, PisPaymentType.SINGLE, AspspDayOfExecution._08));

    }

    private AspspPayment getPayment(String paymentId, Psu debtor, Currency currency, BigDecimal amount, Psu creditor, String purposeCode, LocalDate requestedExecutionDate,
                                    LocalDateTime requestedExecutionTime, AspspTransactionStatus paymentStatus, PisPaymentType paymentType, AspspDayOfExecution dayOfExecution) {
        AspspPayment payment = new AspspPayment();
        payment.setPaymentId(paymentId);
        payment.setDebtorAccount(getRef(debtor, currency));
        payment.setUltimateDebtor(getFirstElementName(debtor));
        payment.setInstructedAmount(new AspspAmount(currency, amount));
        payment.setCreditorAccount(getRef(creditor, currency));
        payment.setCreditorName(getFirstElementName(creditor));
        payment.setUltimateCreditor(getFirstElementName(creditor));
        payment.setPurposeCode(purposeCode);
        payment.setRequestedExecutionDate(requestedExecutionDate);
        payment.setRequestedExecutionTime(requestedExecutionTime);
        payment.setDayOfExecution(dayOfExecution);
        payment.setPisPaymentType(paymentType);
        payment.setPaymentStatus(paymentStatus);
        return payment;
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
        transactionRepository.save(getTransaction("abc962ca-f6db-4ba7-b187-2b2e1af2test", psus.get(4), psus.get(3), BigDecimal.valueOf(70), EUR, LocalDate.parse("2018-11-24"), LocalDate.parse("2018-12-25"), "Spende fuer Nuernberg Bieber"));
    }

    private AspspTransaction getTransaction(String transactionId, Psu psu, Psu debtor, BigDecimal amount, Currency currency, LocalDate bookingDate, LocalDate valueDate, String purposeCode) {
        return new AspspTransaction(
            transactionId, "", "", "", "", psu.getPsuId(), bookingDate, valueDate,
            new AspspAmount(currency, amount), Collections.emptyList(), getFirstElementName(psu), getRef(psu, currency), getFirstElementName(psu),
            getFirstElementName(debtor), getRef(debtor, currency), getFirstElementName(debtor), "",
            "", purposeCode, "", "");
    }

    private String getFirstElementName(Psu creditor) {
        return creditor.getAccountDetailsList().get(0).getName();
    }

    private AspspAccountReference getRef(Psu psu, Currency currency) {
        return psu.getAccountDetailsList().stream()
                   .filter(det -> det.getCurrency() == currency)
                   .map(this::mapToReferenceFromDetails).findFirst().get();
    }

    private List<Psu> fillPsu() {
        return Arrays.asList(
            psuRepository.save(new Psu("PSU_001", "johndoutestemail@gmail.com", "aspsp", "zzz", Arrays.asList(accountDetails.get(0), accountDetails.get(1), accountDetails.get(2)), ALLOWED_PAYMENTS, Collections.emptyList())),
            psuRepository.save(new Psu("PSU_002", "johndoutestemail@gmail.com", "aspsp1", "zzz", Arrays.asList(accountDetails.get(0), accountDetails.get(1), accountDetails.get(2)), ALLOWED_PAYMENTS, Arrays.asList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_003", "johndoutestemail@gmail.com", "aspsp2", "zzz", Arrays.asList(accountDetails.get(3), accountDetails.get(4)), ALLOWED_PAYMENTS, Arrays.asList(new AspspAuthenticationObject("SMS_OTP", "sms"), new AspspAuthenticationObject("PUSH_OTP", "push")))),
            psuRepository.save(new Psu("PSU_004", "johndoutestemail@gmail.com", "aspsp3", "zzz", Arrays.asList(accountDetails.get(5), accountDetails.get(6)), ALLOWED_PAYMENTS, Arrays.asList(new AspspAuthenticationObject("PUSH_OTP", "push"), new AspspAuthenticationObject("CHIP_OTP", "chip")))),

            // Test User for Cucumber tests //TODO Update Sca Methods for all Cucumber PSUs
            psuRepository.save(new Psu("d9e71419-24e4-4c5a-8d93-fcc23153aaff", "mueller.alex@web.de", "aspsp4", "zzz", Arrays.asList(accountDetails.get(7), accountDetails.get(14)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("d9e71419-24e4-4c5a-8d93-fcc23153aaff", "mueller.alex@web.de", "aspsp5", "zzz", Arrays.asList(accountDetails.get(7), accountDetails.get(14)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_CucumberGreenpeace", "greenpeace@web.de", "aspsp6", "zzz", Arrays.asList(accountDetails.get(8)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_CucumberTelekom", "telekom@telekom.de", "aspsp7", "zzz", Arrays.asList(accountDetails.get(9)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_CucumberJochen", "jochen.mueller@web.de", "aspsp8", "zzz", Arrays.asList(accountDetails.get(10)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_CucumberAmazon", "amazon@mail.com", "aspsp9", "zzz", Arrays.asList(accountDetails.get(11)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_CucumberHolidayCheck", "holidaycheck@mail.com", "aspsp10", "zzz", Arrays.asList(accountDetails.get(12)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms")))),
            psuRepository.save(new Psu("PSU_CucumberEventim", "eventim@web.de", "aspsp11", "zzz", Arrays.asList(accountDetails.get(13)), ALLOWED_PAYMENTS_CUCUMBER_TESTUSER, Collections.singletonList(new AspspAuthenticationObject("SMS_OTP", "sms"))))
        );

    }

    private List<AspspAccountDetails> fillAccounts() {

        return Arrays.asList(getNewAccount("11111-11111", "11111-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(1000)), "DE89370400440532013000", "AEYPM5403H", "DEUTDE8EXXX", "Müller", "SCT", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccount("11111-11111", "77777-999999999", getNewBalanceList(USD, BigDecimal.valueOf(350)), "DE89370400440532013000", "FFGHPM5403H", "DEUTDE8EXXX", "Müller", "SCT", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccount("22222-22222", "22222-999999999", getNewBalanceList(USD, BigDecimal.valueOf(2500)), "DE89370400440532013001", "QWEPM6427U", "DEUTDE8EXXX", "Müller", "SCT", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccount("33333-33333", "33333-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(3000)), "LU280019400644750000", "EWQPS8534R", "DEUTDE8EXXX", "Schmidt", "SCT", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccount("44444-44444", "44444-999999999", getNewBalanceList(USD, BigDecimal.valueOf(3500)), "DE89370400440532013003", "ASDPS9547Z", "DEUTDE8EXXX", "Schmidt", "SCT", Arrays.asList(new AspspPsuData("aspsp", "psu-type", "123456", "corp-type"), new AspspPsuData("aspsp1", "psu-type", "123456", "corp-type"), new AspspPsuData("aspsp2", "psu-type", "123456", "corp-type"))),
                             getNewAccount("55555-55555", "55555-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(4000)), "DE89370400440532013004", "DSACC1876N", "DEUTDE8EXXX", "Company AG", "SCT", Arrays.asList(new AspspPsuData("aspsp1", "psu-type", "123456", "corp-type"), new AspspPsuData("aspsp2", "psu-type", "123456", "corp-type"))),
                             getNewAccount("66666-66666", "66666-999999999", getNewBalanceList(USD, BigDecimal.valueOf(1400)), "DE89370400440532013005", "CXZCC6427T", "DEUTDE8EXXX", "Company AG", "SCT", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccount("11111-11111", "66666-999999999", getNewBalanceList(USD, BigDecimal.valueOf(1400)), "DE52500105173911841934", "CXZCC6427T", "DEUTDE8EXXX", "Company AG", "SCT", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),

                             // account Test User for Cucumber
                             getNewAccountCucumberTest("11111-11112", "42fb4cc3-91cb-45ba-9159-b87acf6d8add", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(50000)), "DE52500105173911841934", null, null, "Alexander Mueller", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11113", "88888-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(1000000)), "DE24500105172916349286", null, null, "Greenpeace", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11114", "99999-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(500000)), "DE68500105174416628385", null, null, "Telekom", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11115", "12345-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(20000)), "DE06500105171657611553", null, null, "Jochen Mueller", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11116", "23236-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(6500000)), "DE49500105175378548627", null, null, "Amazon", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11117", "37289-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(9500000)), "DE21500105176194357737", null, null, "Holidaycheck.com", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11118", "10023-999999999", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(2500000)), "DE54500105173424724776", null, null, "Eventim", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type"))),
                             getNewAccountCucumberTest("11111-11119", "868beafc-ef87-4fdb-ac0a-dd6c52b77ee6", getNewBalanceListCucumberTests(EUR, BigDecimal.valueOf(50000)), "DE15086016167627403587", null, null, "Alexander Mueller", "GIRO", Collections.singletonList(new AspspPsuData("PSU_001", "psu-type", "123456", "corp-type")))
        );

    }

    private AspspAccountDetails getNewAccount(String aspspAccountId, String resourceId, List<AspspAccountBalance> balance, String iban, String pan, String bic, String name, String accountType, List<AspspPsuData> psuDataList) {
        return new AspspAccountDetails(
            aspspAccountId,
            resourceId,
            iban,
            iban.substring(3),
            pan,
            pan.substring(3) + "****",
            null,
            balance.get(0).getSpiBalanceAmount().getCurrency(),
            name,
            accountType,
            null,
            null,
            psuDataList,
            bic,
            null,
            null,
            null,
            balance
        );
    }

    private List<AspspAccountBalance> getNewBalanceList(Currency currency, BigDecimal amount1) {
        return Collections.singletonList(getBalance(currency, amount1, LocalDate.now(), LocalDateTime.now()));
    }

    private AspspAccountBalance getBalance(Currency currency, BigDecimal amount, LocalDate date, LocalDateTime dateTime) {
        AspspAccountBalance balance = new AspspAccountBalance();
        balance.setSpiBalanceAmount(new AspspAmount(currency, amount));
        balance.setSpiBalanceType(BALANCE_TYPE);
        balance.setReferenceDate(date);
        balance.setLastChangeDateTime(dateTime);
        balance.setLastCommittedTransaction("abcd");
        return balance;
    }

    // Custom Methods to create Test account for Cucumber tests
    private AspspAccountDetails getNewAccountCucumberTest(String aspspAccountId, String resourceId, List<AspspAccountBalance> balance, String iban, String pan, String bic, String name, String accountType, List<AspspPsuData> psuDataList) {
        return new AspspAccountDetails(
            aspspAccountId,
            resourceId,
            iban,
            iban.substring(3),
            pan,
            null,
            null,
            balance.get(0).getSpiBalanceAmount().getCurrency(),
            name,
            accountType,
            null,
            null,
            psuDataList,
            bic,
            null,
            null,
            null,
            balance
        );
    }

    private List<AspspAccountBalance> getNewBalanceListCucumberTests(Currency currency, BigDecimal amount1) {
        return Collections.singletonList(getBalance(currency, amount1, LocalDate.now(), LocalDateTime.now()));
    }

    private AspspAccountReference mapToReferenceFromDetails(AspspAccountDetails details) {
        return new AspspAccountReference(details.getResourceId(), details.getIban(), details.getBban(), details.getPan(), details.getMaskedPan(), details.getMsisdn(), details.getCurrency());
    }

    private void fillTanRepository() {
        tanRepository.save(new Tan("PSU_001", "111111"));
        tanRepository.save(new Tan("PSU_002", "222222"));
        tanRepository.save(new Tan("PSU_003", "333333"));
    }
}
