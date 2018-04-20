package de.adorsys.aspsp.xs2a.service;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FundsConfirmationServiceTest {
    private final String FUNDS_REQ_DATA = "/json/FundsConfirmationRequestTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    private FundsConfirmationService fundsConfirmationService;

    @Test
    public void fundsConfirmation() throws Exception {
        //Given:
        FundsConfirmationRequest expectedRequest = readFundsConfirmationRequest();

        //When:
        ResponseObject<FundsConfirmationResponse> actualResponse = fundsConfirmationService.fundsConfirmation(expectedRequest);

        //Then:
        assertThat(actualResponse.getBody().isFundsAvailable()).isEqualTo(true);
    }

    private FundsConfirmationRequest readFundsConfirmationRequest() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), FundsConfirmationRequest.class);
    }
}
