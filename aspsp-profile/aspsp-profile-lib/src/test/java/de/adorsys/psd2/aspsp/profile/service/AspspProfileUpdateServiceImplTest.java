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
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.mapper.AspspSettingsToBankProfileSettingMapper;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;
import static de.adorsys.psd2.xs2a.core.ais.BookingStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspProfileUpdateServiceImplTest {
    private static final int ACCOUNT_ACCESS_FREQUENCY_PER_DAY = 5;
    private static final boolean AIS_PIS_SESSION_SUPPORTED = true;
    private static final boolean TPP_SIGNATURE_REQUIRED = true;
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
    private static final int MAX_CONSENT_VALIDITY_DAYS = 10;
    private static final int MAX_TRANSACTION_VALIDITY_DAYS = 10;
    private static final boolean GLOBAL_CONSENT_SUPPORTED = true;
    private static final boolean BANK_OFFERED_CONSENT_SUPPORTED = true;
    private static final boolean TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED = true;
    private static final boolean SIGNING_BASKET_SUPPORTED = true;
    private static final int SIGNING_BASKET_MAX_ENTRIES = 10;
    private static final boolean PAYMENT_CANCELLATION_AUTHORISATION_MANDATED = true;
    private static final PiisConsentSupported PIIS_CONSENT_SUPPORTED = PiisConsentSupported.ASPSP_CONSENT_SUPPORTED;
    private static final boolean DELTA_LIST_SUPPORTED = true;
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
    private static final boolean PSU_IN_INITIAL_REQUEST_MANDATED = true;
    private static final boolean FORCE_XS2A_BASE_LINKS_URL = true;
    private static final String XS2A_BASE_LINKS_URL = "http://myhost.com/";
    private static final ScaRedirectFlow SCA_REDIRECT_FLOW = ScaRedirectFlow.REDIRECT;
    private static final boolean ENTRY_REFERENCE_FROM_SUPPORTED = true;
    private static final List<String> SUPPORTED_TRANSACTION_APPLICATION_TYPES = Arrays.asList("application/json", "application/xml");
    private static final StartAuthorisationMode START_AUTHORISATION_MODE = StartAuthorisationMode.AUTO;
    private static final boolean ACCOUNT_OWNER_INFORMATION_SUPPORTED = true;
    private static final boolean TRUSTED_BENEFICIARIES_SUPPORTED = true;
    private static final String COUNTRY_VALIDATION_SUPPORTED = "DE";
    private static final List<String> SUPPORTED_TRANSACTION_STATUS_FORMATS = Arrays.asList("application/json", "application/xml");
    private static final boolean IS_CHECK_TPP_ROLES_FROM_CERTIFICATE = true;
    private static final List<NotificationSupportedMode> ASPSP_NOTIFICATIONS_SUPPORTED = Collections.singletonList(NotificationSupportedMode.NONE);
    private static final boolean AUTHORISATION_CONFIRMATION_REQUEST_MANDATED = false;
    private static final boolean AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A = false;
    private static final boolean CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED = false;
    private static final TppUriCompliance TPP_URI_COMPLIANCE_RESPONSE = TppUriCompliance.WARNING;
    private static final String INSTANCE_ID = "bank1";

    @InjectMocks
    private AspspProfileUpdateServiceImpl aspspProfileUpdateService;

    @Mock
    private ProfileConfigurations profileConfigurations;

    @Spy
    private AspspSettingsToBankProfileSettingMapper profileSettingMapper = Mappers.getMapper(AspspSettingsToBankProfileSettingMapper.class);

    @BeforeEach
    void setUp() {
        when(profileConfigurations.getSetting(INSTANCE_ID)).thenReturn(buildBankProfileSetting());
    }

    @Test
    void updateScaApproaches_success() {
        //When:
        aspspProfileUpdateService.updateScaApproaches(Collections.singletonList(REDIRECT_APPROACH), INSTANCE_ID);

        //Then:
        assertEquals(Collections.singletonList(REDIRECT_APPROACH), profileConfigurations.getSetting(INSTANCE_ID).getCommon().getScaApproachesSupported());
    }

    @Test
    void updateAspspSettings_success_ais() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(buildAspspSettings(), INSTANCE_ID);

        //Then:
        BankProfileSetting setting = profileConfigurations.getSetting(INSTANCE_ID);
        assertEquals(ACCOUNT_ACCESS_FREQUENCY_PER_DAY, setting.getAis().getConsentTypes().getAccountAccessFrequencyPerDay());
        assertEquals(MAX_CONSENT_VALIDITY_DAYS, setting.getAis().getConsentTypes().getMaxConsentValidityDays());
        assertEquals(BANK_OFFERED_CONSENT_SUPPORTED, setting.getAis().getConsentTypes().isBankOfferedConsentSupported());
        assertEquals(NOT_CONFIRMED_CONSENT_EXPIRATION_TIME_MS, setting.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs());
        assertEquals(AVAILABLE_ACCOUNTS_CONSENT_SUPPORTED, setting.getAis().getConsentTypes().isAvailableAccountsConsentSupported());
        assertEquals(GLOBAL_CONSENT_SUPPORTED, setting.getAis().getConsentTypes().isGlobalConsentSupported());
        assertEquals(AIS_REDIRECT_LINK, setting.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp());
        assertEquals(AVAILABLE_BOOKING_STATUSES, setting.getAis().getTransactionParameters().getAvailableBookingStatuses());
        assertEquals(TRANSACTIONS_WITHOUT_BALANCES_SUPPORTED, setting.getAis().getTransactionParameters().isTransactionsWithoutBalancesSupported());
        assertEquals(SUPPORTED_TRANSACTION_APPLICATION_TYPES, setting.getAis().getTransactionParameters().getSupportedTransactionApplicationTypes());
        assertEquals(DELTA_LIST_SUPPORTED, setting.getAis().getDeltaReportSettings().isDeltaListSupported());
        assertEquals(ENTRY_REFERENCE_FROM_SUPPORTED, setting.getAis().getDeltaReportSettings().isEntryReferenceFromSupported());
        assertEquals(SCA_BY_ONE_TIME_AVAILABLE_CONSENT_REQUIRED, setting.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired());
    }

    @Test
    void updateAspspSettings_success_pis() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(buildAspspSettings(), INSTANCE_ID);

        //Then:
        BankProfileSetting setting = profileConfigurations.getSetting(INSTANCE_ID);
        assertEquals(MAX_TRANSACTION_VALIDITY_DAYS, setting.getPis().getMaxTransactionValidityDays());
        assertEquals(PAYMENT_CANCELLATION_AUTHORISATION_MANDATED, setting.getPis().isPaymentCancellationAuthorisationMandated());
        assertEquals(NOT_CONFIRMED_PAYMENT_EXPIRATION_TIME_MS, setting.getPis().getNotConfirmedPaymentExpirationTimeMs());
        assertEquals(SUPPORTED_PAYMENT_TYPE_AND_PRODUCT_MATRIX, setting.getPis().getSupportedPaymentTypeAndProductMatrix());
        assertEquals(PIS_CANCELLATION_REDIRECT_LINK, setting.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp());
        assertEquals(PAYMENT_CANCELLATION_REDIRECT_URL_EXPIRATION_TIME_MS, setting.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs());
        assertEquals(PIS_REDIRECT_LINK, setting.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp());
        assertEquals(COUNTRY_VALIDATION_SUPPORTED, setting.getPis().getCountryValidationSupported());
        assertEquals(SUPPORTED_TRANSACTION_STATUS_FORMATS, setting.getPis().getSupportedTransactionStatusFormats());
    }

    @Test
    void updateAspspSettings_success_piis() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(buildAspspSettings(), INSTANCE_ID);

        //Then:
        BankProfileSetting setting = profileConfigurations.getSetting(INSTANCE_ID);
        assertEquals(PIIS_CONSENT_SUPPORTED, setting.getPiis().getPiisConsentSupported());
    }

    @Test
    void updateAspspSettings_success_common() {
        //When:
        aspspProfileUpdateService.updateAspspSettings(buildAspspSettings(), INSTANCE_ID);

        //Then:
        BankProfileSetting setting = profileConfigurations.getSetting(INSTANCE_ID);
        assertEquals(PSU_IN_INITIAL_REQUEST_MANDATED, setting.getCommon().isPsuInInitialRequestMandated());
        assertEquals(FORCE_XS2A_BASE_LINKS_URL, setting.getCommon().isForceXs2aBaseLinksUrl());
        assertEquals(AUTHORISATION_EXPIRATION_TIME_MS, setting.getCommon().getAuthorisationExpirationTimeMs());
        assertEquals(REDIRECT_URL_EXPIRATION_TIME_MS, setting.getCommon().getRedirectUrlExpirationTimeMs());
        assertEquals(SIGNING_BASKET_SUPPORTED, setting.getSb().isSigningBasketSupported());
        assertEquals(START_AUTHORISATION_MODE.getValue(), setting.getCommon().getStartAuthorisationMode());
        assertEquals(SUPPORTED_ACCOUNT_REFERENCE_FIELDS, setting.getCommon().getSupportedAccountReferenceFields());
        assertEquals(MULTICURRENCY_ACCOUNT_LEVEL_SUPPORTED, setting.getCommon().getMulticurrencyAccountLevelSupported());
        assertEquals(TPP_SIGNATURE_REQUIRED, setting.getCommon().isTppSignatureRequired());
        assertEquals(AIS_PIS_SESSION_SUPPORTED, setting.getCommon().isAisPisSessionsSupported());
        assertEquals(XS2A_BASE_LINKS_URL, setting.getCommon().getXs2aBaseLinksUrl());
        assertEquals(SCA_REDIRECT_FLOW, setting.getCommon().getScaRedirectFlow());
    }

    private AspspSettings buildAspspSettings() {
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
                                                                SUPPORTED_TRANSACTION_STATUS_FORMATS,
                                                                false);
        PiisAspspProfileSetting piis = new PiisAspspProfileSetting(PIIS_CONSENT_SUPPORTED, new PiisRedirectLinkSetting(PIIS_REDIRECT_LINK));
        SbAspspProfileSetting sb = new SbAspspProfileSetting(SIGNING_BASKET_SUPPORTED,
                                                             SIGNING_BASKET_MAX_ENTRIES,
                                                             NOT_CONFIRMED_SIGNING_BASKET_EXPIRATION_TIME_MS,
                                                             SB_REDIRECT_LINK);
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
                                                                         IS_CHECK_TPP_ROLES_FROM_CERTIFICATE,
                                                                         ASPSP_NOTIFICATIONS_SUPPORTED,
                                                                         AUTHORISATION_CONFIRMATION_REQUEST_MANDATED,
                                                                         AUTHORISATION_CONFIRMATION_CHECK_BY_XS2A,
                                                                         CHECK_URI_COMPLIANCE_TO_DOMAIN_SUPPORTED,
                                                                         TPP_URI_COMPLIANCE_RESPONSE,
                                                                         false,
                                                                         false);

        return new AspspSettings(ais, pis, piis, sb, common);
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
        PisAspspProfileBankSetting pis = new PisAspspProfileBankSetting(null, 0, 0, false, pisRedirectLinkToOnlineBanking, COUNTRY_VALIDATION_SUPPORTED, new ArrayList<>(), false);
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting();
        SbAspspProfileBankSetting sb = new SbAspspProfileBankSetting();
        CommonAspspProfileBankSetting common = new CommonAspspProfileBankSetting();
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
