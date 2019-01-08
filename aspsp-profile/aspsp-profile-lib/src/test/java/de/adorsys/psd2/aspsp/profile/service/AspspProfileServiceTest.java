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

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.BookingStatus;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.BookingStatus.*;
import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileServiceTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final List<String> AVAILABLE_PAYMENT_PRODUCTS = getPaymentProducts();
    private static final List<PaymentType> AVAILABLE_PAYMENT_TYPES = getPaymentTypes();
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final ScaApproach REDIRECT_APPROACH = ScaApproach.REDIRECT;
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
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS = 86400000;
    private static Map<PaymentType, List<String>> TYPE_PRODUCT_MATRIX = buildTypeProductMatrix();

    @InjectMocks
    private AspspProfileServiceImpl aspspProfileService;

    @Mock
    private ProfileConfiguration profileConfiguration;

    @Before
    public void setUpAccountServiceMock() {
        when(profileConfiguration.getSetting()).thenReturn(buildBankProfileSetting());
    }

    @Test
    public void getPisRedirectUrlToAspsp_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getPisRedirectUrlToAspsp()).isEqualTo(PIS_REDIRECT_LINK);
    }

    @Test
    public void getAisRedirectUrlToAspsp_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getAisRedirectUrlToAspsp()).isEqualTo(AIS_REDIRECT_LINK);
    }

    @Test
    public void getAvailablePaymentTypes_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getAvailablePaymentTypes()).isEqualTo(AVAILABLE_PAYMENT_TYPES);
    }

    @Test
    public void getAvailablePaymentProducts_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getAvailablePaymentProducts()).isEqualTo(AVAILABLE_PAYMENT_PRODUCTS);
    }

    @Test
    public void getScaApproach_success() {
        //When:
        ScaApproach actualResponse = aspspProfileService.getScaApproach();

        //Then:
        Assertions.assertThat(actualResponse).isEqualTo(REDIRECT_APPROACH);
    }

    @Test
    public void getRedirectUrlExpirationTimeMs_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getRedirectUrlExpirationTimeMs()).isEqualTo(REDIRECT_URL_EXPIRATION_TIME_MS);
    }

    @Test
    public void getFrequencyPerDay_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getFrequencyPerDay()).isEqualTo(FREQUENCY_PER_DAY);
    }

    @Test
    public void getNotConfirmedConsentExpirationPeriodMs_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getNotConfirmedConsentExpirationPeriodMs()).isEqualTo(NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS);
    }

    @Test
    public void getNotConfirmedPaymentExpirationPeriodMs_success() {
        //When:
        AspspSettings actualResponse = aspspProfileService.getAspspSettings();

        //Then:
        Assertions.assertThat(actualResponse.getNotConfirmedPaymentExpirationPeriodMs()).isEqualTo(NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS);
    }

    private BankProfileSetting buildBankProfileSetting() {
        BankProfileSetting setting = new BankProfileSetting();
        setting.setFrequencyPerDay(FREQUENCY_PER_DAY);
        setting.setCombinedServiceIndicator(COMBINED_SERVICE_INDICATOR);
        setting.setAvailablePaymentProducts(AVAILABLE_PAYMENT_PRODUCTS);
        setting.setAvailablePaymentTypes(AVAILABLE_PAYMENT_TYPES);
        setting.setTppSignatureRequired(TPP_SIGNATURE_REQUIRED);
        setting.setPisRedirectUrlToAspsp(PIS_REDIRECT_LINK);
        setting.setAisRedirectUrlToAspsp(AIS_REDIRECT_LINK);
        setting.setMulticurrencyAccountLevel(MULTICURRENCY_ACCOUNT_LEVEL);
        setting.setBankOfferedConsentSupport(BANK_OFFERED_CONSENT_SUPPORT);
        setting.setAvailableBookingStatuses(AVAILABLE_BOOKING_STATUSES);
        setting.setSupportedAccountReferenceFields(SUPPORTED_ACCOUNT_REFERENCE_FIELDS);
        setting.setConsentLifetime(CONSENT_LIFETIME);
        setting.setTransactionLifetime(TRANSACTION_LIFETIME);
        setting.setAllPsd2Support(ALL_PSD_2_SUPPORT);
        setting.setTransactionsWithoutBalancesSupported(TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED);
        setting.setSigningBasketSupported(SIGNING_BASKET_SUPPORTED);
        setting.setPaymentCancellationAuthorizationMandated(PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED);
        setting.setPiisConsentSupported(PIIS_CONSENT_SUPPORTED);
        setting.setDeltaReportSupported(DELTA_REPORT_SUPPORTED);
        setting.setRedirectUrlExpirationTimeMs(REDIRECT_URL_EXPIRATION_TIME_MS);
        setting.setScaApproach(REDIRECT_APPROACH);
        setting.setNotConfirmedConsentExpirationPeriodMs(NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS);
        setting.setNotConfirmedPaymentExpirationPeriodMs(NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS);
        setting.setTypeProductMatrix(TYPE_PRODUCT_MATRIX);
        return setting;
    }


    private static List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        return Collections.singletonList(IBAN);
    }

    private static List<String> getPaymentProducts() {
        return Arrays.asList(
            "sepa-credit-transfers",
            "instant-sepa-credit-transfers");
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

    private static Map<PaymentType, List<String>> buildTypeProductMatrix() {
        Map<PaymentType, List<String>> matrix = new HashMap<>();
        List<String> availablePaymentProducts = Arrays.asList("sepa-credit-transfers", "instant-sepa-credit-transfers");
        matrix.put(PaymentType.SINGLE, availablePaymentProducts);
        return matrix;
    }
}
