/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
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

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileUpdateServiceImplTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = true;
    private static final boolean TPP_SIGNATURE_REQUIRED = true;
    private static final ScaApproach REDIRECT_APPROACH = ScaApproach.REDIRECT;
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String PIS_CANCELLATION_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/cancellation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int CONSENT_LIFETIME = 10;
    private static final int TRANSACTION_LIFETIME = 10;
    private static final boolean ALL_PSD_2_SUPPORT = true;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORT = true;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = true;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final boolean PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED = true;
    private static final boolean PIIS_CONSENT_SUPPORTED = true;
    private static final boolean DELTA_REPORT_SUPPORTED = true;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = true;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = true;
    private static final boolean FORCE_XS2A_BASE_URL = true;
    private static final String XS2A_BASE_URL = "http://myhost.com/";

    @InjectMocks
    private AspspProfileUpdateServiceImpl aspspProfileUpdateService;

    @Mock
    private ProfileConfiguration profileConfiguration;

    @Before
    public void setUp() {
        when(profileConfiguration.getSetting()).thenReturn(new BankProfileSetting());
    }

    @Test
    public void updateScaApproaches_success() {
        //When:
        aspspProfileUpdateService.updateScaApproaches(Collections.singletonList(REDIRECT_APPROACH));

        //Then:
        Assertions.assertThat(profileConfiguration.getSetting().getScaApproaches()).isEqualTo(Collections.singletonList(REDIRECT_APPROACH));
    }

    @Test
    public void updateAspspSettings_success() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(new AspspSettings(FREQUENCY_PER_DAY, COMBINED_SERVICE_INDICATOR, TPP_SIGNATURE_REQUIRED, PIS_REDIRECT_LINK, AIS_REDIRECT_LINK,
                                                                        MULTICURRENCY_ACCOUNT_LEVEL, BANK_OFFERED_CONSENT_SUPPORT, AVAILABLE_BOOKING_STATUSES, SUPPORTED_ACCOUNT_REFERENCE_FIELDS, CONSENT_LIFETIME, TRANSACTION_LIFETIME, ALL_PSD_2_SUPPORT,
                                                                        TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED, SIGNING_BASKET_SUPPORTED, PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED, PIIS_CONSENT_SUPPORTED, DELTA_REPORT_SUPPORTED, REDIRECT_URL_EXPIRATION_TIME_MS,
                                                                        PIS_CANCELLATION_REDIRECT_LINK, NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS, NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS, SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX, PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS,
                                                                        AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED, SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, PSU_IN_INITIAL_REQUEST_MANDATED, FORCE_XS2A_BASE_URL, XS2A_BASE_URL));

        //Then:
        BankProfileSetting setting = profileConfiguration.getSetting();
        Assertions.assertThat(setting.getFrequencyPerDay()).isEqualTo(FREQUENCY_PER_DAY);
        Assertions.assertThat(setting.isCombinedServiceIndicator()).isEqualTo(COMBINED_SERVICE_INDICATOR);
        Assertions.assertThat(setting.isTppSignatureRequired()).isEqualTo(TPP_SIGNATURE_REQUIRED);
        Assertions.assertThat(setting.getPisRedirectUrlToAspsp()).isEqualTo(PIS_REDIRECT_LINK);
        Assertions.assertThat(setting.getAisRedirectUrlToAspsp()).isEqualTo(AIS_REDIRECT_LINK);
        Assertions.assertThat(setting.getMulticurrencyAccountLevel()).isEqualTo(MULTICURRENCY_ACCOUNT_LEVEL);
        Assertions.assertThat(setting.isBankOfferedConsentSupport()).isEqualTo(BANK_OFFERED_CONSENT_SUPPORT);
        Assertions.assertThat(setting.getAvailableBookingStatuses()).isEqualTo(AVAILABLE_BOOKING_STATUSES);
        Assertions.assertThat(setting.getSupportedAccountReferenceFields()).isEqualTo(SUPPORTED_ACCOUNT_REFERENCE_FIELDS);
        Assertions.assertThat(setting.getConsentLifetime()).isEqualTo(CONSENT_LIFETIME);
        Assertions.assertThat(setting.getTransactionLifetime()).isEqualTo(TRANSACTION_LIFETIME);
        Assertions.assertThat(setting.isAllPsd2Support()).isEqualTo(ALL_PSD_2_SUPPORT);
        Assertions.assertThat(setting.isTransactionsWithoutBalancesSupported()).isEqualTo(TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED);
        Assertions.assertThat(setting.isSigningBasketSupported()).isEqualTo(SIGNING_BASKET_SUPPORTED);
        Assertions.assertThat(setting.isPaymentCancellationAuthorizationMandated()).isEqualTo(PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED);
        Assertions.assertThat(setting.isPiisConsentSupported()).isEqualTo(PIIS_CONSENT_SUPPORTED);
        Assertions.assertThat(setting.isDeltaReportSupported()).isEqualTo(DELTA_REPORT_SUPPORTED);
        Assertions.assertThat(setting.getRedirectUrlExpirationTimeMs()).isEqualTo(REDIRECT_URL_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.getPisPaymentCancellationRedirectUrlToAspsp()).isEqualTo(PIS_CANCELLATION_REDIRECT_LINK);
        Assertions.assertThat(setting.getNotConfirmedConsentExpirationPeriodMs()).isEqualTo(NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS);
        Assertions.assertThat(setting.getNotConfirmedPaymentExpirationPeriodMs()).isEqualTo(NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS);
        Assertions.assertThat(setting.getSupportedPaymentTypeAndProductMatrix()).isEqualTo(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX);
        Assertions.assertThat(setting.getPaymentCancellationRedirectUrlExpirationTimeMs()).isEqualTo(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.isAvailableAccountsConsentSupported()).isEqualTo(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED);
        Assertions.assertThat(setting.isScaByOneTimeAvailableAccountsConsentRequired()).isEqualTo(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED);
        Assertions.assertThat(setting.isPsuInInitialRequestMandated()).isEqualTo(PSU_IN_INITIAL_REQUEST_MANDATED);
        Assertions.assertThat(setting.isForceXs2aBaseUrl()).isEqualTo(FORCE_XS2A_BASE_URL);
        Assertions.assertThat(setting.getXs2aBaseUrl()).isEqualTo(XS2A_BASE_URL);
    }

    private static List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        return Collections.singletonList(IBAN);
    }

    private static List<BookingStatus> getBookingStatuses() {
        return Arrays.asList(
            BOOKED,
            PENDING,
            BOTH
        );
    }

    private static Map<PaymentType, Set<String>> buildSupportedPaymentTypeAndProductMatrix() {
        Map<PaymentType, Set<String>> matrix = new HashMap<>();
        Set<String> availablePaymentProducts = Collections.singleton("sepa-credit-transfers");
        matrix.put(PaymentType.SINGLE, availablePaymentProducts);
        return matrix;
    }
}
