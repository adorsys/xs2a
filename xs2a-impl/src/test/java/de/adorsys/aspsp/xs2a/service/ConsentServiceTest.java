package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentResp;
import de.adorsys.aspsp.xs2a.spi.utils.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    @Autowired
    private ConsentService consentService;
    
    @Test
    public void createAccountConsentsWithResponse_returnCreatedConsent() throws IOException {
        //Given:
        CreateConsentReq expectedAicRequest = getCreateConsentsRequestTest();
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        
        //When:
        CreateConsentResp actualAicResponse = consentService.createAccountConsentsWithResponse(expectedAicRequest, withBalance, tppRedirectPreferred);
        
        //Then:
        assertThat(actualAicResponse.getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
        
        //Given:
        String consentId = actualAicResponse.getConsentId();
        
        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(consentId);
        //Then:
        assertThat(actualAccountConsents.getAccess()).isEqualTo(expectedAicRequest.getAccess());
        assertThat(actualAccountConsents.isRecurringIndicator()).isEqualTo(expectedAicRequest.isRecurringIndicator());
        assertThat(actualAccountConsents.getValidUntil()).isEqualTo(expectedAicRequest.getValidUntil());
        assertThat(actualAccountConsents.getFrequencyPerDay()).isEqualTo(expectedAicRequest.getFrequencyPerDay());
    }
    
    @Test
    public void shouldReturnEmptyObject_getAccountConsentsById_WrongConsentId() throws IOException {
        //Given:
        String wrongId = "111111";
        
        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(wrongId);
        //Then:
        assertThat(actualAccountConsents.getId()).isNull();
        assertThat(actualAccountConsents.getAccess()).isNull();
        assertThat(actualAccountConsents.isRecurringIndicator()).isFalse();
        assertThat(actualAccountConsents.getValidUntil()).isNull();
        assertThat(actualAccountConsents.getFrequencyPerDay()).isEqualTo(0);
        assertThat(actualAccountConsents.getLastActionDate()).isNull();
        assertThat(actualAccountConsents.getTransactionStatus()).isNull();
        assertThat(actualAccountConsents.getConsentStatus()).isNull();
        assertThat(actualAccountConsents.isWithBalance()).isFalse();
        assertThat(actualAccountConsents.isTppRedirectPreferred()).isFalse();
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
        aicRequestObj.setValidUntil(DateUtil.getDateFromDateStringNoTimeZone("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);
        
        return aicRequestObj;
    }
}
