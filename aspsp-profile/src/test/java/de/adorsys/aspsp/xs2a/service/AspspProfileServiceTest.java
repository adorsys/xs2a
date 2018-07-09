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
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AspspProfileServiceTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<String> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/payment/confirmation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";

    @Autowired
    private AspspProfileService aspspProfileService;

    @MockBean
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
