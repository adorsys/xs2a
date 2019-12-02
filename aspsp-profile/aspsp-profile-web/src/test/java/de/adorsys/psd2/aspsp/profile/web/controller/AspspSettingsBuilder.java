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

package de.adorsys.psd2.aspsp.profile.web.controller;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkSetting;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;

public class AspspSettingsBuilder {
    private static final int ACCOUNT_ACCESS_FREQUENCY_PER_DAY = 5;
    private static final boolean AIS_PIS_SESSION_SUPPORTED = false;
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final String PIS_REDIRECT_LINK = "https://localhost/payment/confirmation/";
    private static final String AIS_REDIRECT_LINK = "https://localhost/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int MAX_CONSENT_VALIDITY_DAYS = 0;
    private static final int MAX_TRANSACTION_VALIDITY_DAYS = 0;
    private static final boolean GLOBAL_CONSENT_SUPPORTED = false;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORTED = false;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = false;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final boolean PAYMENT_CANCELLATION_AUTHORISATION_MANDATED = false;
    private static final boolean PIIS_CONSENT_SUPPORTED = false;
    private static final boolean DELTA_LIST_SUPPORTED = false;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS = 86400000;
    private static final String PIS_PAYMENT_CANCELLATION_REDIRECT_URL_TO_ASPSP = "https://localhost/payment/cancellation/";
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = false;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED = false;
    private static final boolean SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = false;
    private static final boolean FORCE_XS2A_BASE_LINKS_URL = false;
    private static final String XS2A_BASE_LINKS_URL = "http://myhost.com/";
    private static final ScaRedirectFlow SCA_REDIRECT_FLOW = ScaRedirectFlow.REDIRECT;
    private static final String OAUTH_CONFIGURATION_URL = "http://localhost:4200/idp/";
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;
    private static final boolean ACCOUNT_OWNER_INFORMATION_SUPPORTED = true;
    private static final String COUNTRY_VALIDATION_SUPPORTED = "DE";
    private static final List<String> SUPPORTED_TRANSACTION_STATUS_FORMATS = Arrays.asList("application/json", "application/xml");
    private static final boolean IS_CHECK_TPP_ROLES_FROM_CERTIFICATE = true;
    private static final List<NotificationSupportedMode> ASPSP_NOTIFICATIONS_SUPPORTED = Collections.singletonList(NotificationSupportedMode.NONE);

    private AspspSettingsBuilder() {
    }

    static AspspSettings buildAspspSettings() {
        ConsentTypeSetting consentTypes = new ConsentTypeSetting(BANK_OFFERED_CONSENT_SUPPORTED,
                                                                 GLOBAL_CONSENT_SUPPORTED,
                                                                 AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED,
                                                                 ACCOUNT_ACCESS_FREQUENCY_PER_DAY,
                                                                 NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS,
                                                                 MAX_CONSENT_VALIDITY_DAYS,
                                                                 ACCOUNT_OWNER_INFORMATION_SUPPORTED);
        AisRedirectLinkSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkSetting(AIS_REDIRECT_LINK);
        AisTransactionSetting transactionParameters = new AisTransactionSetting(AVAILABLE_BOOKING_STATUSES,
                                                                                TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED,
                                                                                SUPPORTED_TRANSACTION_APPLICATION_TYPES);
        DeltaReportSetting deltaReportSettings = new DeltaReportSetting(ENTRY_REFERENCE_FROM_SUPPORTED,
                                                                        DELTA_LIST_SUPPORTED);
        OneTimeConsentScaSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaSetting(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED);
        AisAspspProfileSetting ais = new AisAspspProfileSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);
        PisRedirectLinkSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkSetting(PIS_REDIRECT_LINK,
                                                                                           PIS_PAYMENT_CANCELLATION_REDIRECT_URL_TO_ASPSP,
                                                                                           PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        PisAspspProfileSetting pis = new PisAspspProfileSetting(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX,
                                                                MAX_TRANSACTION_VALIDITY_DAYS,
                                                                NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS,
                                                                PAYMENT_CANCELLATION_AUTHORISATION_MANDATED,
                                                                pisRedirectLinkToOnlineBanking,
                                                                COUNTRY_VALIDATION_SUPPORTED,
                                                                SUPPORTED_TRANSACTION_STATUS_FORMATS
        );
        PiisAspspProfileSetting piis = new PiisAspspProfileSetting(PIIS_CONSENT_SUPPORTED);
        CommonAspspProfileSetting common = new CommonAspspProfileSetting(SCA_REDIRECT_FLOW,
                                                                         OAUTH_CONFIGURATION_URL,
                                                                         START_AUTHORISATION_MODE,
                                                                         TPP_SIGNATURE_REQUIRED,
                                                                         PSU_IN_INITIAL_REQUEST_MANDATED,
                                                                         REDIRECT_URL_EXPIRATION_TIME_MS,
                                                                         AUTHORISATION_EXPIRATION_TIME_MS,
                                                                         FORCE_XS2A_BASE_LINKS_URL,
                                                                         XS2A_BASE_LINKS_URL,
                                                                         SUPPORTED_ACCOUNT_REFERENCE_FIELDS,
                                                                         MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED,
                                                                         AIS_PIS_SESSION_SUPPORTED,
                                                                         SIGNING_BASKET_SUPPORTED,
                                                                         IS_CHECK_TPP_ROLES_FROM_CERTIFICATE,
                                                                         ASPSP_NOTIFICATIONS_SUPPORTED);

        return new AspspSettings(ais, pis, piis, common);
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
        Map<PaymentType, Set<String>> matrix = new EnumMap<>(PaymentType.class);
        Set<String> availablePaymentProducts = Collections.singleton("sepa-credit-transfers");
        matrix.put(PaymentType.SINGLE, availablePaymentProducts);
        return matrix;
    }
}
