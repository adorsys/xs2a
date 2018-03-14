package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccess;
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
    public void createAicRequest_returnCreatedConsent() throws IOException {
        //Given:
        CreateConsentReq expectedAicRequest = getAICRequestTest();
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        
        //When:
        CreateConsentResp actualAicResponse = consentService.createAicRequest(expectedAicRequest, withBalance, tppRedirectPreferred);
        
        //Then:
        assertThat(actualAicResponse.getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
        
        //Given:
        String consentId = actualAicResponse.getConsentId();
        
        //When:
        CreateConsentReq actualAicRequest = consentService.getAicRequest(consentId);
        //Then:
        assertThat(actualAicRequest).isEqualTo(expectedAicRequest);
    }
    
    @Test
    public void shouldFail_getAicRequest_WrongConsentId() throws IOException {
        //Given:
        String wrongId = "111111";
        
        //When:
        CreateConsentReq actualAicRequest = consentService.getAicRequest(wrongId);
        //Then:
        assertThat(actualAicRequest).isNull();
    }
    
    private CreateConsentReq getAICRequestTest() {
        
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
