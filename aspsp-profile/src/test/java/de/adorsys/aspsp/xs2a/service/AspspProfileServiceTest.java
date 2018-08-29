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
import de.adorsys.aspsp.xs2a.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.*;
import static de.adorsys.aspsp.xs2a.domain.SupportedAccountReferenceField.IBAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileServiceTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<String> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int CONSENT_LIFETIME = 0;
    private static final int TRANSACTION_LIFETIME = 0;
    private static final boolean ALL_PSD_2_SUPPORT = false;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORT = false;

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
        when(profileConfiguration.isTppSignatureRequired())
            .thenReturn(TPP_SIGNATURE_REQUIRED);
        when(profileConfiguration.getScaApproach())
            .thenReturn(SCA_APPROACH);
        when(profileConfiguration.getPisRedirectUrlToAspsp())
            .thenReturn(PIS_REDIRECT_LINK);
        when(profileConfiguration.getAisRedirectUrlToAspsp())
            .thenReturn(AIS_REDIRECT_LINK);
        when(profileConfiguration.getMulticurrencyAccountLevel())
            .thenReturn(MULTICURRENCY_ACCOUNT_LEVEL);
        when(profileConfiguration.getAvailableBookingStatuses())
            .thenReturn(AVAILABLE_BOOKING_STATUSES);
        when(profileConfiguration.getSupportedAccountReferenceFields())
            .thenReturn(SUPPORTED_ACCOUNT_REFERENCE_FIELDS);
        when(profileConfiguration.getConsentLifetime())
            .thenReturn(CONSENT_LIFETIME);
        when(profileConfiguration.getTransactionLifetime())
            .thenReturn(TRANSACTION_LIFETIME);
        when(profileConfiguration.isAllPsd2Support())
            .thenReturn(ALL_PSD_2_SUPPORT);
        when(profileConfiguration.isBankOfferedConsentSupport())
            .thenReturn(BANK_OFFERED_CONSENT_SUPPORT);
    }

    @Test
    public void getAspspSettings() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        assertThat(actualResponse).isEqualTo(buildAspspSettings());
    }

    @Test
    public void getScaApproach() {
        //When:
        ScaApproach actualResponse = aspspProfileService.getScaApproach();

        //Then:
        assertThat(actualResponse).isEqualTo(SCA_APPROACH);
    }

    private static AspspSettings buildAspspSettings() {
        return new AspspSettings(
            FREQUENCY_PER_DAY,
            COMBINED_SERVICE_INDICATOR,
            AVAILABLE_PAYMENT_PRODUCTS,
            AVAILABLE_PAYMENT_TYPES,
            SCA_APPROACH,
            TPP_SIGNATURE_REQUIRED,
            PIS_REDIRECT_LINK,
            AIS_REDIRECT_LINK,
            MULTICURRENCY_ACCOUNT_LEVEL,
            BANK_OFFERED_CONSENT_SUPPORT,
            AVAILABLE_BOOKING_STATUSES,
            SUPPORTED_ACCOUNT_REFERENCE_FIELDS,
            CONSENT_LIFETIME,
            TRANSACTION_LIFETIME,
            ALL_PSD_2_SUPPORT);
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
