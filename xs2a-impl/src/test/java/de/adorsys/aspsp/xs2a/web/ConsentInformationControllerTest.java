package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentResp;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentInformationControllerTest {
    private final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";

    @Autowired
    private ConsentInformationController consentInformationController;
    @Autowired
    private ConsentService consentService;

    @Test
    public void createConsentForAccounts_withBalanceAndTppRedirect() throws IOException {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        String aicRequestJson = getStringFromFile(CREATE_CONSENT_REQ_JSON_PATH);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);

        //When:
        ResponseEntity<CreateConsentResp> actualResponse = consentInformationController.createAccountConsent(withBalance, tppRedirectPreferred, expectedRequest);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        CreateConsentResp actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);

        //Given:
        String consentId = actualResult.getConsentId();

        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsents.getAccess()).isEqualTo(expectedRequest.getAccess());
        assertThat(actualAccountConsents.isRecurringIndicator()).isEqualTo(expectedRequest.isRecurringIndicator());
        assertThat(actualAccountConsents.getValidUntil()).isEqualTo(expectedRequest.getValidUntil());
        assertThat(actualAccountConsents.getFrequencyPerDay()).isEqualTo(expectedRequest.getFrequencyPerDay());
    }

    @Test
    public void getAccountConsentsStatusById_successesResult() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String aicRequestJson = getStringFromFile(CREATE_CONSENT_REQ_JSON_PATH);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        String accountConsentsId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred);
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", TransactionStatus.ACTC);

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = consentInformationController.getAccountConsentsStatusById(accountConsentsId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, TransactionStatus> actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void shouldFail_getAccountConsentsStatusById_wrongId() throws IOException {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", null);
        String wrongId = "111111";

        //When:
        ResponseEntity<Map<String, TransactionStatus>> actualResponse = consentInformationController.getAccountConsentsStatusById(wrongId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        Map<String, TransactionStatus> actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    public void getAccountConsentsInformationById_successesResult() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String aicRequestJson = getStringFromFile(CREATE_CONSENT_REQ_JSON_PATH);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        String accountConsentsId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred);

        //When:
        ResponseEntity<AccountConsents> actualResponse = consentInformationController.getAccountConsentsInformationById(accountConsentsId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountConsents actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getAccess()).isEqualTo(expectedRequest.getAccess());
        assertThat(actualResult.isRecurringIndicator()).isEqualTo(expectedRequest.isRecurringIndicator());
        assertThat(actualResult.getValidUntil()).isEqualTo(expectedRequest.getValidUntil());
        assertThat(actualResult.getFrequencyPerDay()).isEqualTo(expectedRequest.getFrequencyPerDay());
    }

    @Test
    public void getAccountConsentsInformationById_wrongId_shouldReturnEmptyObject() throws IOException {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", null);
        String wrongId = "111111";

        //When:
        ResponseEntity<AccountConsents> actualResponse = consentInformationController.getAccountConsentsInformationById(wrongId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountConsents actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult).isNull();
    }

    private String getStringFromFile(String pathToFile) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(pathToFile);

        return (String) IOUtils.readLines(inputStream).stream()
                        .collect(Collectors.joining());
    }

    @Test
    public void deleteAccountConsent_correctId() throws IOException {
        //Given:
        ResponseEntity<CreateConsentResp> createdObjectResponse = createConsentRespResponseEntity();
        String consentId = createdObjectResponse.getBody().getConsentId();

        //When:
        ResponseEntity<Void> actualResponse = consentInformationController.deleteAccountConsent(consentId);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<CreateConsentResp> createConsentRespResponseEntity() throws IOException {
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        String aicRequestJson = getStringFromFile(CREATE_CONSENT_REQ_JSON_PATH);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
       return consentInformationController.createAccountConsent(withBalance, tppRedirectPreferred, expectedRequest);
    }
}
