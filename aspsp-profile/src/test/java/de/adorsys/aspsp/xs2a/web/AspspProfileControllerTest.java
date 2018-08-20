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
import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
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

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOOKED;
import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOTH;
import static de.adorsys.aspsp.xs2a.domain.BookingStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AspspProfileControllerTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<String> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final boolean ALL_PSD_2_SUPPORT = false;

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
            .thenReturn(ScaApproach.REDIRECT);
        when(aspspProfileService.isTppSignatureRequired())
            .thenReturn(true);
        when(aspspProfileService.getPisRedirectUrlToAspsp())
            .thenReturn(PIS_REDIRECT_LINK);
        when(aspspProfileService.getAisRedirectUrlToAspsp())
            .thenReturn(AIS_REDIRECT_LINK);
        when(aspspProfileService.getMulticurrencyAccountLevel())
            .thenReturn(MULTICURRENCY_ACCOUNT_LEVEL);
        when(aspspProfileService.getAvailableBookingStatuses())
            .thenReturn(AVAILABLE_BOOKING_STATUSES);
        when(aspspProfileService.isAllPsd2Support())
            .thenReturn(ALL_PSD_2_SUPPORT);
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
        ResponseEntity<ScaApproach> actualResponse = aspspProfileController.getScaApproach();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(ScaApproach.REDIRECT);
    }

    @Test
    public void getTppSignatureRequired() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<Boolean> actualResponse = aspspProfileController.getTppSignatureRequired();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(true);
    }

    @Test
    public void getPisRedirectUrlToAspsp() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<String> actualResponse = aspspProfileController.getPisRedirectUrlToAspsp();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(PIS_REDIRECT_LINK);
    }

    @Test
    public void getAisRedirectUrlToAspsp() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<String> actualResponse = aspspProfileController.getAisRedirectUrlToAspsp();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(AIS_REDIRECT_LINK);
    }

    @Test
    public void getMulticurrencyAccountLevel() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<MulticurrencyAccountLevel> actualResponse = aspspProfileController.getMulticurrencyAccountLevel();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(MULTICURRENCY_ACCOUNT_LEVEL);
    }

    @Test
    public void getAvailableBookingStatuses() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<List<BookingStatus>> actualResponse = aspspProfileController.getAvailableBookingStatuses();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(AVAILABLE_BOOKING_STATUSES);
    }

    @Test
    public void getAllPsd2Support() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<Boolean> actualResponse = aspspProfileController.getAllPsd2Support();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(ALL_PSD_2_SUPPORT);
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

    private static List<BookingStatus> getBookingStatuses() {
        return Arrays.asList(
            BOOKED,
            PENDING,
            BOTH
        );
    }
}
