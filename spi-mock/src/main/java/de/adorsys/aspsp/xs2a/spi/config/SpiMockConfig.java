package de.adorsys.aspsp.xs2a.spi.config;

import de.adorsys.aspsp.xs2a.spi.domain.Amount;
import de.adorsys.aspsp.xs2a.spi.domain.SingleBalance;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionsArt;
import de.adorsys.aspsp.xs2a.spi.test.data.MockData;
import org.springframework.context.annotation.Configuration;

import java.util.Currency;

@Configuration
public class SpiMockConfig {

    public SpiMockConfig() {
        Currency euro = Currency.getInstance("EUR");

        MockData.createAmount("-1000.34", euro);
        MockData.createAmount("2000.56", euro);
        MockData.createAmount("3000.45", euro);
        MockData.createAmount("4000.43", euro);
        MockData.createAmount("-500.14", euro);

        int i = 0;

        for (Amount amount : MockData.getAmounts()) {
            MockData.createSingleBalances(amount, i, "future");
            i++;
        }
        i = 0;

        for (SingleBalance singleBalance : MockData.getSingleBalances()) {
            switch (i) {
                case 0:
                    MockData.createBalances(singleBalance, TransactionsArt.booked);
                    break;
                case 1:
                    MockData.createBalances(singleBalance, TransactionsArt.opening_booked);
                    break;
                case 2:
                    MockData.createBalances(singleBalance, TransactionsArt.closing_booked);
                    break;
                case 3:
                    MockData.createBalances(singleBalance, TransactionsArt.expected);
                    break;
                case 4:
                    MockData.createBalances(singleBalance, TransactionsArt.interim_available);
                    break;
            }
            i++;
        }

        MockData.addAccount("11111-999999999", euro, MockData.getBalances().get(0), "DE371234599999", "GENODEF1N02", "Müller", "SCT");
        MockData.addAccount("22222-999999999", euro, MockData.getBalances().get(1), "DE371234599998", "GENODEF1N02", "Albert", "SCT");
        MockData.addAccount("33333-999999999", euro, MockData.getBalances().get(2), "DE371234599997", "GENODEF1N02", "Schmidt", "SCT");
        MockData.addAccount("44444-999999999", euro, MockData.getBalances().get(3), "DE371234599996", "GENODEF1N02", "Telekom", "SCT");
        MockData.addAccount("55555-999999999", euro, MockData.getBalances().get(4), "DE371234599995", "GENODEF1N02", "Bauer", "SCT");

        MockData.createAccountsHashMap();

        MockData.createTransactions(
        MockData.getAmounts().get(0), "12345",
        7, 4, "future",
        "debit", MockData.getAccounts().get(3).getName(), MockData.getAccounts().get(3), "",
        "", null, "", "Example for remittance information");

        MockData.createTransactions(
        MockData.getAmounts().get(4), "123456",
        4, 6, "future",
        "debit", MockData.getAccounts().get(4).getName(), MockData.getAccounts().get(4), "",
        "", null, "", "Another Example for remittance information");

        MockData.createTransactions(
        MockData.getAmounts().get(1), "123457",
        6, 10, "past",
        "credit", "", null, "",
        "MockData.getAccounts().get(2)", MockData.getAccounts().get(2), "", "remittance information");

        MockData.createTransactions(
        MockData.getAmounts().get(2), "1234578",
        17, 20, "past",
        "credit", "", null, "",
        "MockData.getAccounts().get(2)", MockData.getAccounts().get(2), "", "remittance information");
        MockData.createTransactions(
        MockData.getAmounts().get(3), "1234578",
        5, 3, "future",
        "credit", "", null, "",
        "MockData.getAccounts().get(1)", MockData.getAccounts().get(1), "", "remittance information");
    }
}
