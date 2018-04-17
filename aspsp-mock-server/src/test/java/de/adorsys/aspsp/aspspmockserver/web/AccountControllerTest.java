package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {
    private static final String ACCOUNT_ID = "2123sndjk2w23";
    private static final String WRONG_ACCOUNT_ID = "0";

    @MockBean
    private AccountService accountService;
    @Autowired
    private AccountController accountController;


    @Before
    public void setUpAccountServiceMock(){
        List<SpiAccountDetails> accountList = new ArrayList<>();
        accountList.add(getSpiAccountDetails_1());
        accountList.add(getSpiAccountDetails_2());
        when(accountService.getAccount(ACCOUNT_ID))
        .thenReturn(Optional.of(getSpiAccountDetails_1()));
        when(accountService.getAllAccounts())
        .thenReturn(accountList);
        when(accountService.addAccount(getSpiAccountDetails_1()))
        .thenReturn(getSpiAccountDetails_1());
        when(accountService.deleteAccountById(ACCOUNT_ID))
        .thenReturn(true);
        when(accountService.deleteAccountById(WRONG_ACCOUNT_ID))
        .thenReturn(false);
    }


    @Test
    public void readAllAccounts() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        List<SpiAccountDetails> expectedResult = accountService.getAllAccounts();

        //When:
        ResponseEntity<List<SpiAccountDetails>> actualResponse = accountController.readAllAccounts();

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        List<SpiAccountDetails> actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void readAccountById() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        SpiAccountDetails expectedResult = accountService.getAccount(ACCOUNT_ID).get();

        //When:
        ResponseEntity<SpiAccountDetails> actualResponse = accountController.readAccountById(ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        SpiAccountDetails actualResult = actualResponse.getBody();

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void createAccount() throws Exception {
        //Given
        MockHttpServletRequest expectedRequest = new MockHttpServletRequest();
        expectedRequest.setRequestURI("/account/");
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        accountController.createAccount(expectedRequest,expectedSpiAccountDetails);
        SpiAccountDetails actualSpiAccountDetails = accountService.getAccount(ACCOUNT_ID).get();

        //Then
        assertThat(actualSpiAccountDetails).isNotNull();
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void deleteAccount_Success(){
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NO_CONTENT;

        //When:
        ResponseEntity actualResponse = accountController.deleteAccount(ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
    }

    @Test
    public void deleteAccount_WrongId(){
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.NOT_FOUND;

        //When:
        ResponseEntity actualResponse = accountController.deleteAccount(WRONG_ACCOUNT_ID);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
    }

    private SpiAccountDetails getSpiAccountDetails_1(){
        SpiAccountDetails spiAccountDetails = new SpiAccountDetails();
        spiAccountDetails.setId(ACCOUNT_ID);
        spiAccountDetails.setIban("DE12345235431234");
        spiAccountDetails.setBban(null);
        spiAccountDetails.setPan("1111222233334444");
        spiAccountDetails.setMaskedPan("111122xxxxxx44");
        spiAccountDetails.setMsisdn(null);
        spiAccountDetails.setCurrency(Currency.getInstance("EUR"));
        spiAccountDetails.setName("Jack");
        spiAccountDetails.setAccountType("GIRO");
        spiAccountDetails.setCashSpiAccountType(null);
        spiAccountDetails.setBic("XE3DDD");
        spiAccountDetails.setBalances(null);

        return spiAccountDetails;
    }

    private SpiAccountDetails getSpiAccountDetails_2(){
        SpiAccountDetails spiAccountDetails = new SpiAccountDetails();
        spiAccountDetails.setId("qwertyuiop12345678");
        spiAccountDetails.setIban("DE99999999999999");
        spiAccountDetails.setBban(null);
        spiAccountDetails.setPan("4444333322221111");
        spiAccountDetails.setMaskedPan("444433xxxxxx1111");
        spiAccountDetails.setMsisdn(null);
        spiAccountDetails.setCurrency(Currency.getInstance("EUR"));
        spiAccountDetails.setName("Emily");
        spiAccountDetails.setAccountType("GIRO");
        spiAccountDetails.setCashSpiAccountType(null);
        spiAccountDetails.setBic("ACVB222");
        spiAccountDetails.setBalances(null);

        return spiAccountDetails;
    }
}
