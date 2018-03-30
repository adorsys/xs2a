package de.adorsys.aspsp.xs2a.spi.config;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionsArt;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.List;

@Configuration
public class SpiMockConfig {

    public SpiMockConfig() {
        fillAccounts();
        fillConsents();
    }

    private void fillAccounts() {
        Currency euro = Currency.getInstance("EUR");

        AccountMockData.createAmount("-1000.34", euro);
        AccountMockData.createAmount("2000.56", euro);
        AccountMockData.createAmount("3000.45", euro);
        AccountMockData.createAmount("4000.43", euro);
        AccountMockData.createAmount("-500.14", euro);

        int i = 0;

        for (SpiAmount spiAmount : AccountMockData.getSpiAmounts()) {
            AccountMockData.createSingleBalances(spiAmount, i, "future");
            i++;
        }
        i = 0;

        for (SpiAccountBalance singleBalance : AccountMockData.getSingleBalances()) {
            switch (i) {
                case 0:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.booked);
                    break;
                case 1:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.opening_booked);
                    break;
                case 2:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.closing_booked);
                    break;
                case 3:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.expected);
                    break;
                case 4:
                    AccountMockData.createBalances(singleBalance, TransactionsArt.interim_available);
                    break;
            }
            i++;
        }

        AccountMockData.addAccount("11111-999999999", euro, AccountMockData.getBalances(), "DE371234599999", "GENODEF1N02", "Müller", "SCT");
        AccountMockData.addAccount("22222-999999999", euro, AccountMockData.getBalances(), "DE371234599998", "GENODEF1N02", "Albert", "SCT");
        AccountMockData.addAccount("33333-999999999", euro, AccountMockData.getBalances(), "DE371234599997", "GENODEF1N02", "Schmidt", "SCT");
        AccountMockData.addAccount("44444-999999999", euro, AccountMockData.getBalances(), "DE371234599996", "GENODEF1N02", "Telekom", "SCT");
        AccountMockData.addAccount("55555-999999999", euro, AccountMockData.getBalances(), "DE371234599995", "GENODEF1N02", "Bauer", "SCT");

        AccountMockData.createAccountsHashMap();

        AccountMockData.createTransactions(
        AccountMockData.getSpiAmounts().get(0), "12345",
        7, 4, "future",
        "debit", AccountMockData.getAccountDetails().get(3).getName(), convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(3)), "",
        "", null, "", "Example for remittance information");

        AccountMockData.createTransactions(
        AccountMockData.getSpiAmounts().get(4), "123456",
        4, 6, "future",
        "debit", AccountMockData.getAccountDetails().get(4).getName(), convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(4)), "",
        "", null, "", "Another Example for remittance information");

        AccountMockData.createTransactions(
        AccountMockData.getSpiAmounts().get(1), "123457",
        6, 10, "past",
        "credit", "", null, "",
        "AccountMockData.getAccounts().get(2)", convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(2)), "", "remittance information");

        AccountMockData.createTransactions(
        AccountMockData.getSpiAmounts().get(2), "1234578",
        17, 20, "past",
        "credit", "", null, "",
        "AccountMockData.getAccounts().get(2)", convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(2)), "", "remittance information");
        AccountMockData.createTransactions(
        AccountMockData.getSpiAmounts().get(3), "1234578",
        5, 3, "future",
        "credit", "", null, "",
        "AccountMockData.getAccounts().get(1)", convertAccountDetailsToAccountReference(AccountMockData.getAccountDetails().get(1)), "", "remittance information");

    }

    private void fillConsents() {
        ConsentMockData.createAccountConsent(getAicRequest_1(), true, false);
        ConsentMockData.createAccountConsent(getAicRequest_2(), true, false);
    }

    private SpiAccountReference convertAccountDetailsToAccountReference(SpiAccountDetails accountDetails) {
        return new SpiAccountReference(
            accountDetails.getId(),
            accountDetails.getIban(),
            accountDetails.getBban(),
            accountDetails.getMaskedPan(),
            accountDetails.getMaskedPan(),
            accountDetails.getMsisdn(),
            accountDetails.getCurrency()
        );
    }

    private SpiCreateConsentRequest getAicRequest_1() {
        SpiAccountReference iban1 = new SpiAccountReference(
        null,
        "DE8710010010653456712",
        null,
        null,
        null,
        null,
        null
        );

        SpiAccountReference iban2 = new SpiAccountReference(
            null,
            "DE8710010010653456723",
            null,
            null,
            null,
            null,
            Currency.getInstance("USD")
        );

        SpiAccountReference iban3 = new SpiAccountReference(
            null,
            "DE8710010010653456734",
            null,
            null,
            null,
            null,
            null
        );

        SpiAccountReference iban4 = new SpiAccountReference(
            null,
            "DE870010010165456745",
            null,
            null,
            null,
            null,
            null
        );

        SpiAccountReference maskedPan = new SpiAccountReference(
            null,
            null,
            null,
            null,
            "873456xxxxxx1245",
            null,
            null
        );

        List<SpiAccountReference> balances = Arrays.asList(iban1, iban2, iban3);
        List<SpiAccountReference> transactions = Arrays.asList(iban4, maskedPan);

        SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
        spiAccountAccess.setBalances(balances);
        spiAccountAccess.setTransactions(transactions);

        return new SpiCreateConsentRequest(
        spiAccountAccess,
            true,
            getDateFromDateStringNoTimeZone("2017-11-01"),
            4,
            false
        );
    }

    private SpiCreateConsentRequest getAicRequest_2() {
        SpiAccountReference iban1 = new SpiAccountReference(
            null,
            "DE5410010010165456787",
            null,
            null,
            null,
            null,
            null
        );

        SpiAccountReference iban2 = new SpiAccountReference(
            null,
            "DE650010010123456743",
            null,
            null,
            null,
            null,
            Currency.getInstance("USD")
        );

        SpiAccountReference iban3 = new SpiAccountReference(
            null,
            "DE430010010123456534",
            null,
            null,
            null,
            null,
            null
        );

        SpiAccountReference iban4 = new SpiAccountReference(
            null,
            "DE9780010010123452356",
            null,
            null,
            null,
            null,
            null
        );

        SpiAccountReference maskedPan = new SpiAccountReference(
            null,
            null,
            null,
            null,
            "553456xxxxxx12397",
            null,
            null
        );

        List<SpiAccountReference> balances = Arrays.asList(iban1, iban2, iban3);
        List<SpiAccountReference> transactions = Arrays.asList(iban4, maskedPan);

        SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
        spiAccountAccess.setBalances(balances);
        spiAccountAccess.setTransactions(transactions);

        return new SpiCreateConsentRequest(
        spiAccountAccess,
            true,
            getDateFromDateStringNoTimeZone("2017-04-25"),
            4,
            false
        );
    }

    private static Date getDateFromDateStringNoTimeZone(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
