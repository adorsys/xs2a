package de.adorsys.aspsp.xs2a.domain.consents;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.ApiDateConstants;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountConsentsModelsTest {
    private final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private final String ALL_ACCOUNTS_AVAILABLE_REQ_PATH = "/json/CreateConsentsAllAccountsAvailableReqTest.json";
    private final String NO_DEDICATE_REQ_PATH = "/json/CreateConsentsNoDedicateAccountReqTest.json";
    private ObjectMapper mapper = new ObjectMapper();
    @Test
    public void createConsentReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = getStringFromFile(CREATE_CONSENT_REQ_JSON_PATH);
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();
        
        //When:
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);
        
        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }
    
    @Test
    public void createConsentAllAccountsAvailableReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = getStringFromFile(ALL_ACCOUNTS_AVAILABLE_REQ_PATH);
        CreateConsentReq expectedRequest = getAicAvailableAccountsRequest();
        
        //When:
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);
 
         //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }
    
    @Test
    public void createConsentNoDedicateAccountReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = getStringFromFile(NO_DEDICATE_REQ_PATH);
        CreateConsentReq expectedRequest = getAicNoDedicatedAccountRequest();
        
        //When:
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);
        
        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }
    
    private CreateConsentReq getAicNoDedicatedAccountRequest() {
        
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setBalances(new AccountReference[0]);
        accountAccess.setTransactions(new AccountReference[0]);
        
        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(getDateFromDateString("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);
        
        return aicRequestObj;
    }
    
    private CreateConsentReq getAicAvailableAccountsRequest() {
        
        AccountAccess accountAccess = new AccountAccess();
        accountAccess.setAvailableAccounts(AccountAccessType.ALL_ACCOUNTS);
        
        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(false);
        aicRequestObj.setValidUntil(getDateFromDateString("2017-08-06"));
        aicRequestObj.setFrequencyPerDay(1);
        
        return aicRequestObj;
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
    
    private String getStringFromFile(String pathToFile) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(pathToFile);
        
        return (String) IOUtils.readLines(inputStream).stream()
                        .collect(Collectors.joining());
    }
}
