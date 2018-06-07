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

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.aspspmockserver.repository.TransactionRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    private ConsentRepository consentRepository;
    private TransactionRepository transactionRepository;
    private List<SpiAccountDetails> accountDetails;
    private List<SpiAccountReference> references;
    private List<Psu> psus;
    private final Currency EUR = Currency.getInstance("EUR");
    private final Currency USD = Currency.getInstance("USD");

    public AccountMockServerData(PsuRepository psuRepository, ConsentRepository consentRepository, TransactionRepository transactionRepository) {
        this.psuRepository = psuRepository;
        this.consentRepository = consentRepository;
        this.transactionRepository = transactionRepository;
        this.accountDetails = fillAccounts();
        this.references = getReferencesList();
        this.psus = fillPsu();
        fillConsent();
        fillTransactions();
    }

    private void fillTransactions() {
        transactionRepository.save(getTransaction("0001", psus.get(0), psus.get(1), BigDecimal.valueOf(200), EUR, "02/01/2018", "02/01/2018"));
        transactionRepository.save(getTransaction("0002", psus.get(0), psus.get(1), BigDecimal.valueOf(150), USD, null, "02/01/2018"));
        transactionRepository.save(getTransaction("0003", psus.get(1), psus.get(0), BigDecimal.valueOf(250), EUR, "02/02/2018", "02/02/2018"));
        transactionRepository.save(getTransaction("0004", psus.get(1), psus.get(0), BigDecimal.valueOf(20), USD, null, "02/02/2018"));
        transactionRepository.save(getTransaction("0005", psus.get(2), psus.get(0), BigDecimal.valueOf(40), EUR, "02/03/2018", "02/03/2018"));
        transactionRepository.save(getTransaction("0006", psus.get(2), psus.get(1), BigDecimal.valueOf(50), USD, null, "02/01/2018"));
        transactionRepository.save(getTransaction("0007", psus.get(2), psus.get(1), BigDecimal.valueOf(120), EUR, "02/01/2018", "02/01/2018"));
        transactionRepository.save(getTransaction("0008", psus.get(1), psus.get(2), BigDecimal.valueOf(30), USD, null, "02/01/2018"));
        transactionRepository.save(getTransaction("0009", psus.get(1), psus.get(2), BigDecimal.valueOf(80), EUR, "02/02/2018", "02/02/2018"));
    }

    private SpiTransaction getTransaction(String transactionId, Psu creditor, Psu debtor, BigDecimal amount, Currency currency, String bookingDate, String valueDate) {
        return new SpiTransaction(
            transactionId, "", "", creditor.getId(), getDateFromString(bookingDate), getDateFromString(valueDate),
            new SpiAmount(currency, amount), getFirstElementName(creditor), getRef(creditor, currency), getFirstElementName(creditor),
            getFirstElementName(debtor), getRef(debtor, currency), getFirstElementName(debtor), "",
            "", "", "");
    }

    private String getFirstElementName(Psu creditor) {
        return creditor.getAccountDetailsList().get(0).getName();
    }

    private SpiAccountReference getRef(Psu psu, Currency currency) {
        return psu.getAccountDetailsList().stream()
                   .filter(det -> det.getCurrency() == currency)
                   .map(this::mapToReferenceFromDetails).findFirst().get();
    }

    private void fillConsent() {
        consentRepository.save(
            new SpiAccountConsent("AllWB",
                new SpiAccountAccess(
                    references, references, references, SpiAccountAccessType.ALL_ACCOUNTS, SpiAccountAccessType.ALL_ACCOUNTS),
                false, new Date(), 100, new Date(), SpiConsentStatus.VALID, true, false)
        );
        consentRepository.save(
            new SpiAccountConsent("AllWOB",
                new SpiAccountAccess(
                    references, Collections.emptyList(), Collections.emptyList(), SpiAccountAccessType.ALL_ACCOUNTS, SpiAccountAccessType.ALL_ACCOUNTS),
                false, new Date(), 100, new Date(), SpiConsentStatus.VALID, true, false)
        );

        consentRepository.save(
            new SpiAccountConsent("Acc1WB",
                new SpiAccountAccess(
                    Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), null, null),
                false, new Date(), 100, new Date(), SpiConsentStatus.VALID, true, false)
        );
        consentRepository.save(
            new SpiAccountConsent("Acc1WOB",
                new SpiAccountAccess(
                    Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), null, null),
                false, new Date(), 100, new Date(), SpiConsentStatus.VALID, false, false));
        consentRepository.save(
            new SpiAccountConsent("Acc2WB",
                new SpiAccountAccess(
                    Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(1))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(1))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(1))), null, null),
                false, new Date(), 100, new Date(), SpiConsentStatus.VALID, true, false)
        );
        consentRepository.save(
            new SpiAccountConsent("Acc2WOB",
                new SpiAccountAccess(
                    Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(1))), Collections.emptyList(), Collections.emptyList(), null, null),
                false, new Date(), 100, new Date(), SpiConsentStatus.VALID, false, false)
        );
    }

    private List<Psu> fillPsu() {
        return Arrays.asList(
            psuRepository.save(new Psu("PSU_001", Arrays.asList(accountDetails.get(0), accountDetails.get(1), accountDetails.get(2)))),
            psuRepository.save(new Psu("PSU_002", Arrays.asList(accountDetails.get(3), accountDetails.get(4)))),
            psuRepository.save(new Psu("PSU_003", Arrays.asList(accountDetails.get(5), accountDetails.get(6)))));
    }

    private List<SpiAccountDetails> fillAccounts() {

        return Arrays.asList(
            getNewAccount("11111-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(1000), BigDecimal.valueOf(200)), "DE89370400440532013000", "AEYPM5403H", "DEUTDE8EXXX", "Müller", "SCT"),
            getNewAccount("77777-999999999", getNewBalanceList(USD, BigDecimal.valueOf(350), BigDecimal.valueOf(100)), "DE89370400440532013000", "FFGHPM5403H", "DEUTDE8EXXX", "Müller", "SCT"),
            getNewAccount("22222-999999999", getNewBalanceList(USD, BigDecimal.valueOf(2500), BigDecimal.valueOf(300)), "DE89370400440532013001", "QWEPM6427U", "DEUTDE8EXXX", "Müller", "SCT"),
            getNewAccount("33333-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(3000), BigDecimal.valueOf(400)), "DE89370400440532013002", "EWQPS8534R", "DEUTDE8EXXX", "Schmidt", "SCT"),
            getNewAccount("44444-999999999", getNewBalanceList(USD, BigDecimal.valueOf(3500), BigDecimal.valueOf(500)), "DE89370400440532013003", "ASDPS9547Z", "DEUTDE8EXXX", "Schmidt", "SCT"),
            getNewAccount("55555-999999999", getNewBalanceList(EUR, BigDecimal.valueOf(4000), BigDecimal.valueOf(600)), "DE89370400440532013004", "DSACC1876N", "DEUTDE8EXXX", "Company AG", "SCT"),
            getNewAccount("66666-999999999", getNewBalanceList(USD, BigDecimal.valueOf(1400), BigDecimal.valueOf(700)), "DE89370400440532013005", "CXZCC6427T", "DEUTDE8EXXX", "Company AG", "SCT"));
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
        spiBalances.setOpeningBooked(getBalance(currency, amount1));
        spiBalances.setInterimAvailable(getBalance(currency, amount1));
        spiBalances.setAuthorised(getBalance(currency, amount2));
        return Collections.singletonList(spiBalances);
    }

    private SpiAccountBalance getBalance(Currency currency, BigDecimal amount) {
        SpiAccountBalance balance = new SpiAccountBalance();
        balance.setSpiAmount(new SpiAmount(currency, amount));
        balance.setDate(new Date());
        balance.setLastActionDateTime(new Date());
        return balance;
    }

    private List<SpiAccountReference> getReferencesList() {
        return accountDetails.stream()
                   .map(this::mapToReferenceFromDetails)
                   .collect(Collectors.toList());
    }

    private SpiAccountReference mapToReferenceFromDetails(SpiAccountDetails details) {
        return new SpiAccountReference(details.getIban(), details.getBban(), details.getPan(), details.getMaskedPan(), details.getMsisdn(), details.getCurrency());
    }

    private Date getDateFromString(String date) {
        date = Optional.ofNullable(date).orElse("24/01/2019");
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        try {
            return df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
