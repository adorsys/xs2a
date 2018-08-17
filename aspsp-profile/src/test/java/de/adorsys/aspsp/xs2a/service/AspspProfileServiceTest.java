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

import de.adorsys.aspsp.xs2a.config.ProfileConfiguration;
import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOOKED;
import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOTH;
import static de.adorsys.aspsp.xs2a.domain.BookingStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileServiceTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<String> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final int CONSENT_LIFETIME = 0;

    @InjectMocks
    private AspspProfileService aspspProfileService;

    @Mock
    private ProfileConfiguration profileConfiguration;

    @Before
    public void setUpAccountServiceMock() {
        when(profileConfiguration.getFrequencyPerDay())
            .thenReturn(FREQUENCY_PER_DAY);
        when(profileConfiguration.isCombinedServiceIndicator())
            .thenReturn(COMBINED_SERVICE_INDICATOR);
        when(profileConfiguration.getAvailablePaymentProducts())
            .thenReturn(AVAILABLE_PAYMENT_PRODUCTS);
        when(profileConfiguration.getAvailablePaymentTypes())
            .thenReturn(AVAILABLE_PAYMENT_TYPES);
        when(profileConfiguration.getScaApproach())
            .thenReturn(SCA_APPROACH);
        when(profileConfiguration.isTppSignatureRequired())
            .thenReturn(true);
        when(profileConfiguration.getPisRedirectUrlToAspsp())
            .thenReturn(PIS_REDIRECT_LINK);
        when(profileConfiguration.getAisRedirectUrlToAspsp())
            .thenReturn(AIS_REDIRECT_LINK);
        when(profileConfiguration.getMulticurrencyAccountLevel())
            .thenReturn(MULTICURRENCY_ACCOUNT_LEVEL);
        when(profileConfiguration.getAvailableBookingStatuses())
            .thenReturn(AVAILABLE_BOOKING_STATUSES);
        when(profileConfiguration.getConsentLifetime())
            .thenReturn(CONSENT_LIFETIME);
    }

    @Test
    public void getFrequencyPerDay() {
        //When:
        int actualResponse = aspspProfileService.getFrequencyPerDay();

        //Then:
        assertThat(actualResponse).isEqualTo(FREQUENCY_PER_DAY);
    }

    @Test
    public void getCombinedServiceIndicator() {
        //When:
        boolean actualResponse = aspspProfileService.isCombinedServiceIndicator();

        //Then:
        assertThat(actualResponse).isEqualTo(COMBINED_SERVICE_INDICATOR);
    }

    @Test
    public void getAvailablePaymentProducts() {
        //When:
        List<String> actualResponse = aspspProfileService.getAvailablePaymentProducts();

        //Then:
        assertThat(actualResponse).isEqualTo(AVAILABLE_PAYMENT_PRODUCTS);
    }

    @Test
    public void getAvailablePaymentTypes() {
        //When:
        List<String> actualResponse = aspspProfileService.getAvailablePaymentTypes();

        //Then:
        assertThat(actualResponse).isEqualTo(AVAILABLE_PAYMENT_TYPES);
    }

    @Test
    public void getScaApproach() {
        //When:
        ScaApproach actualResponse = aspspProfileService.getScaApproach();

        //Then:
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    @Test
    public void isTppSignatureRequired() {
        //When:
        boolean actualResponse = aspspProfileService.isTppSignatureRequired();

        //Then:
        assertThat(actualResponse).isEqualTo(true);
    }

    @Test
    public void getPisRedirectUrlToAspsp() {
        //When:
        String actualResponse = aspspProfileService.getPisRedirectUrlToAspsp();

        //Then:
        assertThat(actualResponse).isEqualTo(PIS_REDIRECT_LINK);
    }

    @Test
    public void getAisRedirectUrlToAspsp() {
        //When:
        String actualResponse = aspspProfileService.getAisRedirectUrlToAspsp();

        //Then:
        assertThat(actualResponse).isEqualTo(AIS_REDIRECT_LINK);
    }

    @Test
    public void getMulticurrencyAccountLevel() {
        //When:
        MulticurrencyAccountLevel actualResponse = aspspProfileService.getMulticurrencyAccountLevel();

        //Then:
        assertThat(actualResponse).isEqualTo(MULTICURRENCY_ACCOUNT_LEVEL);
    }

    @Test
    public void getAvailableBookingStatuses() {
        //When:
        List<BookingStatus> actualResponse = aspspProfileService.getAvailableBookingStatuses();

        //Then:
        assertThat(actualResponse).isEqualTo(AVAILABLE_BOOKING_STATUSES);
    }

    @Test
    public void getConsentLifetime() {
        //When:
        int actualResponse = aspspProfileService.getConsentLifetime();

        //Then:
        assertThat(actualResponse).isEqualTo(CONSENT_LIFETIME);
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
