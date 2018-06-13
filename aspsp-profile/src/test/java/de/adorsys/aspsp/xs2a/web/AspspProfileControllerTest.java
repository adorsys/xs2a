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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.config.ProfileConfiguration;
import de.adorsys.aspsp.xs2a.service.AspspProfileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AspspProfileControllerTest {
    private final int FREQUENCY_PER_DAY = 5;
    private final boolean COMBINED_SERVICE_INDICATOR = false;
    private final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private final List<String> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private final String SCA_APPROACH = "redirect";

    @Autowired
    private AspspProfileController aspspProfileController;

    @MockBean
    private AspspProfileService aspspProfileService;

    @MockBean
    private ProfileConfiguration profileConfiguration;

    @Before
    public void setUpAccountServiceMock() {
        when(aspspProfileService.getFrequencyPerDay())
            .thenReturn(FREQUENCY_PER_DAY);
        when(aspspProfileService.isCombinedServiceIndicator())
            .thenReturn(COMBINED_SERVICE_INDICATOR);
        when(aspspProfileService.getAvailablePaymentProducts())
            .thenReturn(AVAILABLE_PAYMENT_PRODUCTS);
        when(aspspProfileService.getAvailablePaymentTypes())
            .thenReturn(AVAILABLE_PAYMENT_TYPES);
        when(aspspProfileService.getScaApproach())
            .thenReturn(SCA_APPROACH);
    }

    @Test
    public void getFrequencyPerDay() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<Integer> actualResponse = aspspProfileController.getFrequencyPerDay();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(FREQUENCY_PER_DAY);
    }

    @Test
    public void getCombinedServiceIndicator() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<Boolean> actualResponse = aspspProfileController.getCombinedServiceIndicator();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(COMBINED_SERVICE_INDICATOR);
    }

    @Test
    public void getAvailablePaymentProducts() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<String>> actualResponse = aspspProfileController.getAvailablePaymentProducts();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(AVAILABLE_PAYMENT_PRODUCTS);
    }

    @Test
    public void getAvailablePaymentTypes() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<String>> actualResponse = aspspProfileController.getAvailablePaymentTypes();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(AVAILABLE_PAYMENT_TYPES);
    }

    @Test
    public void getScaApproach() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<String> actualResponse = aspspProfileController.getScaApproach();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(SCA_APPROACH);
    }

    private static List<String> getPaymentProducts() {
        return Arrays.asList(
            "sepa-credit-transfers",
            "instant-sepa-credit-transfers");
    }

    private static List<String> getPaymentTypes() {
        return Arrays.asList(
            "periodic",
            "delayed",
            "bulk");
    }
}
