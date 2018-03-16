package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.ApiDateConstants;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentResp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    @Autowired
    private ConsentService consentService;
    
    @Test
    public void createAccountConsentsWithResponse_returnCreatedConsent() throws IOException {
        //Given:
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        
        //When:
        CreateConsentResp actualAicResponse = consentService.createAccountConsentsWithResponse(expectedRequest, withBalance, tppRedirectPreferred);
        
        //Then:
        assertThat(actualAicResponse.getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
        
        //Given:
        String consentId = actualAicResponse.getConsentId();
        
        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(consentId);
        //Then:
        assertThat(actualAccountConsents.getAccess()).isEqualTo(expectedRequest.getAccess());
        assertThat(actualAccountConsents.isRecurringIndicator()).isEqualTo(expectedRequest.isRecurringIndicator());
        assertThat(actualAccountConsents.getValidUntil()).isEqualTo(expectedRequest.getValidUntil());
        assertThat(actualAccountConsents.getFrequencyPerDay()).isEqualTo(expectedRequest.getFrequencyPerDay());
    }
    
    @Test
    public void createAccountConsentsAndReturnId_successesResult() {
        //Given:
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        
        //When:
        String actualConsentId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred);
        
        //Then:
        assertThat(actualConsentId).isNotEmpty();
    }
    
    @Test
    public void getAccountConsentsStatusById_successesResult() {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();
        String validAccountConsentsId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred);
        TransactionStatus expectedStatus = TransactionStatus.ACTC;
        
        //When:
        TransactionStatus actualStatus = consentService.getAccountConsentsStatusById(validAccountConsentsId);
        
        //Then:
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }
    
    @Test
    public void getAccountConsentsStatusById_wrongId() {
        //Given:
        String wrongId = "111111";
        
        //When:
        TransactionStatus actualStatus = consentService.getAccountConsentsStatusById(wrongId);
        
        //Then:
        assertThat(actualStatus).isNull();
    }
    
    @Test
    public void getAccountConsentsById_successesResult() {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();
        String validAccountConsentsId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred);
        
        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(validAccountConsentsId);
        
        //Then:
        assertThat(actualAccountConsents.getAccess()).isEqualTo(expectedRequest.getAccess());
        assertThat(actualAccountConsents.isRecurringIndicator()).isEqualTo(expectedRequest.isRecurringIndicator());
        assertThat(actualAccountConsents.getValidUntil()).isEqualTo(expectedRequest.getValidUntil());
        assertThat(actualAccountConsents.getFrequencyPerDay()).isEqualTo(expectedRequest.getFrequencyPerDay());
    }
    
    @Test
    public void getAccountConsentsById_WrongConsentId_shouldReturnEmptyObject() {
        //Given:
        String wrongId = "111111";
        
        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(wrongId);
        //Then:
        assertThat(actualAccountConsents).isNull();
    }
    
    private CreateConsentReq getCreateConsentsRequestTest() {
        
        AccountReference iban1 = new AccountReference();
        iban1.setIban("DE2310010010123456789");
        
        AccountReference iban2 = new AccountReference();
        iban2.setIban("DE2310010010123456790");
        iban2.setCurrency(Currency.getInstance("USD"));
        
        AccountReference iban3 = new AccountReference();
        iban3.setIban("DE2310010010123456788");
        
        AccountReference iban4 = new AccountReference();
        iban4.setIban("DE2310010010123456789");
        
        AccountReference maskedPan = new AccountReference();
        maskedPan.setMaskedPan("123456xxxxxx1234");
        
        AccountReference[] balances = (AccountReference[]) Arrays.asList(iban1, iban2, iban3).toArray();
        AccountReference[] transactions = (AccountReference[]) Arrays.asList(iban4, maskedPan).toArray();
        
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setBalances(balances);
        accountAccess.setTransactions(transactions);
        
        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(getDateFromDateString("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);
        
        return aicRequestObj;
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
