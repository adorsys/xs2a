package de.adorsys.aspsp.xs2a.domain.consents;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.utils.DateUtil;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Currency;

import static de.adorsys.aspsp.xs2a.spi.utils.FileUtil.readFromInputStream;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsentModelsTest {
    private final String AIC_REQUEST_PATH = "json/AccountInformationConsentRequestTest.json";
    
    @Test
    public void accountInformationConsentRequest_jsonTest() throws IOException {
        //Given:
        String aicRequestJson = getJsonString(AIC_REQUEST_PATH);
        CreateConsentReq expectedAICRequest = getAICRequestTest();
        
        //When:
        CreateConsentReq actualAICRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        
        //Then:
        assertThat(actualAICRequest).isEqualTo(expectedAICRequest);
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
    
    public String getJsonString(String filePath) throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        
        return readFromInputStream(is);
        
    }
}
