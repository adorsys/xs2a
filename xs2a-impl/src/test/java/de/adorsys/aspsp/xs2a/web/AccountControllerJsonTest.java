package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.service.AccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerJsonTest {

    @Autowired
    private AccountController accountController;

    @MockBean
    private AccountService accountServiceMocked;

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

    private void checkAccountResults(boolean withBalance, boolean psuInvolved) {

        //Given:
        AccountDetails accountDetails = new AccountDetails(
        "21fef",
        "DE1234523543",
        null,
        null,
        null,
        null,
        Currency.getInstance("EUR"),
        "name",
        "GIRO",
        null,
        "XE3DDD",
        null,
        null
        );
        List<AccountDetails> accountDetailsList = new ArrayList<>();
        accountDetailsList.add(accountDetails);

        HttpStatus expectedStatusCode = HttpStatus.OK;
        Map<String, List<AccountDetails>> expectedResult = new HashMap<>();
        expectedResult.put("accountList", accountDetailsList);

        when(accountServiceMocked.getAccountDetailsList(withBalance, psuInvolved))
        .thenReturn(Collections.singletonList(accountDetails));

        //When:
        ResponseEntity<Map<String, List<AccountDetails>>> actualResponse = accountController.getAccounts(withBalance, psuInvolved);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, List<AccountDetails>> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);

    }
}
