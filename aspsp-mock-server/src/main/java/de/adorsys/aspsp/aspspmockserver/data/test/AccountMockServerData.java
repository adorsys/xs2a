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
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
    private List<SpiAccountDetails> accountDetails;
    private final Currency EUR = Currency.getInstance("EUR");
    private final Currency USD = Currency.getInstance("USD");

    public AccountMockServerData(PsuRepository psuRepository, ConsentRepository consentRepository) {
        this.psuRepository = psuRepository;
        this.consentRepository = consentRepository;
        this.accountDetails = fillAccounts();
        fillPsu();
        fillConsent();
    }

    private void fillConsent() {
        consentRepository.save(
            new SpiAccountConsent("AllWB",
                new SpiAccountAccess(
                    getReferencesList(),getReferencesList(),getReferencesList(),SpiAccountAccessType.ALL_ACCOUNTS,SpiAccountAccessType.ALL_ACCOUNTS),
                false, new Date(), 100, new Date(), SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, true,false)
        );
        consentRepository.save(
            new SpiAccountConsent("AllWOB",
                new SpiAccountAccess(
                    getReferencesList(),getReferencesList(),getReferencesList(),SpiAccountAccessType.ALL_ACCOUNTS,SpiAccountAccessType.ALL_ACCOUNTS),
                false, new Date(), 100, new Date(), SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, true,false)
        );

        consentRepository.save(
            new SpiAccountConsent("Acc1WB",
                new SpiAccountAccess(
                    Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))),Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))),null,null),
                false, new Date(), 100, new Date(), SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, true,false)
        );
        consentRepository.save(
            new SpiAccountConsent("Acc1WOB",
                new SpiAccountAccess(
                    Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))), Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))),Collections.singletonList(mapToReferenceFromDetails(accountDetails.get(0))),null,null),
                false, new Date(), 100, new Date(), SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, false,false)
        );
    }

    private void fillPsu() {
        psuRepository.save(new Psu("PSU_001", Arrays.asList(accountDetails.get(0), accountDetails.get(1), accountDetails.get(2))));
        psuRepository.save(new Psu("PSU_002", Arrays.asList(accountDetails.get(3), accountDetails.get(4))));
        psuRepository.save(new Psu("PSU_003", Arrays.asList(accountDetails.get(5), accountDetails.get(6))));

    }

    private List<SpiAccountDetails> fillAccounts() {

        return Arrays.asList(
            getNewAccount("11111-999999999", getNewBalanceList(EUR,"1000", "200"), "DE89370400440532013000", "AEYPM5403H","DEUTDE8EXXX","Müller", "SCT"),
            getNewAccount("77777-999999999", getNewBalanceList(USD,"350", "100"), "DE89370400440532013000", "FFGHPM5403H","DEUTDE8EXXX","Müller", "SCT"),
            getNewAccount("22222-999999999", getNewBalanceList(USD,"2500", "300"), "DE89370400440532013001", "QWEPM6427U","DEUTDE8EXXX","Müller", "SCT"),
            getNewAccount("33333-999999999", getNewBalanceList( EUR,"3000", "400"), "DE89370400440532013002", "EWQPS8534R","DEUTDE8EXXX","Schmidt", "SCT"),
            getNewAccount("44444-999999999", getNewBalanceList(USD,"3500", "500"), "DE89370400440532013003", "ASDPS9547Z","DEUTDE8EXXX","Schmidt", "SCT"),
            getNewAccount("55555-999999999", getNewBalanceList( EUR,"4000", "600"), "DE89370400440532013004", "DSACC1876N","DEUTDE8EXXX","Company AG", "SCT"),
            getNewAccount("66666-999999999", getNewBalanceList( USD,"1400", "700"), "DE89370400440532013005", "CXZCC6427T","DEUTDE8EXXX","Company AG", "SCT"));
    }

    private SpiAccountDetails getNewAccount(String id, List<SpiBalances> balance, String iban, String pan, String bic, String name, String accountType) {
        return new SpiAccountDetails(
            id,
            iban,
            iban.substring(3),
            pan,
            pan.substring(3)+"****",
            null,
            balance.get(0).getOpeningBooked().getSpiAmount().getCurrency(),
            name,
            accountType,
            null,
            bic,
            balance
        );
    }

    private List<SpiBalances> getNewBalanceList(Currency currency, String ammount1, String ammount2){
        SpiBalances spiBalances = new SpiBalances();
        spiBalances.setOpeningBooked(getBalance(currency,ammount1));
        spiBalances.setAuthorised(getBalance(currency,ammount2));
        return Collections.singletonList(spiBalances);

    }

    private SpiAccountBalance getBalance(Currency currency, String ammount){
        SpiAccountBalance balance = new SpiAccountBalance();
        balance.setSpiAmount(new SpiAmount(currency,ammount));
        balance.setDate(new Date());
        balance.setLastActionDateTime(new Date());
        return balance;
    }

    private List<SpiAccountReference> getReferencesList(){
     return accountDetails.stream().map(this::mapToReferenceFromDetails).collect(Collectors.toList());
    }
    private SpiAccountReference mapToReferenceFromDetails(SpiAccountDetails details){
        return new SpiAccountReference(details.getIban(),details.getBban(),details.getPan(),details.getMaskedPan(),details.getMsisdn(),details.getCurrency());
    }

}
