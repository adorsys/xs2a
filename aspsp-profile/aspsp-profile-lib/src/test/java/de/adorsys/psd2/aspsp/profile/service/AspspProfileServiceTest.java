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
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
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
public class AspspProfileServiceTest {
    private static final int ACCOUNT_ACCESS_FREQUENCY_PER_DAY = 5;
    private static final boolean AIS_PIS_SESSION_SUPPORTED = false;
    private static final boolean TPP_SIGNATURE_REQUIRED = false;
    private static final ScaApproach REDIRECT_APPROACH = ScaApproach.REDIRECT;
    private static final String OAUTH_CONFIGURATION_URL = "http://localhost:4200/idp/";
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String PIS_CANCELLATION_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/cancellation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int MAX_CONSENT_VALIDITY_DAYS = 0;
    private static final int MAX_TRANSACTION_VALIDITY_DAYS = 0;
    private static final boolean GLOBAL_CONSENT_SUPPORTED = false;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORTED = false;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = false;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final boolean PAYMENT_CANCELLATION_AUTHORIZATION_MANDATED = false;
    private static final boolean PIIS_CONSENT_SUPPORTED = false;
    private static final boolean DELTA_LIST_SUPPORTED = false;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS = 86400000;
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
    private static final String COUNTRY_VALIDATION_SUPPORTED = "DE";
    private static final List<String> SUPPORTED_TRANSACTION_STATUS_FORMATS = Arrays.asList("application/json", "application/xml");
    private static final boolean IS_CHECK_TPP_ROLES_FROM_CERTIFICATE = true;
    private static final List<NotificationSupportedMode> ASPSP_NOTIFICATIONS_SUPPORTED = Collections.singletonList(NotificationSupportedMode.NONE);
    private static final boolean AUTHORISATION_CONFIRMATION_REQUEST_MANDATED = false;
    private static final boolean AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A = false;
    private static final boolean CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED = false;

    @InjectMocks
    private AspspProfileServiceImpl aspspProfileService;

    @Mock
    private ProfileConfiguration profileConfiguration;
    private AspspSettings actualResponse;

    @Before
    public void setUpAccountServiceMock() {
        when(profileConfiguration.getSetting()).thenReturn(buildBankProfileSetting());

        actualResponse = aspspProfileService.getAspspSettings();
    }

    @Test
    public void getPisRedirectUrlToAspsp_success() {
        Assertions.assertThat(actualResponse.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp()).isEqualTo(PIS_REDIRECT_LINK);
    }

    @Test
    public void getPisPaymentCancellationRedirectUrlToAspsp_success() {
        Assertions.assertThat(actualResponse.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp()).isEqualTo(PIS_CANCELLATION_REDIRECT_LINK);
    }

    @Test
    public void getAisRedirectUrlToAspsp_success() {
        Assertions.assertThat(actualResponse.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp()).isEqualTo(AIS_REDIRECT_LINK);
    }

    @Test
    public void getAvailablePaymentTypes_success() {
        Assertions.assertThat(actualResponse.getPis().getSupportedPaymentTypeAndProductMatrix()).isEqualTo(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX);
    }

    @Test
    public void getScaApproach_success() {
        //When:
        List<ScaApproach> actualResponse = aspspProfileService.getScaApproaches();

        //Then:
        Assertions.assertThat(actualResponse).isEqualTo(Collections.singletonList(REDIRECT_APPROACH));
    }

    @Test
    public void getRedirectUrlExpirationTimeMs_success() {
        Assertions.assertThat(actualResponse.getCommon().getRedirectUrlExpirationTimeMs()).isEqualTo(REDIRECT_URL_EXPIRATION_TIME_MS);
    }

    @Test
    public void getFrequencyPerDay_success() {
        Assertions.assertThat(actualResponse.getAis().getConsentTypes().getAccountAccessFrequencyPerDay()).isEqualTo(ACCOUNT_ACCESS_FREQUENCY_PER_DAY);
    }

    @Test
    public void getNotConfirmedConsentExpirationPeriodMs_success() {
        Assertions.assertThat(actualResponse.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs()).isEqualTo(NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS);
    }

    @Test
    public void getNotConfirmedPaymentExpirationPeriodMs_success() {
        Assertions.assertThat(actualResponse.getPis().getNotConfirmedPaymentExpirationTimeMs()).isEqualTo(NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS);
    }

    @Test
    public void getPaymentCancellationRedirectUrlExpirationTimeMs_success() {
        Assertions.assertThat(actualResponse.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs()).isEqualTo(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
    }

    @Test
    public void getAuthorisationExpirationTimeMs_success() {
        Assertions.assertThat(actualResponse.getCommon().getAuthorisationExpirationTimeMs()).isEqualTo(AUTHORISATION_EXPIRATION_TIME_MS);
    }

    @Test
    public void getAvailableAccountsConsentSupported_success() {
        Assertions.assertThat(actualResponse.getAis().getConsentTypes().isAvailableAccountsConsentSupported()).isEqualTo(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED);
    }

    @Test
    public void getScaByOneTimeAvailableAccountsConsentRequired_success() {
        Assertions.assertThat(actualResponse.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired()).isEqualTo(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED);
    }

    @Test
    public void getPsuInInitialRequestMandated_success() {
        Assertions.assertThat(actualResponse.getCommon().isPsuInInitialRequestMandated()).isEqualTo(PSU_IN_INITIAL_REQUEST_MANDATED);
    }

    @Test
    public void getForceXs2aBaseUrl_success() {
        Assertions.assertThat(actualResponse.getCommon().isForceXs2aBaseLinksUrl()).isEqualTo(FORCE_XS2A_BASE_LINKS_URL);
    }

    @Test
    public void getXs2aBaseUrl_success() {
        Assertions.assertThat(actualResponse.getCommon().getXs2aBaseLinksUrl()).isEqualTo(XS2A_BASE_LINKS_URL);
    }

    @Test
    public void getEntryReferenceFromSupported_success() {
        Assertions.assertThat(actualResponse.getAis().getDeltaReportSettings().isEntryReferenceFromSupported()).isEqualTo(ENTRY_REFERENCE_FROM_SUPPORTED);
    }

    @Test
    public void supportedTransactionApplicationTypes_success() {
        Assertions.assertThat(actualResponse.getAis().getTransactionParameters().getSupportedTransactionApplicationTypes()).isEqualTo(SUPPORTED_TRANSACTION_APPLICATION_TYPES);
    }

    @Test
    public void getStartAuthorisationMode() {
        Assertions.assertThat(actualResponse.getCommon().getStartAuthorisationMode()).isEqualTo(START_AUTHORISATION_MODE);
    }

    @Test
    public void getScaRedirectFlow() {
        Assertions.assertThat(actualResponse.getCommon().getScaRedirectFlow()).isEqualTo(SCA_REDIRECT_FLOW);
    }

    private BankProfileSetting buildBankProfileSetting() {
        ConsentTypeBankSetting consentTypes = new ConsentTypeBankSetting(BANK_OFFERED_CONSENT_SUPPORTED,
                                                                         GLOBAL_CONSENT_SUPPORTED,
                                                                         AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED,
                                                                         ACCOUNT_ACCESS_FREQUENCY_PER_DAY,
                                                                         NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS,
                                                                         MAX_CONSENT_VALIDITY_DAYS,
                                                                         ACCOUNT_OWNER_INFORMATION_SUPPORTED);
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
                                                                        SUPPORTED_TRANSACTION_STATUS_FORMATS);
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting(PIIS_CONSENT_SUPPORTED);
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
                                                                                 SIGNING_BASKET_SUPPORTED,
                                                                                 IS_CHECK_TPP_ROLES_FROM_CERTIFICATE,
                                                                                 ASPSP_NOTIFICATIONS_SUPPORTED,
                                                                                 AUTHORISATION_CONFIRMATION_REQUEST_MANDATED,
                                                                                 AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A,
                                                                                 CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED);
        return new BankProfileSetting(ais, pis, piis, common);
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
