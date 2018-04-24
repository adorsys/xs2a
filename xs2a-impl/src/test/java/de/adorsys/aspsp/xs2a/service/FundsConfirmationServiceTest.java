/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
