package de.adorsys.aspsp.xs2a.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.ApiDateConstants;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountAccessType;
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
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentInformationControllerTest {
    private final ObjectMapper MAPPER = new ObjectMapper();
    private final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private final String CREATE_CONSENT_ALL_REQ_JSON_PATH = "/json/CreateConsentsAllAccountsAvailableReqTest.json";
    private final String CREATE_CONSENT_NOSELECTED_REQ_JSON_PATH = "/json/CreateConsentsNoDedicateAccountReqTest.json";
    private final String CREATE_CONSENT_ALLPSD2_REQ_JSON_PATH = "/json/CreateConsentsPSD2AllAccountsAvailableReqTest.json";

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
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, Charset.forName("utf-8"));
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
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, Charset.forName("utf-8"));
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
    public void shouldFail_getAccountConsentsStatusById_wrongId() {
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
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, Charset.forName("utf-8"));
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
    public void getAccountConsentsInformationById_wrongId_shouldReturnEmptyObject() {
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

    @Test
    public void deleteAccountConsent_correctId() throws IOException {
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_ALL_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        ResponseEntity<Void> actualResponse = consentInformationController.deleteAccountConsent(consentId);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void createAccountConsent_availableAccounts() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_ALL_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsents.getId()).isNotNull();
        assertThat(actualAccountConsents.getAccess()).isNotNull();
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getAccounts(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getBalances(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getTransactions(), new AccountReference[]{}));
        assertThat(actualAccountConsents.getAccess().getAvailableAccounts()).isEqualTo(AccountAccessType.ALL_ACCOUNTS);
        assertThat(actualAccountConsents.getAccess().getAllPsd2()).isNull();
        assertThat(actualAccountConsents.isRecurringIndicator()).isFalse();
        assertThat(actualAccountConsents.getValidUntil().compareTo(getDateFromDateString("2017-08-06")));
    }

    @Test
    public void createAccountConsent_allPsd2() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_ALLPSD2_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsents.getId()).isNotNull();
        assertThat(actualAccountConsents.getAccess()).isNotNull();
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getAccounts(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getBalances(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getTransactions(), new AccountReference[]{}));
        assertThat(actualAccountConsents.getAccess().getAccounts()).isNull();
        assertThat(actualAccountConsents.getAccess().getAllPsd2()).isEqualTo(AccountAccessType.ALL_ACCOUNTS);
        assertThat(actualAccountConsents.isRecurringIndicator()).isFalse();
        assertThat(actualAccountConsents.getValidUntil().compareTo(getDateFromDateString("2017-07-11")));
    }

    @Test
    public void createConsentForAllAvailableAccountsNoIndication_withBalanceAndTppRedirect() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_NOSELECTED_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        AccountConsents actualAccountConsents = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsents.getId()).isNotNull();
        assertThat(actualAccountConsents.getAccess()).isNotNull();
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getAccounts(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getBalances(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsents.getAccess().getTransactions(), new AccountReference[]{}));
        assertThat(actualAccountConsents.getAccess().getAvailableAccounts()).isNull();
        assertThat(actualAccountConsents.getAccess().getAllPsd2()).isNull();
        assertThat(actualAccountConsents.isRecurringIndicator()).isTrue();
        assertThat(actualAccountConsents.getValidUntil().compareTo(getDateFromDateString("2017-11-01")));
    }

    private CreateConsentResp getCreateConsentRespEntityFromController(String path, boolean withBalance, boolean tppRedirectPreferred) throws IOException {
        // Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String requestJson = IOUtils.resourceToString(path, Charset.forName("utf-8"));
        CreateConsentReq createConsentReq = MAPPER.readValue(requestJson, CreateConsentReq.class);

        // When:
        ResponseEntity<CreateConsentResp> actualResponse = consentInformationController.createAccountConsent(withBalance, tppRedirectPreferred, createConsentReq);

        // Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);

        CreateConsentResp actualResult = actualResponse.getBody();
        return actualResult;
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
