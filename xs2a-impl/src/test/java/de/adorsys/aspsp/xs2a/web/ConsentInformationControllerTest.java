package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentRequestBody;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentResponseBody;
import de.adorsys.aspsp.xs2a.spi.utils.FileUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentInformationControllerTest {
    private final String AIC_REQUEST_PATH = "json/AccountInformationConsentRequestTest.json";

    @Autowired
    private ConsentInformationController consentInformationController;
    @Autowired
    private ConsentService consentService;

    @Test
    public void createConsentForAccounts_withBalanceAndTppRedirect() throws IOException {
        boolean withBalance = true;
        boolean tppRedirectPreferred = false;

        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;
        String aicRequestJson = getJsonString(AIC_REQUEST_PATH);
        AccountInformationConsentRequestBody expectedAicRequest = new Gson().fromJson(aicRequestJson, AccountInformationConsentRequestBody.class);

        //When:
        ResponseEntity<AccountInformationConsentResponseBody> actualAicResponse = consentInformationController.createConsentForAccounts(withBalance, tppRedirectPreferred, expectedAicRequest);

        //Then:
        HttpStatus actualStatusCode = actualAicResponse.getStatusCode();
        AccountInformationConsentResponseBody actualResult = actualAicResponse.getBody();
        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getTransactionStatus()).isEqualTo(TransactionStatus.RCVD);

        //Given:
        String consentId = actualResult.getConsentId();

        //When:
        AccountInformationConsentRequestBody actualAicRequest = consentService.getAicRequest(consentId);
        //Then:
        assertThat(actualAicRequest).isEqualTo(expectedAicRequest);
    }

    public String getJsonString(String filePath) throws IOException {
        String fullPath = getClass().getClassLoader().getResource(filePath).getFile();
        return FileUtil.getJsonStringFromFile(fullPath);

    }
}
