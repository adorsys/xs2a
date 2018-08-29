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

package de.adorsys.aspsp.xs2a.web12;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.service.AccountReferenceValidationService;
import de.adorsys.aspsp.xs2a.service.FundsConfirmationService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FundsConfirmationControllerTest {
    private final String FUNDS_REQ_DATA = "/json/web12/ConfirmationOfFundsTestData.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @InjectMocks
    private FundsConfirmationController12 fundsConfirmationController;
    @Mock
    private FundsConfirmationService fundsConfirmationService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private AccountReferenceValidationService referenceValidationService;

    @Before
    public void setUp() {
        when(fundsConfirmationService.fundsConfirmation(any())).thenReturn(readResponseObject());
        when(referenceValidationService.validateAccountReferences(any())).thenReturn(ResponseObject.builder().build());
    }

    @Test
    public void fundConfirmation() throws IOException {
        when(responseMapper.ok(any())).thenReturn(new ResponseEntity<>(readResponseObject().getBody(), HttpStatus.OK));

        //Given
        ConfirmationOfFunds confirmationOfFunds = getConfirmationOfFunds();
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<?> actualResult = fundsConfirmationController.checkAvailabilityOfFunds(confirmationOfFunds, null, null, null, null);
        FundsConfirmationResponse fundsConfirmationResponse = (FundsConfirmationResponse) actualResult.getBody();

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(fundsConfirmationResponse.isFundsAvailable()).isEqualTo(true);
    }

    private ResponseObject<FundsConfirmationResponse> readResponseObject() {
        return ResponseObject.<FundsConfirmationResponse>builder()
                   .body(new FundsConfirmationResponse(true)).build();
    }

    private ConfirmationOfFunds getConfirmationOfFunds() throws IOException {
        return new Gson().fromJson(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), ConfirmationOfFunds.class);
    }
}
