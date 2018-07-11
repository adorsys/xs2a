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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.config.WebConfigTest;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebConfigTest.class)
public class AccountMapperTest {
    private static final String SPI_ACCOUNT_DETAILS_JSON_PATH = "/json/MapSpiAccountDetailsToXs2aAccountDetailsTest.json";
    private static final String SPI_BALANCES_JSON_PATH = "/json/MapSpiBalancesTest.json";
    private static final String SPI_TRANSACTION_JSON_PATH = "/json/AccountReportDataTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private JsonConverter jsonConverter;

    @Test
    public void mapSpiAccountDetailsToXs2aAccountDetails() throws IOException {
        //Given:
        String spiAccountDetailsJson = IOUtils.resourceToString(SPI_ACCOUNT_DETAILS_JSON_PATH, UTF_8);
        SpiAccountDetails donorAccountDetails = jsonConverter.toObject(spiAccountDetailsJson, SpiAccountDetails.class).get();

        //When:
        assertNotNull(donorAccountDetails);
        AccountDetails actualAccountDetails = accountMapper.mapToAccountDetails(donorAccountDetails);

        //Then:
        assertThat(actualAccountDetails.getId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(actualAccountDetails.getIban()).isEqualTo("DE2310010010123456789");
        assertThat(actualAccountDetails.getBban()).isEqualTo("DE2310010010123452343");
        assertThat(actualAccountDetails.getAccountType()).isEqualTo("Girokonto");
        assertThat(actualAccountDetails.getName()).isEqualTo("Main Account");
        assertThat(actualAccountDetails.getCashAccountType()).isEqualTo(CashAccountType.CURRENT_ACCOUNT);
        assertThat(actualAccountDetails.getBic()).isEqualTo("EDEKDEHHXXX");
        SingleBalance closingBooked = actualAccountDetails.getBalances().get(0).getClosingBooked();
        assertThat(closingBooked.getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(closingBooked.getLastActionDateTime()).isEqualTo(LocalDateTime.parse("2017-10-25T15:30:35.035"));
        assertThat(closingBooked.getDate()).isEqualTo(LocalDate.parse("2007-01-01"));
    }

    @Test
    public void mapSpiBalances() throws IOException {
        //Given:
        String spiBalancesJson = IOUtils.resourceToString(SPI_BALANCES_JSON_PATH, UTF_8);
        SpiBalances donorBalances = jsonConverter.toObject(spiBalancesJson, SpiBalances.class).get();
        List<SpiBalances> donorBalancesList = new ArrayList<>();
        donorBalancesList.add(donorBalances);

        //When:
        assertNotNull(donorBalances);
        List<Balances> actualBalances = accountMapper.mapToBalancesList(donorBalancesList);

        //Then:
        assertThat(actualBalances.get(0).getClosingBooked().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getClosingBooked().getLastActionDateTime()).isEqualTo(LocalDateTime.parse("2017-10-25T15:30:35.035"));
        assertThat(actualBalances.get(0).getClosingBooked().getDate()).isEqualTo(LocalDate.parse("2007-01-01"));
        assertThat(actualBalances.get(0).getAuthorised().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getAuthorised().getLastActionDateTime()).isEqualTo(LocalDateTime.parse("2017-10-25T15:30:35.035"));
        assertThat(actualBalances.get(0).getAuthorised().getDate()).isEqualTo(LocalDate.parse("2007-01-01"));
        assertThat(actualBalances.get(0).getExpected().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getExpected().getLastActionDateTime()).isEqualTo(LocalDateTime.parse("2017-10-25T15:30:35.035"));
        assertThat(actualBalances.get(0).getExpected().getDate()).isEqualTo(LocalDate.parse("2007-01-01"));
        assertThat(actualBalances.get(0).getInterimAvailable().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getInterimAvailable().getLastActionDateTime()).isEqualTo(LocalDateTime.parse("2017-10-25T15:30:35.035"));
        assertThat(actualBalances.get(0).getInterimAvailable().getDate()).isEqualTo(LocalDate.parse("2007-01-01"));
        assertThat(actualBalances.get(0).getOpeningBooked().getAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(actualBalances.get(0).getOpeningBooked().getLastActionDateTime()).isEqualTo(LocalDateTime.parse("2017-10-25T15:30:35.035"));
        assertThat(actualBalances.get(0).getOpeningBooked().getDate()).isEqualTo(LocalDate.parse("2007-01-01"));
    }

    @Test
    public void mapAccountReport() throws IOException {
        //Given:
        String spiTransactionJson = IOUtils.resourceToString(SPI_TRANSACTION_JSON_PATH, UTF_8);
        SpiTransaction donorSpiTransaction = jsonConverter.toObject(spiTransactionJson, SpiTransaction.class).get();
        List<SpiTransaction> donorSpiTransactions = new ArrayList<>();
        donorSpiTransactions.add(donorSpiTransaction);
        SpiTransaction[] expectedBooked = donorSpiTransactions.stream()
                                          .filter(transaction -> transaction.getBookingDate() != null)
                                          .toArray(SpiTransaction[]::new);

        //When:
        assertNotNull(donorSpiTransaction);
        Optional<AccountReport> aAR = accountMapper.mapToAccountReport(donorSpiTransactions);
        AccountReport actualAccountReport;
        actualAccountReport = aAR.orElseGet(() -> new AccountReport(new Transactions[]{}, new Transactions[]{}));


        //Then:
        assertThat(actualAccountReport.getBooked()[0].getTransactionId())
            .isEqualTo(expectedBooked[0].getTransactionId());
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
        assertThat(actualAccountReport.getBooked()[0].getAmount().getContent()).isEqualTo(expectedBooked[0].getSpiAmount()
        .getContent().toString());
        assertThat(actualAccountReport.getBooked()[0].getAmount().getCurrency()).isEqualTo(expectedBooked[0].getSpiAmount()
        .getCurrency());
        assertThat(actualAccountReport.getBooked()[0].getBankTransactionCodeCode()
        .getCode()).isEqualTo(expectedBooked[0].getBankTransactionCodeCode());
        assertThat(actualAccountReport.getBooked()[0].getPurposeCode().getCode()).isEqualTo(expectedBooked[0].getPurposeCode());
    }
}
