package de.adorsys.aspsp.aspspmockserver.data.test;

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;

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

    public AccountMockServerData(AccountService accountService) {
        this.accountService = accountService;

        fillAccounts();
    }

    private void fillAccounts() {
        Currency euro = Currency.getInstance("EUR");

        accountService.addAccount(getNewAccount("11111-999999999", euro, getNewBalanceList("1000", "200"), "DE371234599999", "GENODEF1N02", "Müller", "SCT"));
        accountService.addAccount(getNewAccount("22222-999999999", euro, getNewBalanceList("2500", "300"), "DE371234599998", "GENODEF1N02", "Albert", "SCT"));
        accountService.addAccount(getNewAccount("33333-999999999", euro, getNewBalanceList("3000", "400"), "DE371234599997", "GENODEF1N02", "Schmidt", "SCT"));
        accountService.addAccount(getNewAccount("44444-999999999", euro, getNewBalanceList("3500", "500"), "DE371234599996", "GENODEF1N02", "Telekom", "SCT"));
        accountService.addAccount(getNewAccount("55555-999999999", euro, getNewBalanceList("4000", "600"), "DE371234599995", "GENODEF1N02", "Bauer", "SCT"));
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
