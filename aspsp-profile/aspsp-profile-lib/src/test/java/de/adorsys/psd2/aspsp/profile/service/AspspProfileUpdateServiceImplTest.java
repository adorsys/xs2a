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
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.mapper.AspspSettingsToBankProfileSettingMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AspspProfileUpdateServiceImplTest {
    private static final int ACCOUNT_ACCESS_FREQUENCY_PER_DAY = 5;
    private static final boolean AIS_PIS_SESSION_SUPPORTED = true;
    private static final boolean TPP_SIGNATURE_REQUIRED = true;
    private static final ScaApproach REDIRECT_APPROACH = ScaApproach.REDIRECT;
    private static final String OAUTH_CONFIGURATION_URL = "http://localhost:4200/idp/";
    private static final String PIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/confirmation/";
    private static final String PIS_CANCELLATION_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/payment/cancellation/";
    private static final String AIS_REDIRECT_LINK = "https://aspsp-mock-integ.cloud.adorsys.de/view/account/";
    private static final MulticurrencyAccountLevel MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED = MulticurrencyAccountLevel.SUBACCOUNT;
    private static final List<BookingStatus> AVAILABLE_BOOKING_STATUSES = getBookingStatuses();
    private static final List<SupportedAccountReferenceField> SUPPORTED_ACCOUNT_REFERENCE_FIELDS = getSupportedAccountReferenceFields();
    private static final int MAX_CONSENT_VALIDITY_DAYS = 10;
    private static final int MAX_TRANSACTION_VALIDITY_DAYS = 10;
    private static final boolean GLOBAL_CONSENT_SUPPORTED = true;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORTED = true;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = true;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final boolean PAYMENT_CANCELLATION_AUTHORISATION_MANDATED = true;
    private static final boolean PIIS_CONSENT_SUPPORTED = true;
    private static final boolean DELTA_LIST_SUPPORTED = true;
    private static final long REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final long AUTHORISATION_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS = 86400000;
    private static final long NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS = 86400000;
    private static final Map<PaymentType, Set<String>> SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX = buildSupportedPaymentTypeAndProductMatrix();
    private static final long PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS = 600000;
    private static final boolean AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED = true;
    private static final boolean SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED = true;
    private static final boolean SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED = true;
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = true;
    private static final boolean FORCE_XS2A_BASE_LINKS_URL = true;
    private static final String XS2A_BASE_LINKS_URL = "http://myhost.com/";
    private static final ScaRedirectFlow SCA_REDIRECT_FLOW = ScaRedirectFlow.REDIRECT;
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;
    private static final boolean ACCOUNT_OWNER_INFORMATION_SUPPORTED = true;
    private static final String COUNTRY_VALIDATION_SUPPORTED = "DE";
    private static final List<String> SUPPORTED_TRANSACTION_STATUS_FORMATS = Arrays.asList("application/json", "application/xml");
    private static final boolean IS_CHECK_TPP_ROLES_FROM_CERTIFICATE = true;
    private static final List<NotificationSupportedMode> ASPSP_NOTIFICATIONS_SUPPORTED = Collections.singletonList(NotificationSupportedMode.NONE);

    @InjectMocks
    private AspspProfileUpdateServiceImpl aspspProfileUpdateService;

    @Mock
    private ProfileConfiguration profileConfiguration;

    @Spy
    private AspspSettingsToBankProfileSettingMapper profileSettingMapper = Mappers.getMapper(AspspSettingsToBankProfileSettingMapper.class);

    @Before
    public void setUp() {
        when(profileConfiguration.getSetting()).thenReturn(buildBankProfileSetting());
    }

    @Test
    public void updateScaApproaches_success() {
        //When:
        aspspProfileUpdateService.updateScaApproaches(Collections.singletonList(REDIRECT_APPROACH));

        //Then:
        Assertions.assertThat(profileConfiguration.getSetting().getCommon().getScaApproachesSupported()).isEqualTo(Collections.singletonList(REDIRECT_APPROACH));
    }

    @Test
    public void updateAspspSettings_success() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(buildAspspSettings());

        //Then:
        BankProfileSetting setting = profileConfiguration.getSetting();
        Assertions.assertThat(setting.getAis().getConsentTypes().getAccountAccessFrequencyPerDay()).isEqualTo(ACCOUNT_ACCESS_FREQUENCY_PER_DAY);
        Assertions.assertThat(setting.getAis().getConsentTypes().getMaxConsentValidityDays()).isEqualTo(MAX_CONSENT_VALIDITY_DAYS);
        Assertions.assertThat(setting.getAis().getConsentTypes().isBankOfferedConsentSupported()).isEqualTo(BANK_OFFERED_CONSENT_SUPPORTED);
        Assertions.assertThat(setting.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs()).isEqualTo(NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.getAis().getConsentTypes().isAvailableAccountsConsentSupported()).isEqualTo(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED);
        Assertions.assertThat(setting.getAis().getConsentTypes().isGlobalConsentSupported()).isEqualTo(GLOBAL_CONSENT_SUPPORTED);
        Assertions.assertThat(setting.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp()).isEqualTo(AIS_REDIRECT_LINK);
        Assertions.assertThat(setting.getAis().getTransactionParameters().getAvailableBookingStatuses()).isEqualTo(AVAILABLE_BOOKING_STATUSES);
        Assertions.assertThat(setting.getAis().getTransactionParameters().isTransactionsWithoutBalancesSupported()).isEqualTo(TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED);
        Assertions.assertThat(setting.getAis().getTransactionParameters().getSupportedTransactionApplicationTypes()).isEqualTo(SUPPORTED_TRANSACTION_APPLICATION_TYPES);
        Assertions.assertThat(setting.getAis().getDeltaReportSettings().isDeltaListSupported()).isEqualTo(DELTA_LIST_SUPPORTED);
        Assertions.assertThat(setting.getAis().getDeltaReportSettings().isEntryReferenceFromSupported()).isEqualTo(ENTRY_REFERENCE_FROM_SUPPORTED);
        Assertions.assertThat(setting.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired()).isEqualTo(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED);
        Assertions.assertThat(setting.getPis().getMaxTransactionValidityDays()).isEqualTo(MAX_TRANSACTION_VALIDITY_DAYS);
        Assertions.assertThat(setting.getPis().isPaymentCancellationAuthorisationMandated()).isEqualTo(PAYMENT_CANCELLATION_AUTHORISATION_MANDATED);
        Assertions.assertThat(setting.getPis().getNotConfirmedPaymentExpirationTimeMs()).isEqualTo(NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.getPis().getSupportedPaymentTypeAndProductMatrix()).isEqualTo(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX);
        Assertions.assertThat(setting.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp()).isEqualTo(PIS_CANCELLATION_REDIRECT_LINK);
        Assertions.assertThat(setting.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs()).isEqualTo(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp()).isEqualTo(PIS_REDIRECT_LINK);
        Assertions.assertThat(setting.getPis().getCountryValidationSupported()).isEqualTo(COUNTRY_VALIDATION_SUPPORTED);
        Assertions.assertThat(setting.getPis().getSupportedTransactionStatusFormats()).isEqualTo(SUPPORTED_TRANSACTION_STATUS_FORMATS);
        Assertions.assertThat(setting.getPiis().isPiisConsentSupported()).isEqualTo(PIIS_CONSENT_SUPPORTED);
        Assertions.assertThat(setting.getCommon().isPsuInInitialRequestMandated()).isEqualTo(PSU_IN_INITIAL_REQUEST_MANDATED);
        Assertions.assertThat(setting.getCommon().isForceXs2aBaseLinksUrl()).isEqualTo(FORCE_XS2A_BASE_LINKS_URL);
        Assertions.assertThat(setting.getCommon().getAuthorisationExpirationTimeMs()).isEqualTo(AUTHORISATION_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.getCommon().getRedirectUrlExpirationTimeMs()).isEqualTo(REDIRECT_URL_EXPIRATION_TIME_MS);
        Assertions.assertThat(setting.getCommon().isSigningBasketSupported()).isEqualTo(SIGNING_BASKET_SUPPORTED);
        Assertions.assertThat(setting.getCommon().getStartAuthorisationMode()).isEqualTo(START_AUTHORISATION_MODE.getValue());
        Assertions.assertThat(setting.getCommon().getSupportedAccountReferenceFields()).isEqualTo(SUPPORTED_ACCOUNT_REFERENCE_FIELDS);
        Assertions.assertThat(setting.getCommon().getMulticurrencyAccountLevelSupported()).isEqualTo(MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED);
        Assertions.assertThat(setting.getCommon().isTppSignatureRequired()).isEqualTo(TPP_SIGNATURE_REQUIRED);
        Assertions.assertThat(setting.getCommon().isAisPisSessionsSupported()).isEqualTo(AIS_PIS_SESSION_SUPPORTED);
        Assertions.assertThat(setting.getCommon().getXs2aBaseLinksUrl()).isEqualTo(XS2A_BASE_LINKS_URL);
        Assertions.assertThat(setting.getCommon().getScaRedirectFlow()).isEqualTo(SCA_REDIRECT_FLOW);
    }

    private AspspSettings buildAspspSettings() {
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
                                                                                           PIS_CANCELLATION_REDIRECT_LINK,
                                                                                           PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS);
        PisAspspProfileSetting pis = new PisAspspProfileSetting(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX,
                                                                MAX_TRANSACTION_VALIDITY_DAYS,
                                                                NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS,
                                                                PAYMENT_CANCELLATION_AUTHORISATION_MANDATED,
                                                                pisRedirectLinkToOnlineBanking,
                                                                COUNTRY_VALIDATION_SUPPORTED,
                                                                SUPPORTED_TRANSACTION_STATUS_FORMATS);
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

    private BankProfileSetting buildBankProfileSetting() {
        ConsentTypeBankSetting consentTypes = new ConsentTypeBankSetting();
        AisRedirectLinkBankSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkBankSetting();
        AisTransactionBankSetting transactionParameters = new AisTransactionBankSetting();
        DeltaReportBankSetting deltaReportSettings = new DeltaReportBankSetting();
        OneTimeConsentScaBankSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaBankSetting();
        AisAspspProfileBankSetting ais = new AisAspspProfileBankSetting(consentTypes,
                                                                        aisRedirectLinkToOnlineBanking,
                                                                        transactionParameters,
                                                                        deltaReportSettings,
                                                                        scaRequirementsForOneTimeConsents);
        PisRedirectLinkBankSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkBankSetting();
        PisAspspProfileBankSetting pis = new PisAspspProfileBankSetting(null, 0, 0, false, pisRedirectLinkToOnlineBanking, COUNTRY_VALIDATION_SUPPORTED, new ArrayList<>());
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting();
        CommonAspspProfileBankSetting common = new CommonAspspProfileBankSetting();
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
