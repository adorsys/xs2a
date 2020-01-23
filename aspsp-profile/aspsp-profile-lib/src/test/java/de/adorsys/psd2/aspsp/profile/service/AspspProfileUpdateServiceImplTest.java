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
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspProfileUpdateServiceImplTest {
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
    private static final boolean DELTA_LIST_SUPPORTED = true;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS = 86400000;
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = true;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = true;
    private static final boolean FORCE_XS2A_BASE_URL = true;
    private static final String XS2A_BASE_URL = "http://myhost.com/";
    private static final ScaRedirectFlow SCA_REDIRECT_FLOW = ScaRedirectFlow.REDIRECT;
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;
    private static final boolean CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED = false;

    @InjectMocks
    private AspspProfileUpdateServiceImpl aspspProfileUpdateService;

    @Mock
    private ProfileConfiguration profileConfiguration;

    @BeforeEach
    void setUp() {
        when(profileConfiguration.getSetting()).thenReturn(new BankProfileSetting());
    }

    @Test
    void updateScaApproaches_success() {
        //When:
        aspspProfileUpdateService.updateScaApproaches(Collections.singletonList(REDIRECT_APPROACH));

        //Then:
        assertEquals(Collections.singletonList(REDIRECT_APPROACH), profileConfiguration.getSetting().getScaApproaches());
    }

    @Test
    void updateAspspSettings_success() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(new AspspSettings(FREQUENCY_PER_DAY, COMBINED_SERVICE_INDICATOR, TPP_SIGNATURE_REQUIRED, PIS_REDIRECT_LINK, AIS_REDIRECT_LINK,
                                                                        MULTICURRENCY_ACCOUNT_LEVEL, BANK_OFFERED_CONSENT_SUPPORT, AVAILABLE_BOOKING_STATUSES, SUPPORTED_ACCOUNT_REFERENCE_FIELDS, CONSENT_LIFETIME, TRANSACTION_LIFETIME, ALL_PSD_2_SUPPORT,
                                                                        TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED, SIGNING_BASKET_SUPPORTED, PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED, PIIS_CONSENT_SUPPORTED, REDIRECT_URL_EXPIRATION_TIME_MS, AUTHORISATION_EXPIRATION_TIME_MS,
                                                                        PIS_CANCELLATION_REDIRECT_LINK, NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS, NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS, SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX, PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS,
                                                                        AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED, SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, PSU_IN_INITIAL_REQUEST_MANDATED, FORCE_XS2A_BASE_URL, XS2A_BASE_URL, SCA_REDIRECT_FLOW, DELTA_LIST_SUPPORTED, ENTRY_REFERENCE_FROM_SUPPORTED, SUPPORTED_TRANSACTION_APPLICATION_TYPES,
                                                                        START_AUTHORISATION_MODE, CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED));

        //Then:
        BankProfileSetting setting = profileConfiguration.getSetting();
       assertEquals(FREQUENCY_PER_DAY, setting.getFrequencyPerDay());
       assertEquals(COMBINED_SERVICE_INDICATOR, setting.isCombinedServiceIndicator());
       assertEquals(TPP_SIGNATURE_REQUIRED, setting.isTppSignatureRequired());
       assertEquals(PIS_REDIRECT_LINK, setting.getPisRedirectUrlToAspsp());
       assertEquals(AIS_REDIRECT_LINK, setting.getAisRedirectUrlToAspsp());
       assertEquals(MULTICURRENCY_ACCOUNT_LEVEL, setting.getMulticurrencyAccountLevel());
       assertEquals(BANK_OFFERED_CONSENT_SUPPORT, setting.isBankOfferedConsentSupport());
       assertEquals(AVAILABLE_BOOKING_STATUSES, setting.getAvailableBookingStatuses());
       assertEquals(SUPPORTED_ACCOUNT_REFERENCE_FIELDS, setting.getSupportedAccountReferenceFields());
       assertEquals(CONSENT_LIFETIME, setting.getConsentLifetime());
       assertEquals(TRANSACTION_LIFETIME, setting.getTransactionLifetime());
       assertEquals(ALL_PSD_2_SUPPORT, setting.isAllPsd2Support());
       assertEquals(TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED, setting.isTransactionsWithoutBalancesSupported());
       assertEquals(SIGNING_BASKET_SUPPORTED, setting.isSigningBasketSupported());
       assertEquals(PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED, setting.isPaymentCancellationAuthorizationMandated());
       assertEquals(PIIS_CONSENT_SUPPORTED, setting.isPiisConsentSupported());
       assertEquals(DELTA_LIST_SUPPORTED, setting.isDeltaListSupported());
       assertEquals(REDIRECT_URL_EXPIRATION_TIME_MS, setting.getRedirectUrlExpirationTimeMs());
       assertEquals(AUTHORISATION_EXPIRATION_TIME_MS, setting.getAuthorisationExpirationTimeMs());
       assertEquals(PIS_CANCELLATION_REDIRECT_LINK, setting.getPisPaymentCancellationRedirectUrlToAspsp());
       assertEquals(NOT_CONFIRMED_CONSENT_EXPIRATION_PERIOD_MS, setting.getNotConfirmedConsentExpirationPeriodMs());
       assertEquals(NOT_CONFIRMED_PAYMENT_EXPIRATION_PERIOD_MS, setting.getNotConfirmedPaymentExpirationPeriodMs());
       assertEquals(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX, setting.getSupportedPaymentTypeAndProductMatrix());
       assertEquals(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS, setting.getPaymentCancellationRedirectUrlExpirationTimeMs());
       assertEquals(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED, setting.isAvailableAccountsConsentSupported());
       assertEquals(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, setting.isScaByOneTimeAvailableAccountsConsentRequired());
       assertEquals(PSU_IN_INITIAL_REQUEST_MANDATED, setting.isPsuInInitialRequestMandated());
       assertEquals(FORCE_XS2A_BASE_URL, setting.isForceXs2aBaseUrl());
       assertEquals(XS2A_BASE_URL, setting.getXs2aBaseUrl());
       assertEquals(SCA_REDIRECT_FLOW, setting.getScaRedirectFlow());
       assertEquals(ENTRY_REFERENCE_FROM_SUPPORTED, setting.isEntryReferenceFromSupported());
       assertEquals(SUPPORTED_TRANSACTION_APPLICATION_TYPES, setting.getSupportedTransactionApplicationTypes());
       assertEquals(START_AUTHORISATION_MODE.getValue(), setting.getStartAuthorisationMode());
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
