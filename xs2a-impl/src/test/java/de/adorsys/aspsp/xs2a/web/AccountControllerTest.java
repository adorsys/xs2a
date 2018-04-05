package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.service.AccountService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private final String ACCOUNT_ID = "33333-999999999";
    private final String TRANSACTION_ID = "1234578";
    private final String ACCOUNT_DETAILS_SOURCE = "/json/AccountDetailsTestData.json";
    private final String ACCOUNT_REPORT_SOURCE = "/json/AccountReportTestData.json";
    private final String BALANCES_SOURCE = "/json/BalancesTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private AccountController accountController;

    @MockBean(name = "accountService")
    private AccountService accountService;

    @Before
    public void setUp() throws Exception {
        when(accountService.getAccountDetailsList(anyBoolean(), anyBoolean())).thenReturn(createAccountDetailsList(ACCOUNT_DETAILS_SOURCE));
        when(accountService.getBalancesList(any(String.class), anyBoolean()))
        .thenReturn(readBalances());
        when(accountService.getAccountReport(any(String.class), any(Date.class), any(Date.class), any(String.class), anyBoolean(), any(), anyBoolean(), anyBoolean())).thenReturn(createAccountReport(ACCOUNT_REPORT_SOURCE));
        when(accountService.getAccountDetails(any(), anyBoolean(), anyBoolean())).thenReturn(getAccountDetails());
    }

    @Test
    public void getAccountDetails_withBalance() throws IOException {
        //Given
        boolean withBalance = true;
        boolean psuInvolved = true;
        ResponseObject<AccountDetails> expectedResult = getAccountDetails();

        //When
        AccountDetails result = accountController.readAccountDetails(ACCOUNT_ID, withBalance, psuInvolved).getBody();

        assertThat(result).isEqualTo(expectedResult.getData());
    }

    @Test
    public void getAccounts_ResultTest() throws IOException {
        //Given
        boolean withBalance = true;
        boolean psuInvolved = true;
        Map<String,List<AccountDetails>> expectedResult =  createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getData();

        //When:
        Map<String,List<AccountDetails>> result = accountController.getAccounts(withBalance, psuInvolved).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getBallances_ResultTest() throws IOException {
        //Given:
        boolean psuInvolved = true;
        Balances expectedBalances = new Gson().fromJson(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Balances.class);
        List<Balances> expectedResult = new ArrayList<>();
        expectedResult.add(expectedBalances);

        //When:
        List<Balances> result = accountController.getBalances(ACCOUNT_ID, psuInvolved).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getTransactions_ResultTest() throws IOException {
        //Given:
        boolean psuInvolved = true;
        AccountReport expectedResult = new Gson().fromJson(IOUtils.resourceToString(ACCOUNT_REPORT_SOURCE, UTF_8), AccountReport.class);

        //When
        AccountReport result = accountController.getTransactions(ACCOUNT_ID, null, null, TRANSACTION_ID, psuInvolved, "both", false, false).getBody();

        //Then:
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void getAccounts_withBalance() {
        boolean withBalance = true;
        boolean psuInvolved = false;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccounts_noBalances() {
        boolean withBalance = false;
        boolean psuInvolved = false;

        checkAccountResults(withBalance, psuInvolved);
    }

    @Test
    public void getAccounts_withBalanceAndPsuInvolved() {
        boolean withBalance = true;
        boolean psuInvolved = true;

        checkAccountResults(withBalance, psuInvolved);
    }

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
        Date dateFrom = new Date();
        Date dateTo = new Date();
        String transactionId = "";
        boolean psuInvolved = false;

        checkTransactionResults(ACCOUNT_ID, dateFrom, dateTo, transactionId, psuInvolved);
    }

    private void checkTransactionResults(String accountId, Date dateFrom, Date dateTo, String transactionId,
                                         boolean psuInvolved) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        AccountReport expectedResult = accountService.getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, "both", false, false).getData();

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

        List<Balances> expectedResult = accountService.getBalancesList(accountId, psuInvolved).getData();

        //When:
        ResponseEntity<List<Balances>> actualResponse = accountController.getBalances(accountId, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        List<Balances> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        Map<String, List<AccountDetails>> expectedResult = new HashMap<>();
        expectedResult.put("accountList", accountService.getAccountDetailsList(withBalance, psuInvolved).getData().get("accountList"));

        //When:
        ResponseEntity<Map<String, List<AccountDetails>>> actualResponse = accountController.getAccounts(withBalance, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, List<AccountDetails>> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private ResponseObject <Map<String,List<AccountDetails>>>createAccountDetailsList(String path) throws IOException {
        AccountDetails[] array = new Gson().fromJson(IOUtils.resourceToString(path, UTF_8), AccountDetails[].class);
        Map<String,List<AccountDetails>> result = new HashMap<>();
        result.put("accountList",Arrays.asList(array));
        return new ResponseObject<>(result);
    }

    private ResponseObject<AccountDetails> getAccountDetails() throws IOException {
        Map<String,List<AccountDetails>> map = createAccountDetailsList(ACCOUNT_DETAILS_SOURCE).getData();
        return new ResponseObject<>(map.get("accountList").get(0));
    }

    private ResponseObject<AccountReport> createAccountReport(String path) throws IOException {
        AccountReport accountReport = new Gson().fromJson(IOUtils.resourceToString(path, UTF_8), AccountReport.class);

        return new ResponseObject<>(accountReport);
    }

    private ResponseObject<List<Balances>> readBalances() throws IOException {
        Balances readed = new Gson().fromJson(IOUtils.resourceToString(BALANCES_SOURCE, UTF_8), Balances.class);
        List<Balances> res = new ArrayList<>();
        res.add(readed);
        return new ResponseObject<>(res);
    }
}
