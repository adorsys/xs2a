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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.BookingStatus;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.aspsp.profile.domain.BookingStatus.*;
import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileServiceTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<PaymentProduct> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<PaymentType> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
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
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = false;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final boolean PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED = false;
    private static final boolean PIIS_CONSENT_SUPPORTED = false;
    private static final boolean DELTA_REPORT_SUPPORTED = false;

    private AspspProfileService aspspProfileService;

    @Mock
    private ProfileConfiguration profileConfiguration;

    @Before
    public void setUpAccountServiceMock() {
        Mockito.when(profileConfiguration.getFrequencyPerDay())
            .thenReturn(FREQUENCY_PER_DAY);
        Mockito.when(profileConfiguration.isCombinedServiceIndicator())
            .thenReturn(COMBINED_SERVICE_INDICATOR);
        Mockito.when(profileConfiguration.getAvailablePaymentProducts())
            .thenReturn(AVAILABLE_PAYMENT_PRODUCTS);
        Mockito.when(profileConfiguration.getAvailablePaymentTypes())
            .thenReturn(AVAILABLE_PAYMENT_TYPES);
        Mockito.when(profileConfiguration.isTppSignatureRequired())
            .thenReturn(TPP_SIGNATURE_REQUIRED);
        Mockito.when(profileConfiguration.getScaApproach())
            .thenReturn(SCA_APPROACH);
        Mockito.when(profileConfiguration.getPisRedirectUrlToAspsp())
            .thenReturn(PIS_REDIRECT_LINK);
        Mockito.when(profileConfiguration.getAisRedirectUrlToAspsp())
            .thenReturn(AIS_REDIRECT_LINK);
        Mockito.when(profileConfiguration.getMulticurrencyAccountLevel())
            .thenReturn(MULTICURRENCY_ACCOUNT_LEVEL);
        Mockito.when(profileConfiguration.getAvailableBookingStatuses())
            .thenReturn(AVAILABLE_BOOKING_STATUSES);
        Mockito.when(profileConfiguration.getSupportedAccountReferenceFields())
            .thenReturn(SUPPORTED_ACCOUNT_REFERENCE_FIELDS);
        Mockito.when(profileConfiguration.getConsentLifetime())
            .thenReturn(CONSENT_LIFETIME);
        Mockito.when(profileConfiguration.getTransactionLifetime())
            .thenReturn(TRANSACTION_LIFETIME);
        Mockito.when(profileConfiguration.isAllPsd2Support())
            .thenReturn(ALL_PSD_2_SUPPORT);
        Mockito.when(profileConfiguration.isBankOfferedConsentSupport())
            .thenReturn(BANK_OFFERED_CONSENT_SUPPORT);
         Mockito.when(profileConfiguration.isSigningBasketSupported()    )
             .thenReturn(SIGNING_BASKET_SUPPORTED);
        Mockito.when(profileConfiguration.isPaymentCancellationAuthorizationMandated())
            .thenReturn(PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED);
        Mockito.when(profileConfiguration.isPiisConsentSupported())
            .thenReturn(PIIS_CONSENT_SUPPORTED);
        Mockito.when(profileConfiguration.isDeltaReportSupported())
            .thenReturn(DELTA_REPORT_SUPPORTED);

        aspspProfileService = new AspspProfileServiceImpl(profileConfiguration);
        MockitoAnnotations.initMocks(aspspProfileService);
    }

    @Test
    public void getAspspSettings() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse).isEqualTo(buildAspspSettings());
    }

    @Test
    public void getScaApproach() {
        //When:
        ScaApproach actualResponse = aspspProfileService.getScaApproach();

        //Then:
        Assertions.assertThat(actualResponse).isEqualTo(SCA_APPROACH);
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
            TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED,
            SIGNING_BASKET_SUPPORTED,
            PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED,
            PIIS_CONSENT_SUPPORTED,
            DELTA_REPORT_SUPPORTED);
    }

    private static List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        return Collections.singletonList(IBAN);
    }

    private static List<PaymentProduct> getPaymentProducts() {
        return Arrays.asList(
            PaymentProduct.SEPA,
            PaymentProduct.INSTANT_SEPA);
    }

    private static List<PaymentType> getPaymentTypes() {
        return Arrays.asList(
            PaymentType.PERIODIC,
            PaymentType.BULK
        );
    }

    private static List<BookingStatus> getBookingStatuses() {
        return Arrays.asList(
            BOOKED,
            PENDING,
            BOTH
        );
    }
}
