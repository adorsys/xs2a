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

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AccountMockServerData is used to create test data in DB.
 * To fill DB with test data 'aspsp-mock-server' app should be running with profile "data_test"
 *
 * AFTER TESTING THIS CLASS MUST BE DELETED todo https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/87
 */

@Component
@Profile("data_test")
public class AccountMockServerData {

    private final AccountService accountService;
    private final PsuService psuService;

    public AccountMockServerData(AccountService accountService,PsuService psuService) {
        this.accountService = accountService;
        this.psuService = psuService;
        fillAccounts();
        fillPsu();
    }

    private void fillPsu(){
        Currency euro = Currency.getInstance("EUR");

        List<SpiAccountDetails> newList = Arrays.asList(
        getNewAccount("11111-999999999", euro, getNewBalanceList("1000", "200"), "DE371234599999", "GENODEF1N02", "Müller", "SCT"),
        getNewAccount("99999-999999999", euro, getNewBalanceList("1000", "200"), "DE371234599988", "GENODEF1N03", "Müller", "SCT"));

        psuService.createPsuAndReturnId(newList);
        psuService.createPsuAndReturnId(Collections.singletonList(getNewAccount("22222-999999999", euro, getNewBalanceList("2500", "300"), "DE371234599998", "GENODEF1N02", "Albert", "SCT")));
        psuService.createPsuAndReturnId(Collections.singletonList(getNewAccount("33333-999999999", euro, getNewBalanceList("3000", "400"), "DE371234599997", "GENODEF1N02", "Schmidt", "SCT")));
        psuService.createPsuAndReturnId(Collections.singletonList(getNewAccount("44444-999999999", euro, getNewBalanceList("3500", "500"), "DE371234599996", "GENODEF1N02", "Telekom", "SCT")));
        psuService.createPsuAndReturnId(Collections.singletonList(getNewAccount("55555-999999999", euro, getNewBalanceList("4000", "600"), "DE371234599995", "GENODEF1N02", "Bauer", "SCT")));

    }

    private void fillAccounts() {
        Currency euro = Currency.getInstance("EUR");

        accountService.addOrUpdateAccount(getNewAccount("11111-999999999", euro, getNewBalanceList("1000", "200"), "DE371234599995", "GENODEF1N02", "Müller", "SCT"));
        accountService.addOrUpdateAccount(getNewAccount("22222-999999999", euro, getNewBalanceList("2500", "300"), "DE371234599998", "GENODEF1N02", "Albert", "SCT"));
        accountService.addOrUpdateAccount(getNewAccount("33333-999999999", euro, getNewBalanceList("3000", "400"), "DE371234599997", "GENODEF1N02", "Schmidt", "SCT"));
        accountService.addOrUpdateAccount(getNewAccount("44444-999999999", euro, getNewBalanceList("3500", "500"), "DE371234599996", "GENODEF1N02", "Telekom", "SCT"));
        accountService.addOrUpdateAccount(getNewAccount("55555-999999999", euro, getNewBalanceList("4000", "600"), "DE371234599945", "GENODEF1N02", "Bauer", "SCT"));
    }

    private SpiAccountDetails getNewAccount(String id, Currency currency, List<SpiBalances> balance, String iban, String bic, String name, String accountType) {
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

    private List<SpiBalances> getNewBalanceList(String authorisedBalance, String openingBalance) {
        return Collections.singletonList(getNewBalance(authorisedBalance, openingBalance));
    }

    private SpiBalances getNewBalance(String authorisedBalance, String openingBalance) {
        Currency euro = Currency.getInstance("EUR");

        SpiBalances balance = new SpiBalances();
        balance.setAuthorised(getNewSingleBalances(new SpiAmount(euro, authorisedBalance)));
        balance.setOpeningBooked(getNewSingleBalances(new SpiAmount(euro, openingBalance)));

        return balance;
    }

    private SpiAccountBalance getNewSingleBalances(SpiAmount spiAmount) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setDate(new Date());
        sb.setSpiAmount(spiAmount);
        sb.setLastActionDateTime(new Date());
        return sb;
    }
}
