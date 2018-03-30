package de.adorsys.aspsp.xs2a.service;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountMapperTest {
    private final String SPI_ACCOUNT_DETAILS_JSON_PATH = "/json/MapSpiAccountDetailsToXs2aAccountDetailsTest.json";
    private final String SPI_BALANCES_JSON_PATH = "/json/MapSpiBalancesTest.json";
    private final String SPI_TRANSACTION_JSON_PATH = "/json/mapAccountReportTest.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    AccountMapper accountMapper;

    @Test
    public void mapSpiAccountDetailsToXs2aAccountDetails() throws IOException {
        //Given:
        String spiAccountDetailsJson = IOUtils.resourceToString(SPI_ACCOUNT_DETAILS_JSON_PATH, UTF_8);
        SpiAccountDetails donorAccountDetails = new Gson().fromJson(spiAccountDetailsJson, SpiAccountDetails.class);

        //When:
        assertNotNull(donorAccountDetails);
        AccountDetails actualAccountDetails = accountMapper.mapSpiAccountDetailsToXs2aAccountDetails(donorAccountDetails);

        //Then:
        assertThat(actualAccountDetails.getId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(actualAccountDetails.getIban()).isEqualTo("DE2310010010123456789");
        assertThat(actualAccountDetails.getBban()).isEqualTo("DE2310010010123452343");
        assertThat(actualAccountDetails.getAccountType()).isEqualTo("Girokonto");
        assertThat(actualAccountDetails.getName()).isEqualTo("Main Account");
        assertThat(actualAccountDetails.getCashAccountType()).isEqualTo(CashAccountType.CURRENT_ACCOUNT);
        assertThat(actualAccountDetails.getBic()).isEqualTo("EDEKDEHHXXX");
        assertThat(actualAccountDetails.getBalances()[0].getClosingBooked().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualAccountDetails.getBalances()[0].getClosingBooked().getLastActionDateTime()).isEqualTo("2017-10-25T18:30:35.035Z");
        assertThat(actualAccountDetails.getBalances()[0].getClosingBooked().getDate()).isEqualTo("2007-01-01");
    }

    @Test
    public void mapSpiBalances() throws IOException {
        //Given:
        String spiBalancesJson = IOUtils.resourceToString(SPI_BALANCES_JSON_PATH, UTF_8);
        SpiBalances donorBalances = new Gson().fromJson(spiBalancesJson, SpiBalances.class);
        List<SpiBalances> donorBalancesList = new ArrayList<SpiBalances>();
        donorBalancesList.add(donorBalances);

        //When:
        assertNotNull(donorBalances);
        List<Balances> actualBalances = Arrays.asList(accountMapper.mapListSpiBalances(donorBalancesList));

        //Then:
        assertThat(actualBalances.get(0).getClosingBooked().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getClosingBooked().getLastActionDateTime()).isEqualTo("2017-10-25T18:30:35.035Z");
        assertThat(actualBalances.get(0).getClosingBooked().getDate()).isEqualTo("2007-01-01");
        assertThat(actualBalances.get(0).getAuthorised().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getAuthorised().getLastActionDateTime()).isEqualTo("2017-10-25T18:30:35.035Z");
        assertThat(actualBalances.get(0).getAuthorised().getDate()).isEqualTo("2007-01-01");
        assertThat(actualBalances.get(0).getExpected().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getExpected().getLastActionDateTime()).isEqualTo("2017-10-25T18:30:35.035Z");
        assertThat(actualBalances.get(0).getExpected().getDate()).isEqualTo("2007-01-01");
        assertThat(actualBalances.get(0).getInterimAvailable().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getInterimAvailable().getLastActionDateTime()).isEqualTo("2017-10-25T18:30:35.035Z");
        assertThat(actualBalances.get(0).getInterimAvailable().getDate()).isEqualTo("2007-01-01");
        assertThat(actualBalances.get(0).getOpeningBooked().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getOpeningBooked().getLastActionDateTime()).isEqualTo("2017-10-25T18:30:35.035Z");
        assertThat(actualBalances.get(0).getOpeningBooked().getDate()).isEqualTo("2007-01-01");
    }

    @Test
    public void mapAccountReport() throws IOException {
        //Given:
        String spiTransactionJson = IOUtils.resourceToString(SPI_TRANSACTION_JSON_PATH, UTF_8);
        SpiTransaction donorSpiTransaction = new Gson().fromJson(spiTransactionJson, SpiTransaction.class);
        List<SpiTransaction> donorSpiTransactions = new ArrayList<SpiTransaction>();
        donorSpiTransactions.add(donorSpiTransaction);
        SpiTransaction[] expectedBooked = donorSpiTransactions
        .stream()
        .filter(transaction -> transaction.getBookingDate() != null)
        .toArray(SpiTransaction[]::new);

        //When:
        assertNotNull(donorSpiTransaction);
        AccountReport actualAccountReport = accountMapper.mapAccountReport(donorSpiTransactions);

        //Then:
        assertThat(actualAccountReport.getBooked()[0].getTransactionId()).isEqualTo(expectedBooked[0].getTransactionId());
        assertThat(actualAccountReport.getBooked()[0].getBookingDate()).isEqualTo(expectedBooked[0].getBookingDate());
        assertThat(actualAccountReport.getBooked()[0].getCreditorId()).isEqualTo(expectedBooked[0].getCreditorId());
        assertThat(actualAccountReport.getBooked()[0].getCreditorName()).isEqualTo(expectedBooked[0].getCreditorName());
        assertThat(actualAccountReport.getBooked()[0].getDebtorName()).isEqualTo(expectedBooked[0].getDebtorName());
        assertThat(actualAccountReport.getBooked()[0].getEndToEndId()).isEqualTo(expectedBooked[0].getEndToEndId());
        assertThat(actualAccountReport.getBooked()[0].getMandateId()).isEqualTo(expectedBooked[0].getMandateId());
        assertThat(actualAccountReport.getBooked()[0].getRemittanceInformationStructured()).isEqualTo(expectedBooked[0].getRemittanceInformationStructured());
        assertThat(actualAccountReport.getBooked()[0].getRemittanceInformationUnstructured()).isEqualTo(expectedBooked[0].getRemittanceInformationUnstructured());
        assertThat(actualAccountReport.getBooked()[0].getUltimateCreditor()).isEqualTo(expectedBooked[0].getUltimateCreditor());
        assertThat(actualAccountReport.getBooked()[0].getValueDate()).isEqualTo(expectedBooked[0].getValueDate());
        assertThat(actualAccountReport.getBooked()[0].getAmount().getContent()).isEqualTo(expectedBooked[0].getSpiAmount().getContent());
        assertThat(actualAccountReport.getBooked()[0].getAmount().getCurrency()).isEqualTo(expectedBooked[0].getSpiAmount().getCurrency());
        assertThat(actualAccountReport.getBooked()[0].getBankTransactionCodeCode().getCode()).isEqualTo(expectedBooked[0].getBankTransactionCodeCode());
        assertThat(actualAccountReport.getBooked()[0].getPurposeCode().getCode()).isEqualTo(expectedBooked[0].getPurposeCode());
    }
}
