package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {
    @Autowired
    private AccountService accountService;

    @Test
    public void addAccount() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();

        //When
        SpiAccountDetails actualSpiAccountDetails = accountService.addAccount(expectedSpiAccountDetails);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAllAccounts() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails1 = getSpiAccountDetails_1();
        SpiAccountDetails expectedSpiAccountDetails2 = getSpiAccountDetails_2();
        accountService.addAccount(expectedSpiAccountDetails1);
        accountService.addAccount(expectedSpiAccountDetails2);

        //When
        List<SpiAccountDetails> actualListSpiAccountDetails = accountService.getAllAccounts();

        //Then
        assertThat(actualListSpiAccountDetails).isNotNull();
        assertThat(actualListSpiAccountDetails.get(0)).isEqualTo(expectedSpiAccountDetails1);
        assertThat(actualListSpiAccountDetails.get(1)).isEqualTo(expectedSpiAccountDetails2);
    }

    @Test
    public void getAccount_Success() {
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        String spiAccountDetailsId = expectedSpiAccountDetails.getId();
        accountService.addAccount(expectedSpiAccountDetails);

        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccount(spiAccountDetailsId);

        //Then
        assertThat(actualSpiAccountDetails).isNotNull();
        assertThat(actualSpiAccountDetails.get()).isEqualTo(expectedSpiAccountDetails);
    }

    @Test
    public void getAccount_WrongId() {
        //Given
        String wrongId = "Really wrong id";
        accountService.addAccount(getSpiAccountDetails_1());

        //When
        Optional<SpiAccountDetails> actualSpiAccountDetails = accountService.getAccount(wrongId);

        //Then
        assertThat(actualSpiAccountDetails).isEqualTo(Optional.empty());
    }

    @Test
    public void deleteAccountById_Success(){
        //Given
        SpiAccountDetails expectedSpiAccountDetails = getSpiAccountDetails_1();
        String spiAccountDetailsId = expectedSpiAccountDetails.getId();
        accountService.addAccount(expectedSpiAccountDetails);

        //When
        boolean actualResult = accountService.deleteAccountById(spiAccountDetailsId);

        //Then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void deleteAccountById_WrongId() {
        //Given
        String wrongId = "Really wrong id";

        //When
        boolean actualResult  = accountService.deleteAccountById(wrongId);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void deleteAccountById_Null() {
        //Given
        String wrongId = null;

        //When
        boolean actualResult  = accountService.deleteAccountById(wrongId);

        //Then
        assertThat(actualResult).isFalse();
    }

    @Test
    public void getBalances() {
        //Given
        SpiAccountDetails spiAccountDetails = getSpiAccountDetails_1();
        String spiAccountDetailsId = spiAccountDetails.getId();
        List<SpiBalances> expectedBalance = getNewBalanceList();
        spiAccountDetails.setBalances(expectedBalance);
        accountService.addAccount(spiAccountDetails);

        //When
        Optional<List<SpiBalances>> actualBalanceList = accountService.getBalances(spiAccountDetailsId);

        //Then
        assertThat(actualBalanceList.get()).isEqualTo(expectedBalance);
    }

    private SpiAccountDetails getSpiAccountDetails_1(){
        SpiAccountDetails spiAccountDetails = new SpiAccountDetails();
        spiAccountDetails.setId("21fefdsdvds212sa");
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

    private List<SpiBalances> getNewBalanceList() {
        Currency euro = Currency.getInstance("EUR");

        SpiBalances balance = new SpiBalances();
        balance.setAuthorised(getNewSingleBalances(new SpiAmount(euro, "1000")));
        balance.setOpeningBooked(getNewSingleBalances(new SpiAmount(euro, "200")));

        return Collections.singletonList(balance);
    }

    private SpiAccountBalance getNewSingleBalances(SpiAmount spiAmount) {
        SpiAccountBalance sb = new SpiAccountBalance();
        sb.setDate(new Date(1523951451537L));
        sb.setSpiAmount(spiAmount);
        sb.setLastActionDateTime(new Date(1523951451537L));
        return sb;
    }
}
