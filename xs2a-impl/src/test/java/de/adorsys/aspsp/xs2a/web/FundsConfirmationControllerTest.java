package de.adorsys.aspsp.xs2a.web;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FundsConfirmationControllerTest {
    private final String FUNDS_REQ_DATA = "/json/FundsConfirmationRequestTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private FundsConfirmationController fundsConfirmationController;

    @MockBean(name = "fundsConfirmationService")
    private FundsConfirmationService fundsConfirmationService;

    @Before
    public void setUp() {
        when(fundsConfirmationService.fundsConfirmation(any())).thenReturn(readResponseObject());
    }

    @Test
    public void fundConfirmation() throws IOException {
        //Given
        FundsConfirmationRequest fundsReq = readFundsConfirmationRequest();
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<FundsConfirmationResponse> actualResult = fundsConfirmationController.fundConfirmation(fundsReq);

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResult.getBody().isFundsAvailable()).isEqualTo(true);
    }

    private ResponseObject<FundsConfirmationResponse> readResponseObject() {
        return new ResponseObject<>(new FundsConfirmationResponse(true));
    }

    private FundsConfirmationRequest readFundsConfirmationRequest() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), FundsConfirmationRequest.class);
    }
}
