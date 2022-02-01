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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileBankSetting;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
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
class AspspProfileServiceTest {
    private static final int ACCOUNT_ACCESS_FREQUENCY_PER_DAY = 5;
    private static final boolean AIS_PIS_SESSION_SUPPORTED = false;
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final ScaApproach REDIRECT_APPROACH = ScaApproach.REDIRECT;
    private static final String OAUTH_CONFIGURATION_URL = "http://localhost:4200/idp/";
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String PIS_CANCELLATION_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/cancellation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final String PIIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/piis/account/";
    private static final String SB_REDIRECT_LINK = "http://localhost:4200/signing-basket/{redirect-id}/{encrypted-basket-id}";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int MAX_CONSENT_VALIDITY_DAYS = 0;
    private static final int MAX_TRANSACTION_VALIDITY_DAYS = 0;
    private static final boolean GLOBAL_CONSENT_SUPPORTED = false;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORTED = false;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = false;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final int SIGNING_BASKET_MAX_ENTRIES = 10;
    private static final boolean PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED = false;
    private static final PiisConsentSupported PIIS_CONSENT_SUPPORTED = PiisConsentSupported.NOT_SUPPORTED;
    private static final boolean DELTA_LIST_SUPPORTED = false;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_SIGNING_BASKET_EXPIRATION_TIME_MS = 86400000;
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = true;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED = true;
    private static final boolean SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = false;
    private static final boolean FORCE_XS2A_BASE_LINKS_URL = false;
    private static final String XS2A_BASE_LINKS_URL = "http://myhost.com/";
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;
    private static final ScaRedirectFlow SCA_REDIRECT_FLOW = ScaRedirectFlow.REDIRECT;
    private static final boolean ACCOUNT_OWNER_INFORMATION_SUPPORTED = true;
    private static final boolean TRUSTED_BENEFICIARIES_SUPPORTED = true;
    private static final String COUNTRY_VALIDATION_SUPPORTED = "DE";
    private static final List<String> SUPPORTED_TRANSACTION_STATUS_FORMATS = Arrays.asList("application/json", "application/xml");
    private static final boolean IS_CHECK_TPP_ROLES_FROM_CERTIFICATE = true;
    private static final List<NotificationSupportedMode> ASPSP_NOTIFICATIONS_SUPPORTED = Collections.singletonList(NotificationSupportedMode.NONE);
    private static final boolean AUTHORISATION_CONFIRMATION_REQUEST_MANDATED = false;
    private static final boolean AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A = false;
    private static final boolean CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED = false;
    private static final String INSTANCE_ID = "bank1";
    private static final TppUriCompliance TPP_URI_COMPLIANCE_RESPONSE = TppUriCompliance.WARNING;

    @InjectMocks
    private AspspProfileServiceImpl aspspProfileService;

    @Mock
    private ProfileConfigurations profileConfigurations;
    private AspspSettings actualResponse;

    @BeforeEach
    void setUpAccountServiceMock() {
        when(profileConfigurations.getSetting(INSTANCE_ID)).thenReturn(buildBankProfileSetting());

        actualResponse = aspspProfileService.getAspspSettings(INSTANCE_ID);
    }

    @Test
    void getPisRedirectUrlToAspsp_success() {
        assertEquals(PIS_REDIRECT_LINK, actualResponse.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp());
    }

    @Test
    void getPisPaymentCancellationRedirectUrlToAspsp_success() {
        assertEquals(PIS_CANCELLATION_REDIRECT_LINK, actualResponse.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp());
    }

    @Test
    void getAisRedirectUrlToAspsp_success() {
        assertEquals(AIS_REDIRECT_LINK, actualResponse.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp());
    }

    @Test
    void getAvailablePaymentTypes_success() {
        assertEquals(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX, actualResponse.getPis().getSupportedPaymentTypeAndProductMatrix());
    }

    @Test
    void getScaApproach_success() {
        //When:
        List<ScaApproach> actualResponse = aspspProfileService.getScaApproaches(INSTANCE_ID);

        //Then:
        assertEquals(Collections.singletonList(REDIRECT_APPROACH), actualResponse);
    }

    @Test
    void getRedirectUrlExpirationTimeMs_success() {
        assertEquals(REDIRECT_URL_EXPIRATION_TIME_MS, actualResponse.getCommon().getRedirectUrlExpirationTimeMs());
    }

    @Test
    void getFrequencyPerDay_success() {
        assertEquals(ACCOUNT_ACCESS_FREQUENCY_PER_DAY, actualResponse.getAis().getConsentTypes().getAccountAccessFrequencyPerDay());
    }

    @Test
    void getNotConfirmedConsentExpirationPeriodMs_success() {
        assertEquals(NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS, actualResponse.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs());
    }

    @Test
    void getNotConfirmedPaymentExpirationPeriodMs_success() {
        assertEquals(NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS, actualResponse.getPis().getNotConfirmedPaymentExpirationTimeMs());
    }

    @Test
    void getPaymentCancellationRedirectUrlExpirationTimeMs_success() {
        assertEquals(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS, actualResponse.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs());
    }

    @Test
    void getAuthorisationExpirationTimeMs_success() {
        assertEquals(AUTHORISATION_EXPIRATION_TIME_MS, actualResponse.getCommon().getAuthorisationExpirationTimeMs());
    }

    @Test
    void getAvailableAccountsConsentSupported_success() {
        assertEquals(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED, actualResponse.getAis().getConsentTypes().isAvailableAccountsConsentSupported());
    }

    @Test
    void getScaByOneTimeAvailableAccountsConsentRequired_success() {
        assertEquals(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, actualResponse.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired());
    }

    @Test
    void getPsuInInitialRequestMandated_success() {
        assertEquals(PSU_IN_INITIAL_REQUEST_MANDATED, actualResponse.getCommon().isPsuInInitialRequestMandated());
    }

    @Test
    void getForceXs2aBaseUrl_success() {
        assertEquals(FORCE_XS2A_BASE_LINKS_URL, actualResponse.getCommon().isForceXs2aBaseLinksUrl());
    }

    @Test
    void getXs2aBaseUrl_success() {
        assertEquals(XS2A_BASE_LINKS_URL, actualResponse.getCommon().getXs2aBaseLinksUrl());
    }

    @Test
    void getEntryReferenceFromSupported_success() {
        assertEquals(ENTRY_REFERENCE_FROM_SUPPORTED, actualResponse.getAis().getDeltaReportSettings().isEntryReferenceFromSupported());
    }

    @Test
    void supportedTransactionApplicationTypes_success() {
        assertEquals(SUPPORTED_TRANSACTION_APPLICATION_TYPES, actualResponse.getAis().getTransactionParameters().getSupportedTransactionApplicationTypes());
    }

    @Test
    void getStartAuthorisationMode() {
        assertEquals(START_AUTHORISATION_MODE, actualResponse.getCommon().getStartAuthorisationMode());
    }

    @Test
    void getScaRedirectFlow() {
        assertEquals(SCA_REDIRECT_FLOW, actualResponse.getCommon().getScaRedirectFlow());
    }

    private BankProfileSetting buildBankProfileSetting() {
        ConsentTypeBankSetting consentTypes = new ConsentTypeBankSetting(BANK_OFFERED_CONSENT_SUPPORTED,
                                                                         GLOBAL_CONSENT_SUPPORTED,
                                                                         AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED,
                                                                         ACCOUNT_ACCESS_FREQUENCY_PER_DAY,
                                                                         NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS,
                                                                         MAX_CONSENT_VALIDITY_DAYS,
                                                                         ACCOUNT_OWNER_INFORMATION_SUPPORTED,
                                                                         TRUSTED_BENEFICIARIES_SUPPORTED);
        AisRedirectLinkBankSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkBankSetting(AIS_REDIRECT_LINK);
        AisTransactionBankSetting transactionParameters = new AisTransactionBankSetting(AVAILABLE_BOOKING_STATUSES,
                                                                                        TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED,
                                                                                        SUPPORTED_TRANSACTION_APPLICATION_TYPES);
        DeltaReportBankSetting deltaReportSettings = new DeltaReportBankSetting(ENTRY_REFERENCE_FROM_SUPPORTED,
                                                                                DELTA_LIST_SUPPORTED);
        OneTimeConsentScaBankSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaBankSetting(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED);
        AisAspspProfileBankSetting ais = new AisAspspProfileBankSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);
        PisRedirectLinkBankSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkBankSetting(PIS_REDIRECT_LINK,
                                                                                                   PIS_CANCELLATION_REDIRECT_LINK,
                                                                                                   PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        PisAspspProfileBankSetting pis = new PisAspspProfileBankSetting(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX,
                                                                        MAX_TRANSACTION_VALIDITY_DAYS,
                                                                        NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS,
                                                                        PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED,
                                                                        pisRedirectLinkToOnlineBanking,
                                                                        COUNTRY_VALIDATION_SUPPORTED,
                                                                        SUPPORTED_TRANSACTION_STATUS_FORMATS,
                                                                        false);
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting(PIIS_CONSENT_SUPPORTED, new PiisRedirectLinkBankSetting(PIIS_REDIRECT_LINK));
        SbAspspProfileBankSetting sb = new SbAspspProfileBankSetting(SIGNING_BASKET_SUPPORTED,
                                                                     SIGNING_BASKET_MAX_ENTRIES,
                                                                     NOT_CONFIRMED_SIGNING_BASKET_EXPIRATION_TIME_MS,
                                                                     SB_REDIRECT_LINK);
        CommonAspspProfileBankSetting common = new CommonAspspProfileBankSetting(Collections.singletonList(REDIRECT_APPROACH),
                                                                                 SCA_REDIRECT_FLOW,
                                                                                 OAUTH_CONFIGURATION_URL,
                                                                                 START_AUTHORISATION_MODE.getValue(),
                                                                                 TPP_SIGNATURE_REQUIRED,
                                                                                 PSU_IN_INITIAL_REQUEST_MANDATED,
                                                                                 REDIRECT_URL_EXPIRATION_TIME_MS,
                                                                                 AUTHORISATION_EXPIRATION_TIME_MS,
                                                                                 FORCE_XS2A_BASE_LINKS_URL,
                                                                                 XS2A_BASE_LINKS_URL,
                                                                                 SUPPORTED_ACCOUNT_REFERENCE_FIELDS,
                                                                                 MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED,
                                                                                 AIS_PIS_SESSION_SUPPORTED,
                                                                                 IS_CHECK_TPP_ROLES_FROM_CERTIFICATE,
                                                                                 ASPSP_NOTIFICATIONS_SUPPORTED,
                                                                                 AUTHORISATION_CONFIRMATION_REQUEST_MANDATED,
                                                                                 AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A,
                                                                                 CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED,
                                                                                 TPP_URI_COMPLIANCE_RESPONSE,
                                                                                 false,
                                                                                 false);
        return new BankProfileSetting(ais, pis, piis, sb, common);
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
