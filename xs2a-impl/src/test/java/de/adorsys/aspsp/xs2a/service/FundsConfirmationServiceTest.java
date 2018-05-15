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

package de.adorsys.aspsp.xs2a.service;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FundsConfirmationServiceTest {
    private final String FUNDS_REQ_DATA = "/json/FundsConfirmationRequestTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final String ACCOUNT_DETAILS_SOURCE = "/json/SpiAccountDetails.json";

    private final Currency EUR = Currency.getInstance("EUR");
    private final String AMOUNT_1000 = "1000.00";
    private final String AMOUNT_1001 = "1001.00";


    @Autowired
    private FundsConfirmationService fundsConfirmationService;

    @MockBean(name = "accountService")
    private AccountService accountService;

    @MockBean(name = "accountMapper")
    private AccountMapper accountMapper;

    @Before
    public void setUp() throws IOException {
        when(accountMapper.mapToSpiAmount(getAmount1000()))
            .thenReturn(getSpiAmount1000());
        when(accountMapper.mapToSpiAmount(getAmount1001()))
            .thenReturn(getSpiAmount1001());
        when(accountService.getSpiAccountDetailsByAccountReference(any()))
            .thenReturn(getSpiAccountDetails());
    }

    @Test
    public void fundsConfirmation_success() throws Exception {
        //Given:
        FundsConfirmationRequest request = readFundsConfirmationRequest();

        //When:
        ResponseObject<FundsConfirmationResponse> actualResponse = fundsConfirmationService.fundsConfirmation(request);

        //Then
        assertThat(actualResponse.getBody().isFundsAvailable()).isEqualTo(true);
    }

    @Test
    public void fundsConfirmation_notEnoughMoney() throws Exception {
        //Given:
        FundsConfirmationRequest request = readFundsConfirmationRequest();
        request.setInstructedAmount(getAmount1001());

        //When:
        ResponseObject<FundsConfirmationResponse> actualResponse = fundsConfirmationService.fundsConfirmation(request);

        //Then
        assertThat(actualResponse.getBody().isFundsAvailable()).isEqualTo(false);
    }

    private FundsConfirmationRequest readFundsConfirmationRequest() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), FundsConfirmationRequest.class);
    }

    private Optional<SpiAccountDetails> getSpiAccountDetails() throws IOException {
        SpiAccountDetails spiAccountDetails = new Gson().fromJson(IOUtils.resourceToString(ACCOUNT_DETAILS_SOURCE, UTF_8), SpiAccountDetails.class);
        return Optional.of(spiAccountDetails);
    }

    private SpiAmount getSpiAmount1000() {
        return new SpiAmount(EUR, AMOUNT_1000);
    }

    private SpiAmount getSpiAmount1001() {
        return new SpiAmount(EUR, AMOUNT_1001);
    }

    private Amount getAmount1000() {
        Amount amount = new Amount();
        amount.setContent(AMOUNT_1000);
        amount.setCurrency(EUR);
        return amount;
    }

    private Amount getAmount1001() {
        Amount amount = new Amount();
        amount.setContent(AMOUNT_1001);
        amount.setCurrency(EUR);
        return amount;
    }
}
