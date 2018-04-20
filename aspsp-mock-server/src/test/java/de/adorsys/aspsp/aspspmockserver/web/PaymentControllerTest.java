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

package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PaymentService;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaymentControllerTest {
    @Autowired
    private PaymentController paymentController;
    @MockBean
    private PaymentService paymentService;

    @Before
    public void setUpPaymentServiceMock() throws IOException {
        SpiSinglePayments response = getExpectedRequest();
        response.setPaymentId("12345");
        when(paymentService.addPayment(getExpectedRequest())).thenReturn(Optional.of(response));
    }

    @Test
    public void createPayment() throws Exception {
        //Given
        HttpStatus expectedStatus = HttpStatus.CREATED;

        //When
        ResponseEntity<SpiSinglePayments> actualResponse = paymentController.createPayment(getExpectedRequest());
        HttpStatus actualStatus = actualResponse.getStatusCode();

        //Then
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualResponse.getBody()).isNotNull();
        assertThat(actualResponse.getBody().getPaymentId()).isNotNull();
    }

    private SpiSinglePayments getExpectedRequest() {
       SpiSinglePayments spiSinglePayments = new SpiSinglePayments();
       return spiSinglePayments;
    }
}
