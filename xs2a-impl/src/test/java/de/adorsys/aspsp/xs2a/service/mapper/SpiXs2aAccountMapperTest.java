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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.CashAccountType;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiXs2aAccountMapper;
import de.adorsys.aspsp.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.aspsp.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiExchangeRate;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class SpiXs2aAccountMapperTest {
    private static final String SPI_ACCOUNT_DETAILS_JSON_PATH = "/json/MapSpiAccountDetailsToXs2aAccountDetailsTest.json";
    private static final String SPI_TRANSACTION_JSON_PATH = "/json/AccountReportDataTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");

    @InjectMocks
    private SpiXs2aAccountMapper spiXs2aAccountMapper;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Test
    public void mapSpiAccountDetailsToXs2aAccountDetails() throws IOException {
        //Given:
        String spiAccountDetailsJson = IOUtils.resourceToString(SPI_ACCOUNT_DETAILS_JSON_PATH, UTF_8);
        SpiAccountDetails donorAccountDetails = jsonConverter.toObject(spiAccountDetailsJson, SpiAccountDetails.class).get();

        //When:
        assertNotNull(donorAccountDetails);
        Xs2aAccountDetails actualAccountDetails = spiXs2aAccountMapper.mapToXs2aAccountDetails(donorAccountDetails);

        //Then:
        assertThat(actualAccountDetails.getId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(actualAccountDetails.getIban()).isEqualTo("DE2310010010123456789");
        assertThat(actualAccountDetails.getBban()).isEqualTo("DE2310010010123452343");
        assertThat(actualAccountDetails.getPan()).isEqualTo("1111222233334444");
        assertThat(actualAccountDetails.getMaskedPan()).isEqualTo("111122xxxxxx4444");
        assertThat(actualAccountDetails.getMsisdn()).isEqualTo("4905123123");
        assertThat(actualAccountDetails.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(actualAccountDetails.getName()).isEqualTo("Main Account");
        assertThat(actualAccountDetails.getProduct()).isEqualTo("Girokonto");
        assertThat(actualAccountDetails.getCashAccountType()).isEqualTo(CashAccountType.CACC);
        assertThat(actualAccountDetails.getAccountStatus()).isEqualTo(AccountStatus.ENABLED);
        assertThat(actualAccountDetails.getBic()).isEqualTo("EDEKDEHHXXX");
        assertThat(actualAccountDetails.getUsageType()).isEqualTo(Xs2aUsageType.PRIV);
        assertThat(actualAccountDetails.getDetails()).isEqualTo("Some details");
        assertThat(actualAccountDetails.getBalances()).isEqualTo(Collections.emptyList());
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
        Optional<Xs2aAccountReport> aAR = spiXs2aAccountMapper.mapToXs2aAccountReport(donorSpiTransactions);
        Xs2aAccountReport actualAccountReport;
        actualAccountReport = aAR.orElseGet(() -> new Xs2aAccountReport(new Transactions[]{}, new Transactions[]{}));
        Transactions actualTransaction = actualAccountReport.getBooked()[0];
        SpiTransaction expectedTransaction = expectedBooked[0];

        //Then:
        assertThat(actualTransaction.getTransactionId()).isEqualTo(expectedTransaction.getTransactionId());
        assertThat(actualTransaction.getEntryReference()).isEqualTo(expectedTransaction.getEntryReference());
        assertThat(actualTransaction.getEndToEndId()).isEqualTo(expectedTransaction.getEndToEndId());
        assertThat(actualTransaction.getMandateId()).isEqualTo(expectedTransaction.getMandateId());
        assertThat(actualTransaction.getCheckId()).isEqualTo(expectedTransaction.getCheckId());
        assertThat(actualTransaction.getCreditorId()).isEqualTo(expectedTransaction.getCreditorId());
        assertThat(actualTransaction.getBookingDate()).isEqualTo(expectedTransaction.getBookingDate());
        assertThat(actualTransaction.getValueDate()).isEqualTo(expectedTransaction.getValueDate());

        assertThat(actualTransaction.getAmount().getAmount())
            .isEqualTo(expectedTransaction.getSpiAmount().getAmount().toString());
        assertThat(actualTransaction.getAmount().getCurrency())
            .isEqualTo(expectedTransaction.getSpiAmount().getCurrency());

        Xs2aExchangeRate actualRate = actualTransaction.getExchangeRate().get(0);
        SpiExchangeRate expectedRate = expectedTransaction.getExchangeRate().get(0);
        assertThat(actualRate.getCurrencyFrom()).isEqualTo(expectedRate.getCurrencyFrom());
        assertThat(actualRate.getRateFrom()).isEqualTo(expectedRate.getRateFrom());
        assertThat(actualRate.getCurrencyTo()).isEqualTo(expectedRate.getCurrencyTo());
        assertThat(actualRate.getRateTo()).isEqualTo(expectedRate.getRateTo());
        assertThat(actualRate.getRateDate()).isEqualTo(expectedRate.getRateDate());
        assertThat(actualRate.getRateContract()).isEqualTo(expectedRate.getRateContract());

        assertThat(actualTransaction.getCreditorName()).isEqualTo(expectedTransaction.getCreditorName());
        assertAccountReferences(actualTransaction.getCreditorAccount(), expectedTransaction.getCreditorAccount());
        assertThat(actualTransaction.getUltimateCreditor()).isEqualTo(expectedTransaction.getUltimateCreditor());
        assertThat(actualTransaction.getDebtorName()).isEqualTo(expectedTransaction.getDebtorName());
        assertAccountReferences(actualTransaction.getDebtorAccount(), expectedTransaction.getDebtorAccount());
        assertThat(actualTransaction.getUltimateDebtor()).isEqualTo(expectedTransaction.getUltimateDebtor());
        assertThat(actualTransaction.getRemittanceInformationStructured())
            .isEqualTo(expectedTransaction.getRemittanceInformationStructured());
        assertThat(actualTransaction.getRemittanceInformationUnstructured())
            .isEqualTo(expectedTransaction.getRemittanceInformationUnstructured());
        assertThat(actualTransaction.getPurposeCode().getCode()).isEqualTo(expectedTransaction.getPurposeCode());
        assertThat(actualTransaction.getBankTransactionCodeCode().getCode())
            .isEqualTo(expectedTransaction.getBankTransactionCodeCode());
        assertThat(actualTransaction.getProprietaryBankTransactionCode())
            .isEqualTo(expectedTransaction.getProprietaryBankTransactionCode());
    }

    private void assertAccountReferences(Xs2aAccountReference xs2aAccountReference,
                                     SpiAccountReference spiAccountReference) {
        assertThat(xs2aAccountReference.getIban()).isEqualTo(spiAccountReference.getIban());
        assertThat(xs2aAccountReference.getBban()).isEqualTo(spiAccountReference.getBban());
        assertThat(xs2aAccountReference.getPan()).isEqualTo(spiAccountReference.getPan());
        assertThat(xs2aAccountReference.getMaskedPan()).isEqualTo(spiAccountReference.getMaskedPan());
        assertThat(xs2aAccountReference.getMsisdn()).isEqualTo(spiAccountReference.getMsisdn());
        assertThat(xs2aAccountReference.getCurrency()).isEqualTo(spiAccountReference.getCurrency());
    }
}
