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

import de.adorsys.aspsp.xs2a.domain.*;
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
import java.util.Collections;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.*;
import static de.adorsys.aspsp.xs2a.domain.SupportedAccountReferenceField.IBAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AspspProfileControllerTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<String> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int CONSENT_LIFETIME = 0;
    private static final int TRANSACTION_LIFETIME = 0;
    private static final boolean ALL_PSD_2_SUPPORT = false;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORT = false;
    private static final AuthorisationStartType AUTHORIZATION_START_TYPE = AuthorisationStartType.IMPLICIT;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = false;

    @Autowired
    private AspspProfileController aspspProfileController;

    @MockBean
    private AspspProfileService aspspProfileService;

    @Before
    public void setUpAccountServiceMock() {
        when(aspspProfileService.getAspspSettings())
            .thenReturn(buildAspspSettings());
        when(aspspProfileService.getScaApproach())
            .thenReturn(ScaApproach.REDIRECT);
    }

    @Test
    public void getAspspSettings() {
        //Given:
        HttpStatus expectedStatusCode = HttpStatus.OK;

        //When:
        ResponseEntity<AspspSettings> actualResponse = aspspProfileController.getAspspSettings();

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedStatusCode);
        assertThat(actualResponse.getBody()).isEqualTo(buildAspspSettings());
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

    private static AspspSettings buildAspspSettings() {
        return new AspspSettings(
            FREQUENCY_PER_DAY,
            COMBINED_SERVICE_INDICATOR,
            AVAILABLE_PAYMENT_PRODUCTS,
            AVAILABLE_PAYMENT_TYPES,
            TPP_SIGNATURE_REQUIRED,
            PIS_REDIRECT_LINK,
            AIS_REDIRECT_LINK,
            MULTICURRENCY_ACCOUNT_LEVEL,
            BANK_OFFERED_CONSENT_SUPPORT,
            AVAILABLE_BOOKING_STATUSES,
            SUPPORTED_ACCOUNT_REFERENCE_FIELDS,
            CONSENT_LIFETIME,
            TRANSACTION_LIFETIME,
            ALL_PSD_2_SUPPORT,
            AUTHORIZATION_START_TYPE,
            TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED);
    }

    private static List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        return Collections.singletonList(IBAN);
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
