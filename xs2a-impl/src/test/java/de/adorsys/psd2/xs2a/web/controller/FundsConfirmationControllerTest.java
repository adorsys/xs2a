/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.controller;

import com.google.gson.Gson;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.model.InlineResponse2003;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.service.FundsConfirmationService;
import de.adorsys.psd2.xs2a.service.mapper.FundsConfirmationModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationControllerTest {
    private final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String CONSENT_ID = "233108c8-8f67-4866-b4b7-66a0df044342";

    @InjectMocks
    private FundsConfirmationController fundsConfirmationController;
    @Mock
    private FundsConfirmationService fundsConfirmationService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private FundsConfirmationModelMapper fundsConfirmationModelMapper;

    @BeforeEach
    void setUp() {
        when(fundsConfirmationService.fundsConfirmation(any())).thenReturn(readResponseObject());
    }

    @Test
    void fundConfirmation() throws IOException {
        when(responseMapper.ok(any(), any())).thenReturn(getInlineResponse());

        //Given
        ConfirmationOfFunds confirmationOfFunds = getConfirmationOfFunds();
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<?> actualResult = fundsConfirmationController.checkAvailabilityOfFunds(confirmationOfFunds, null, CONSENT_ID, null, null, null, null);
        InlineResponse2003 fundsConfirmationResponse = (InlineResponse2003) actualResult.getBody();

        //Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(fundsConfirmationResponse).isNotNull();
        assertThat(fundsConfirmationResponse.getFundsAvailable()).isTrue();
        verify(fundsConfirmationModelMapper, atLeastOnce()).mapToFundsConfirmationRequest(confirmationOfFunds, CONSENT_ID);
    }

    private ResponseObject<FundsConfirmationResponse> readResponseObject() {
        return ResponseObject.<FundsConfirmationResponse>builder()
                   .body(new FundsConfirmationResponse(true)).build();
    }

    private ResponseEntity getInlineResponse() {
        return new ResponseEntity<>(new InlineResponse2003().fundsAvailable(true), HttpStatus.OK);
    }

    private ConfirmationOfFunds getConfirmationOfFunds() throws IOException {
        String FUNDS_REQ_DATA = "/json/ConfirmationOfFundsTestData.json";
        return new Gson().fromJson(IOUtils.resourceToString(FUNDS_REQ_DATA, UTF_8), ConfirmationOfFunds.class);
    }
}
