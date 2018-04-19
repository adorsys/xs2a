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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.web.util.ApiDateConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String TRANSACTION_ID = "1234578";

    @Autowired
    private AccountController accountController;

    @Autowired
    private AccountService accountService;

    @Test
    public void getBalance_withPsuInvolved() {
        //Given:
        boolean psuInvolved = true;
        checkBalanceResults(ACCOUNT_ID, psuInvolved);
    }

    @Test
    public void getBalance_noPsuInvolved() {
        //Given:
        boolean psuInvolved = false;
        checkBalanceResults(ACCOUNT_ID, psuInvolved);
    }

    @Test
    public void shouldFail_getBalance_emptyAccountWithBalanceAndPsuInvolved() {
        //Given:
        String accountId = "";
        boolean psuInvolved = true;

        checkBalanceResults(accountId, psuInvolved);
    }

    @Test
    public void getTransactions_withPeriodAndTransactionIdNoPsuInvolved() {
        //Given:
        Date dateFrom = new Date();
        Date dateTo = new Date();
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, TRANSACTION_ID, psuInvolved);
    }

    @Test
    public void getTransactions_onlyByPeriod() {
        //Given:
        Date dateFrom = getDateFromDateString("2017-12-12");
        Date dateTo = getDateFromDateString("2018-12-12");
        String transactionId = "";
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, transactionId, psuInvolved);
    }

    @Test
    public void shouldFail_getTransactions_noTransactionIdNoPsuInvolved() {
        //Given:
        String transactionId = "";
        boolean psuInvolved = false;
        HttpStatus expectedStatusCode = HttpStatus.BAD_REQUEST;

        //When:
        ResponseEntity actualResponse = accountController.getTransactions(ACCOUNT_ID,null, null, transactionId, psuInvolved, "both", false, false);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isInstanceOf(MessageError.class);

        MessageError messageError = (MessageError) actualResponse.getBody();

        assertThat(messageError.getTppMessage().getCategory()).isEqualTo(MessageCategory.ERROR);
        assertThat(messageError.getTppMessage().getCode()).isEqualTo(MessageCode.FORMAT_ERROR);
    }

    private void checkTransactionResults(String accountId, Date dateFrom, Date dateTo, String transactionId,
                                         boolean psuInvolved) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        AccountReport expectedResult = accountService.getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false).getBody();

        //When:
        ResponseEntity<AccountReport> actualResponse = accountController.getTransactions(accountId, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountReport actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkBalanceResults(String accountId, boolean psuInvolved) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        List<Balances> expectedResult = accountService.getBalancesList(accountId, psuInvolved).getBody();

        //When:
        ResponseEntity<List<Balances>> actualResponse = accountController.getBalances(accountId, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        List<Balances> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }
    private static Date getDateFromDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ApiDateConstants.DATE_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
