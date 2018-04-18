package de.adorsys.aspsp.xs2a.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.web.util.ApiDateConstants;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentInformationControllerTest {
    private final ObjectMapper MAPPER = new ObjectMapper();
    private final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private final String CREATE_CONSENT_ALL_REQ_JSON_PATH = "/json/CreateConsentsAllAccountsAvailableReqTest.json";
    private final String CREATE_CONSENT_NOSELECTED_REQ_JSON_PATH = "/json/CreateConsentsNoDedicateAccountReqTest.json";
    private final String CREATE_CONSENT_ALLPSD2_REQ_JSON_PATH = "/json/CreateConsentsPSD2AllAccountsAvailableReqTest.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final String PSU_ID = "123456789";
    private final String CONSENT_ID = "987654321";

    @Autowired
    private ConsentInformationController consentInformationController;

    @MockBean(name = "consentService")
    private ConsentService consentService;

    @Before
    public void setUp() {
        when(consentService.createAccountConsentsWithResponse(any(), anyBoolean(), anyBoolean(), anyString())).thenReturn(createConsentResponse());
    }

    @Test
    public void createAccountConsentTest() throws IOException {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.CREATED;
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);

        //When:
        ResponseEntity<CreateConsentResp> actualResponse = consentInformationController.createAccountConsent(PSU_ID, withBalance, tppRedirectPreferred, expectedRequest);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        CreateConsentResp actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    public void getAccountConsentsStatusById_successesResult() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        String accountConsentsId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred, PSU_ID);
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
        HttpStatus expectedStatusCode = HttpStatus.FORBIDDEN;
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
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq expectedRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        String accountConsentsId = consentService.createAccountConsentsAndReturnId(expectedRequest, withBalance, tppRedirectPreferred, PSU_ID);

        //When:
        ResponseEntity<AccountConsent> actualResponse = consentInformationController.getAccountConsentsInformationById(accountConsentsId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountConsent actualResult = actualResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getAccess()).isEqualTo(expectedRequest.getAccess());
        assertThat(actualResult.isRecurringIndicator()).isEqualTo(expectedRequest.isRecurringIndicator());
        assertThat(actualResult.getValidUntil()).isEqualTo(expectedRequest.getValidUntil());
        assertThat(actualResult.getFrequencyPerDay()).isEqualTo(expectedRequest.getFrequencyPerDay());
    }

    @Test
    public void shouldFail_getAccountConsentsInformationById_wrongId_shouldReturnEmptyObject() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.FORBIDDEN;
        Map<String, TransactionStatus> expectedResult = new HashMap<>();
        expectedResult.put("transactionStatus", null);
        String wrongId = "111111";

        //When:
        ResponseEntity<AccountConsent> actualResponse = consentInformationController.getAccountConsentsInformationById(wrongId);

        //Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        AccountConsent actualResult = actualResponse.getBody();
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
    public void deleteAccountConsent_wrongId() throws IOException {
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;

        String wrongConsentId = "SomeWrongId";

        //When:
        ResponseEntity<Void> actualResponse = consentInformationController.deleteAccountConsent(wrongConsentId);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createAccountConsent_availableAccounts() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_ALL_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        AccountConsent actualAccountConsent = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsent.getId()).isNotNull();
        assertThat(actualAccountConsent.getAccess()).isNotNull();
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getAccounts(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getBalances(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getTransactions(), new AccountReference[]{}));
        assertThat(actualAccountConsent.getAccess().getAvailableAccounts()).isEqualTo(AccountAccessType.ALL_ACCOUNTS);
        assertThat(actualAccountConsent.getAccess().getAllPsd2()).isNull();
        assertThat(actualAccountConsent.isRecurringIndicator()).isFalse();
        assertThat(actualAccountConsent.getValidUntil().compareTo(getDateFromDateString("2017-08-06")));
    }

    @Test
    public void createAccountConsent_allPsd2() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_ALLPSD2_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        AccountConsent actualAccountConsent = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsent.getId()).isNotNull();
        assertThat(actualAccountConsent.getAccess()).isNotNull();
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getAccounts(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getBalances(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getTransactions(), new AccountReference[]{}));
        assertThat(actualAccountConsent.getAccess().getAccounts()).isNull();
        assertThat(actualAccountConsent.getAccess().getAllPsd2()).isEqualTo(AccountAccessType.ALL_ACCOUNTS);
        assertThat(actualAccountConsent.isRecurringIndicator()).isFalse();
        assertThat(actualAccountConsent.getValidUntil().compareTo(getDateFromDateString("2017-07-11")));
    }

    @Test
    public void createConsentForAllAvailableAccountsNoIndication_withBalanceAndTppRedirect() throws IOException {
        //Given:
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;
        CreateConsentResp consentRequest = getCreateConsentRespEntityFromController(CREATE_CONSENT_NOSELECTED_REQ_JSON_PATH, withBalance, tppRedirectPreferred);
        String consentId = consentRequest.getConsentId();

        //When:
        AccountConsent actualAccountConsent = consentService.getAccountConsentsById(consentId);

        //Then:
        assertThat(actualAccountConsent.getId()).isNotNull();
        assertThat(actualAccountConsent.getAccess()).isNotNull();
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getAccounts(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getBalances(), new AccountReference[]{}));
        assertThat(Arrays.equals(actualAccountConsent.getAccess().getTransactions(), new AccountReference[]{}));
        assertThat(actualAccountConsent.getAccess().getAvailableAccounts()).isNull();
        assertThat(actualAccountConsent.getAccess().getAllPsd2()).isNull();
        assertThat(actualAccountConsent.isRecurringIndicator()).isTrue();
        assertThat(actualAccountConsent.getValidUntil().compareTo(getDateFromDateString("2017-11-01")));
    }

    private CreateConsentResp getCreateConsentRespEntityFromController(String path, boolean withBalance, boolean tppRedirectPreferred) throws IOException {
        // Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String requestJson = IOUtils.resourceToString(path, UTF_8);
        CreateConsentReq createConsentReq = MAPPER.readValue(requestJson, CreateConsentReq.class);

        // When:
        ResponseEntity<CreateConsentResp> actualResponse = consentInformationController.createAccountConsent(PSU_ID, withBalance, tppRedirectPreferred, createConsentReq);

        // Then:
        HttpStatus actualStatusCode = actualResponse.getStatusCode();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);

        CreateConsentResp actualResult = actualResponse.getBody();
        return actualResult;
    }

    private ResponseObject<CreateConsentResp> createConsentResponse() {
        CreateConsentResp resp = new CreateConsentResp(TransactionStatus.RCVD, CONSENT_ID, new AuthenticationObject[]{}, new Links(), "message");
        return new ResponseObject<>(resp);
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
