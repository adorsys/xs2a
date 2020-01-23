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
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspProfileServiceTest {
    private static final int FREQUENCY_PER_DAY = 5;
    private static final boolean COMBINED_SERVICE_INDICATOR = false;
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final ScaApproach REDIRECT_APPROACH = ScaApproach.REDIRECT;
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String PIS_CANCELLATION_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/cancellation/";
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
    private static final boolean DELTA_LIST_SUPPORTED = false;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = true;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = false;
    private static final boolean FORCE_XS2A_BASE_URL = false;
    private static final String XS2A_BASE_URL = "http://myhost.com/";
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;


    @InjectMocks
    private AspspProfileServiceImpl aspspProfileService;

    @Mock
    private ProfileConfiguration profileConfiguration;
    private AspspSettings actualResponse;

    @BeforeEach
    void setUpAccountServiceMock() {
        when(profileConfiguration.getSetting()).thenReturn(buildBankProfileSetting());

        actualResponse = aspspProfileService.getAspspSettings();
    }

    @Test
    void getPisRedirectUrlToAspsp_success() {
        Assertions.assertEquals(PIS_REDIRECT_LINK, actualResponse.getPisRedirectUrlToAspsp());
    }

    @Test
    void getPisPaymentCancellationRedirectUrlToAspsp_success() {
        Assertions.assertEquals(PIS_CANCELLATION_REDIRECT_LINK, actualResponse.getPisPaymentCancellationRedirectUrlToAspsp());
    }

    @Test
    void getAisRedirectUrlToAspsp_success() {
        Assertions.assertEquals(AIS_REDIRECT_LINK, actualResponse.getAisRedirectUrlToAspsp());
    }

    @Test
    void getAvailablePaymentTypes_success() {
        Assertions.assertEquals(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX, actualResponse.getSupportedPaymentTypeAndProductMatrix());
    }

    @Test
    void getScaApproach_success() {
        //When:
        List<ScaApproach> actualResponse = aspspProfileService.getScaApproaches();

        //Then:
        Assertions.assertEquals(Collections.singletonList(REDIRECT_APPROACH), actualResponse);
    }

    @Test
    void getRedirectUrlExpirationTimeMs_success() {
        Assertions.assertEquals(REDIRECT_URL_EXPIRATION_TIME_MS, actualResponse.getRedirectUrlExpirationTimeMs());
    }

    @Test
    void getFrequencyPerDay_success() {
        Assertions.assertEquals(FREQUENCY_PER_DAY, actualResponse.getFrequencyPerDay());
    }

    @Test
    void getNotConfirmedConsentExpirationPeriodMs_success() {
        Assertions.assertEquals(NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS, actualResponse.getNotConfirmedConsentExpirationPeriodMs());
    }

    @Test
    void getNotConfirmedPaymentExpirationPeriodMs_success() {
        Assertions.assertEquals(NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS, actualResponse.getNotConfirmedPaymentExpirationPeriodMs());
    }

    @Test
    void getPaymentCancellationRedirectUrlExpirationTimeMs_success() {
        Assertions.assertEquals(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS, actualResponse.getPaymentCancellationRedirectUrlExpirationTimeMs());
    }

    @Test
    void getAuthorisationExpirationTimeMs_success() {
        Assertions.assertEquals(AUTHORISATION_EXPIRATION_TIME_MS, actualResponse.getAuthorisationExpirationTimeMs());
    }

    @Test
    void getAvailableAccountsConsentSupported_success() {
        Assertions.assertEquals(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED, actualResponse.isAvailableAccountsConsentSupported());
    }

    @Test
    void getScaByOneTimeAvailableAccountsConsentRequired_success() {
        Assertions.assertEquals(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, actualResponse.isScaByOneTimeAvailableAccountsConsentRequired());
    }

    @Test
    void getPsuInInitialRequestMandated_success() {
        Assertions.assertEquals(PSU_IN_INITIAL_REQUEST_MANDATED, actualResponse.isPsuInInitialRequestMandated());
    }

    @Test
    void getForceXs2aBaseUrl_success() {
        Assertions.assertEquals(FORCE_XS2A_BASE_URL, actualResponse.isForceXs2aBaseUrl());
    }

    @Test
    void getXs2aBaseUrl_success() {
        Assertions.assertEquals(XS2A_BASE_URL, actualResponse.getXs2aBaseUrl());
    }

    @Test
    void getEntryReferenceFromSupported_success() {
        Assertions.assertEquals(ENTRY_REFERENCE_FROM_SUPPORTED, actualResponse.isEntryReferenceFromSupported());
    }

    @Test
    void supportedTransactionApplicationTypes_success() {
        Assertions.assertEquals(SUPPORTED_TRANSACTION_APPLICATION_TYPES, actualResponse.getSupportedTransactionApplicationTypes());
    }

    @Test
    void getStartAuthorisationMode() {
        Assertions.assertEquals(START_AUTHORISATION_MODE, actualResponse.getStartAuthorisationMode());
    }

    private BankProfileSetting buildBankProfileSetting() {
        BankProfileSetting setting = new BankProfileSetting();
        setting.setFrequencyPerDay(FREQUENCY_PER_DAY);
        setting.setCombinedServiceIndicator(COMBINED_SERVICE_INDICATOR);
        setting.setTppSignatureRequired(TPP_SIGNATURE_REQUIRED);
        setting.setPisRedirectUrlToAspsp(PIS_REDIRECT_LINK);
        setting.setPisPaymentCancellationRedirectUrlToAspsp(PIS_CANCELLATION_REDIRECT_LINK);
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
        setting.setDeltaListSupported(DELTA_LIST_SUPPORTED);
        setting.setRedirectUrlExpirationTimeMs(REDIRECT_URL_EXPIRATION_TIME_MS);
        setting.setAuthorisationExpirationTimeMs(AUTHORISATION_EXPIRATION_TIME_MS);
        setting.setScaApproaches(Collections.singletonList(REDIRECT_APPROACH));
        setting.setNotConfirmedConsentExpirationPeriodMs(NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS);
        setting.setNotConfirmedPaymentExpirationPeriodMs(NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS);
        setting.setSupportedPaymentTypeAndProductMatrix(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX);
        setting.setPaymentCancellationRedirectUrlExpirationTimeMs(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        setting.setAvailableAccountsConsentSupported(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED);
        setting.setScaByOneTimeAvailableAccountsConsentRequired(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED);
        setting.setPsuInInitialRequestMandated(PSU_IN_INITIAL_REQUEST_MANDATED);
        setting.setForceXs2aBaseUrl(FORCE_XS2A_BASE_URL);
        setting.setXs2aBaseUrl(XS2A_BASE_URL);
        setting.setEntryReferenceFromSupported(ENTRY_REFERENCE_FROM_SUPPORTED);
        setting.setSupportedTransactionApplicationTypes(SUPPORTED_TRANSACTION_APPLICATION_TYPES);
        setting.setStartAuthorisationMode(START_AUTHORISATION_MODE.getValue());
        return setting;
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
