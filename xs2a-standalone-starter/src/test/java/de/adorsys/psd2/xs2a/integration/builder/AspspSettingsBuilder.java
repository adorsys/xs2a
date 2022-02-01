/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileSetting;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;

public class AspspSettingsBuilder {
    private static final int ACCOUNT_ACCESS_FREQUENCY_PER_DAY = 5;
    private static final boolean AIS_PIS_SESSION_SUPPORTED = false;
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final String PIS_REDIRECT_LINK = "http://localhost:4200/pis/{redirect-id}/";
    private static final String AIS_REDIRECT_LINK = "http://localhost:4200/ais/{redirect-id}/";
    private static final String PIIS_REDIRECT_LINK = "http://localhost:4200/piis/{redirect-id}/";
    private static final String SB_REDIRECT_LINK = "http://localhost:4200/signing-basket/{redirect-id}/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int MAX_CONSENT_VALIDITY_DAYS = 0;
    private static final int MAX_TRANSACTION_VALIDITY_DAYS = 0;
    private static final boolean GLOBAL_CONSENT_SUPPORTED = true;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORTED = true;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = false;
    private static final boolean SIGNING_BASKET_SUPPORTED = false;
    private static final int SIGNING_BASKET_MAX_ENTRIES = 10;
    private static final boolean PAYMENT_CANCELLATION_AUTHORISATION_MANDATED = false;
    private static final PiisConsentSupported PIIS_CONSENT_SUPPORTED = PiisConsentSupported.NOT_SUPPORTED;
    private static final boolean DELTA_LIST_SUPPORTED = false;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_SIGNING_BASKET_EXPIRATION_TIME_MS = 86400000;
    private static final String PIS_PAYMENT_CANCELLATION_REDIRECT_URL_TO_ASPSP = "http://localhost:4200/pis/cancellation/{redirect-id}/";
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = true;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_ACCOUNTS_CONSENT_REQUIRED = false;
    private static final boolean SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = false;
    private static final boolean FORCE_XS2A_BASE_LINKS_URL = false;
    private static final String XS2A_BASE_LINKS_URL = "http://myhost.com/";
    private static final ScaRedirectFlow SCA_REDIRECT_FLOW = ScaRedirectFlow.REDIRECT;
    private static final String OAUTH_CONFIGURATION_URL = "http://localhost:4200/idp/";
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;
    private static final boolean ACCOUNT_OWNER_INFORMATION_SUPPORTED = true;
    private static final boolean TRUSTED_BENEFICIARIES_SUPPORTED = true;
    private static final String COUNTRY_VALIDATION_SUPPORTED = "DE";
    private static final List<String> SUPPORTED_TRANSACTION_STATUS_FORMATS = Arrays.asList("application/json", "application/xml");
    private static final List<NotificationSupportedMode> ASPSP_NOTIFICATIONS_SUPPORTED = Collections.singletonList(NotificationSupportedMode.NONE);
    private static final boolean AUTHORISATION_CONFIRMATION_REQUEST_MANDATED = false;
    private static final boolean AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A = false;
    private static final boolean CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED = false;
    private static final TppUriCompliance TPP_URI_COMPLIANCE_RESPONSE = TppUriCompliance.WARNING;

    public static AspspSettings buildAspspSettings() {
        return buildCustomAspspSettings(null, null, null, null);
    }

    public static AspspSettings buildAspspSettingsWithForcedXs2aBaseUrl(String xs2aBaseLinksUrl) {
        return buildCustomAspspSettings(xs2aBaseLinksUrl, null, null, null);
    }

    public static AspspSettings buildAspspSettingsWithSigningBasketSupported(boolean signingBasketSupported) {
        return buildCustomAspspSettings(null, signingBasketSupported, null, null);
    }

    public static AspspSettings buildAspspSettingsWithStartAuthorisationMode(StartAuthorisationMode startAuthorisationMode) {
        return buildCustomAspspSettings(null, null, null, startAuthorisationMode);
    }

    public static AspspSettings buildAspspSettingsWithScaRedirectFlow(ScaRedirectFlow scaRedirectFlow) {
        return buildCustomAspspSettings(null, null, scaRedirectFlow, null);
    }

    public static AspspSettings buildAspspSettingsWithStartAuthorisationModeAndSigningBasketSupported(StartAuthorisationMode startAuthorisationMode, boolean signingBasketSupported) {
        return buildCustomAspspSettings(null, signingBasketSupported, null, startAuthorisationMode);
    }

    private static AspspSettings buildCustomAspspSettings(String xs2aBaseLinksUrl, Boolean signingBasketSupported, ScaRedirectFlow scaRedirectFlow, StartAuthorisationMode startAuthorisationMode) {
        ConsentTypeSetting consentTypes = new ConsentTypeSetting(BANK_OFFERED_CONSENT_SUPPORTED,
                                                                 GLOBAL_CONSENT_SUPPORTED,
                                                                 AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED,
                                                                 ACCOUNT_ACCESS_FREQUENCY_PER_DAY,
                                                                 NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS,
                                                                 MAX_CONSENT_VALIDITY_DAYS,
                                                                 ACCOUNT_OWNER_INFORMATION_SUPPORTED,
                                                                 TRUSTED_BENEFICIARIES_SUPPORTED);
        AisRedirectLinkSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkSetting(AIS_REDIRECT_LINK);
        AisTransactionSetting transactionParameters = new AisTransactionSetting(AVAILABLE_BOOKING_STATUSES,
                                                                                TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED,
                                                                                SUPPORTED_TRANSACTION_APPLICATION_TYPES);
        DeltaReportSetting deltaReportSettings = new DeltaReportSetting(ENTRY_REFERENCE_FROM_SUPPORTED,
                                                                        DELTA_LIST_SUPPORTED);
        OneTimeConsentScaSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaSetting(SCA_BY_ONE_TIME_AVAILABLE_ACCOUNTS_CONSENT_REQUIRED, SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED);
        AisAspspProfileSetting ais = new AisAspspProfileSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);
        PisRedirectLinkSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkSetting(PIS_REDIRECT_LINK,
                                                                                           PIS_PAYMENT_CANCELLATION_REDIRECT_URL_TO_ASPSP,
                                                                                           PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        PisAspspProfileSetting pis = new PisAspspProfileSetting(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX,
                                                                MAX_TRANSACTION_VALIDITY_DAYS,
                                                                NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS,
                                                                PAYMENT_CANCELLATION_AUTHORISATION_MANDATED,
                                                                pisRedirectLinkToOnlineBanking,
                                                                COUNTRY_VALIDATION_SUPPORTED, SUPPORTED_TRANSACTION_STATUS_FORMATS,
                                                                false);
        PiisAspspProfileSetting piis = new PiisAspspProfileSetting(PIIS_CONSENT_SUPPORTED, new PiisRedirectLinkSetting(PIIS_REDIRECT_LINK));
        SbAspspProfileSetting sb = new SbAspspProfileSetting(signingBasketSupported == null ? SIGNING_BASKET_SUPPORTED : signingBasketSupported,
                                                             SIGNING_BASKET_MAX_ENTRIES,
                                                             NOT_CONFIRMED_SIGNING_BASKET_EXPIRATION_TIME_MS,
                                                             SB_REDIRECT_LINK);
        CommonAspspProfileSetting common = new CommonAspspProfileSetting(scaRedirectFlow == null ? SCA_REDIRECT_FLOW : scaRedirectFlow,
                                                                         OAUTH_CONFIGURATION_URL,
                                                                         startAuthorisationMode == null ? START_AUTHORISATION_MODE : startAuthorisationMode,
                                                                         TPP_SIGNATURE_REQUIRED,
                                                                         PSU_IN_INITIAL_REQUEST_MANDATED,
                                                                         REDIRECT_URL_EXPIRATION_TIME_MS,
                                                                         AUTHORISATION_EXPIRATION_TIME_MS,
                                                                         xs2aBaseLinksUrl != null || FORCE_XS2A_BASE_LINKS_URL,
                                                                         xs2aBaseLinksUrl == null ? XS2A_BASE_LINKS_URL : xs2aBaseLinksUrl,
                                                                         SUPPORTED_ACCOUNT_REFERENCE_FIELDS,
                                                                         MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED,
                                                                         AIS_PIS_SESSION_SUPPORTED,
                                                                         true,
                                                                         ASPSP_NOTIFICATIONS_SUPPORTED,
                                                                         AUTHORISATION_CONFIRMATION_REQUEST_MANDATED,
                                                                         AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A,
                                                                         CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED,
                                                                         TPP_URI_COMPLIANCE_RESPONSE,
                                                                         false,
                                                                         false);

        return new AspspSettings(ais, pis, piis, sb, common);
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
        Set<String> availablePaymentProducts = new HashSet<>();
        availablePaymentProducts.add("sepa-credit-transfers");
        availablePaymentProducts.add("custom-payment");

        matrix.put(PaymentType.SINGLE, availablePaymentProducts);
        matrix.put(PaymentType.PERIODIC, availablePaymentProducts);
        matrix.put(PaymentType.BULK, availablePaymentProducts);
        return matrix;
    }
}
