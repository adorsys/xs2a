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

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("PMD.ExcessivePublicCount")
@RequiredArgsConstructor
public class AspspProfileServiceWrapper {
    private final AspspProfileService aspspProfileService;
    private final RequestProviderService requestProviderService;

    /**
     * Gets a map with payment types and products allowed by current ASPSP from ASPSP profile service
     *
     * @return Map with payment types and  products supported by current ASPSP
     */
    public Map<PaymentType, Set<String>> getSupportedPaymentTypeAndProductMatrix() {
        return readAspspSettings().getPis().getSupportedPaymentTypeAndProductMatrix();
    }

    /**
     * Reads list of sca approaches from ASPSP profile service
     *
     * @return List of Available SCA approaches for tpp
     */
    public List<ScaApproach> getScaApproaches() {
        return aspspProfileService.getScaApproaches(requestProviderService.getInstanceId());
    }

    /**
     * Reads requirement of tpp signature from ASPSP profile service
     *
     * @return 'true' if tpp signature is required, 'false' if not
     */
    public boolean isTppSignatureRequired() {
        return readAspspSettings().getCommon().isTppSignatureRequired();
    }

    /**
     * Reads get PIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPisRedirectUrlToAspsp() {
        return readAspspSettings().getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp();
    }

    /**
     * Reads get AIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getAisRedirectUrlToAspsp() {
        return readAspspSettings().getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp();
    }

    /**
     * Reads get PIIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPiisRedirectUrlToAspsp() {
        return readAspspSettings().getPiis().getRedirectLinkToOnlineBanking().getPiisRedirectUrlToAspsp();
    }

    /**
     * Retrieves list of supported Xs2aAccountReference fields from ASPSP profile service
     *
     * @return List of supported fields
     */
    public List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        List<de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField> supportedAccountReferenceFields = readAspspSettings().getCommon().getSupportedAccountReferenceFields();
        return supportedAccountReferenceFields.stream()
                   .map(reference -> SupportedAccountReferenceField.valueOf(reference.name()))
                   .collect(Collectors.toList());
    }

    /**
     * Reads maximum lifetime of consent set in days
     *
     * @return int maximum lifetime of consent set in days
     */
    public int getMaxConsentValidityDays() {
        return readAspspSettings().getAis().getConsentTypes().getMaxConsentValidityDays();
    }

    /**
     * Reads value of globalConsentSupported from ASPSP profile service
     *
     * @return true if ASPSP supports Global consents, false if doesn't
     */
    public Boolean isGlobalConsentSupported() {
        return readAspspSettings().getAis().getConsentTypes().isGlobalConsentSupported();
    }

    /**
     * Reads value BankOfferedConsentSupported
     *
     * @return boolean representation of support of Bank Offered Consent
     */
    public boolean isBankOfferedConsentSupported() {
        return readAspspSettings().getAis().getConsentTypes().isBankOfferedConsentSupported();
    }

    /**
     * Reads value of transactions without balances supported from ASPSP profile service
     *
     * @return true if ASPSP transactions without balances supported, false if doesn't
     */
    public boolean isTransactionsWithoutBalancesSupported() {
        return readAspspSettings().getAis().getTransactionParameters().isTransactionsWithoutBalancesSupported();
    }

    /**
     * Reads if signing basket supported from ASPSP profile service
     *
     * @return true if ASPSP supports signing basket , false if doesn't
     */
    public boolean isSigningBasketSupported() {
        return readAspspSettings().getSb().isSigningBasketSupported();
    }

    /**
     * Reads if is payment cancellation authorisation mandated from ASPSP profile service
     *
     * @return true if payment cancellation authorisation is mandated, false if doesn't
     */
    public boolean isPaymentCancellationAuthorisationMandated() {
        return readAspspSettings().getPis().isPaymentCancellationAuthorisationMandated();
    }

    /**
     * Reads piis consent supported parameter
     *
     * @return piis consent supported parameter
     */
    public PiisConsentSupported getPiisConsentSupported() {
        return readAspspSettings().getPiis().getPiisConsentSupported();
    }

    /**
     * Reads redirect url expiration time in milliseconds
     *
     * @return long value of redirect url expiration time
     */
    public long getRedirectUrlExpirationTimeMs() {
        return readAspspSettings().getCommon().getRedirectUrlExpirationTimeMs();
    }

    /**
     * Reads authorisation expiration time in milliseconds
     *
     * @return long value of authorisation expiration time
     */
    public long getAuthorisationExpirationTimeMs() {
        return readAspspSettings().getCommon().getAuthorisationExpirationTimeMs();
    }

    /**
     * Reads get PIS payment cancellation redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPisPaymentCancellationRedirectUrlToAspsp() {
        return readAspspSettings().getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp();
    }

    /**
     * Reads if available accounts for a consent are supported from ASPSP profile service
     *
     * @return true if ASPSP supports available accounts for consent
     */
    public boolean isAvailableAccountsConsentSupported() {
        return readAspspSettings().getAis().getConsentTypes().isAvailableAccountsConsentSupported();
    }

    /**
     * Reads if ASPSP requires usage of SCA to validate a one-time available accounts consent
     *
     * @return true if ASPSP requires usage of SCA to validate a one-time available accounts consent
     */
    public boolean isScaByOneTimeAvailableAccountsConsentRequired() {
        return readAspspSettings().getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired();
    }

    /**
     * Reads if ASPSP requires usage of SCA to validate a one-time global consent
     *
     * @return true if ASPSP requires usage of SCA to validate a one-time global consent
     */
    public boolean isScaByOneTimeGlobalConsentRequired() {
        return readAspspSettings().getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeGlobalConsentRequired();
    }

    /**
     * Reads if ASPSP requires PSU in initial request for payment initiation or establishing consent
     *
     * @return true if ASPSP requires PSU in initial request for payment initiation or establishing consent
     */
    public boolean isPsuInInitialRequestMandated() {
        return readAspspSettings().getCommon().isPsuInInitialRequestMandated();
    }

    /**
     * Reads if links shall be generated with the base URL set by `xs2aBaseLinksUrl`
     *
     * @return true if ASPSP requires that links shall be generated with the base URL set by `xs2aBaseLinksUrl` property
     */
    public boolean isForceXs2aBaseLinksUrl() {
        return readAspspSettings().getCommon().isForceXs2aBaseLinksUrl();
    }

    /**
     * Reads the url, which is used as base url for TPP Links in case when `forceXs2aBaseLinksUrl` property is set to "true"
     *
     * @return String value of the url
     */
    public String getXs2aBaseLinksUrl() {
        return readAspspSettings().getCommon().getXs2aBaseLinksUrl();
    }

    /**
     * Reads whether a payment initiation service will be addressed in the same "session" or not
     *
     * @return true if a payment initiation service will be addressed in the same session
     */
    public boolean isAisPisSessionsSupported() {
        return readAspspSettings().getCommon().isAisPisSessionsSupported();
    }

    /**
     * Reads if 'deltaListSupported' parameter in transaction report is supported from ASPSP profile service
     *
     * @return true if ASPSP supports 'deltaListSupported' parameter in transaction report
     */
    public boolean isDeltaListSupported() {
        return readAspspSettings().getAis().getDeltaReportSettings().isDeltaListSupported();
    }

    /**
     * Reads if 'entryReferenceFromSupported' parameter in transaction report is supported from ASPSP profile service
     *
     * @return true if ASPSP supports 'entryReferenceFromSupported' parameter in transaction report
     */
    public boolean isEntryReferenceFromSupported() {
        return readAspspSettings().getAis().getDeltaReportSettings().isEntryReferenceFromSupported();
    }

    /**
     * Retrieves a list of available booking statuses from the ASPSP profile service
     *
     * @return list of available booking statuses
     */
    public List<BookingStatus> getAvailableBookingStatuses() {
        return readAspspSettings().getAis().getTransactionParameters().getAvailableBookingStatuses();
    }

    /**
     * Reads the mode of authorisation from the ASPSP profile service.
     *
     * @return String with the selected mode.
     */
    public StartAuthorisationMode getStartAuthorisationMode() {
        return readAspspSettings().getCommon().getStartAuthorisationMode();
    }

    /**
     * Reads the variant of redirect approach to be used.
     *
     * @return the variant of redirect approach to be used.
     */
    public ScaRedirectFlow getScaRedirectFlow() {
        return readAspspSettings().getCommon().getScaRedirectFlow();
    }

    /**
     * Reads the link to IDP.
     *
     * @return the URL.
     */
    public String getOauthConfigurationUrl() {
        return readAspspSettings().getCommon().getOauthConfigurationUrl();
    }

    /**
     * Reads the maximum allowed by bank accesses for consent's usage per unique resource for each endpoint.
     *
     * @return int limit of each endpoint usages.
     */
    public int getAccountAccessFrequencyPerDay() {
        return readAspspSettings().getAis().getConsentTypes().getAccountAccessFrequencyPerDay();
    }

    /**
     * Reads transaction application type supported by ASPSP (application/json, application/xml etc).
     *
     * @return List of transaction application type supported by ASPSP.
     */
    public List<String> getSupportedTransactionApplicationTypes() {
        return readAspspSettings().getAis().getTransactionParameters().getSupportedTransactionApplicationTypes();
    }

    /**
     * Reads if 'accountOwnerInformationSupported' parameter is supported from ASPSP profile service
     *
     * @return true if ASPSP supports 'entryReferenceFromSupported' parameter in transaction report
     */
    public boolean isAccountOwnerInformationSupported() {
        return readAspspSettings().getAis().getConsentTypes().isAccountOwnerInformationSupported();
    }

    /**
     * Reads if 'trustedBenefeciariesSupported' parameter is supported from ASPSP profile service
     *
     * @return true if ASPSP supports 'trustedBenefeciariesSupported', else otherwise
     */
    public boolean isTrustedBeneficiariesSupported() {
        return readAspspSettings().getAis().getConsentTypes().isTrustedBeneficiariesSupported();
    }

    private AspspSettings readAspspSettings() {
        return aspspProfileService.getAspspSettings(requestProviderService.getInstanceId());
    }

    /**
     * For which country payment is supported
     * @return country in ISO 3166-1 alpha-2 code (DE, AT, etc)
     */
    public String getSupportedPaymentCountryValidation(){
        return readAspspSettings().getPis().getCountryValidationSupported();
    }

    /**
     * Reads Multicurrency account level
     *
     * @return MulticurrencyAccountLevel object
     */
    public MulticurrencyAccountLevel getMulticurrencyAccountLevel() {
     return readAspspSettings().getCommon().getMulticurrencyAccountLevelSupported();
    }

    /**
     * Reads transaction status application types supported by ASPSP (application/json, application/xml etc).
     *
     * @return list of transaction status application types
     */
    public List<String> getSupportedTransactionStatusFormats() {
        return readAspspSettings().getPis().getSupportedTransactionStatusFormats();
    }

    /**
     * Reads if ASPSP supports validation TPP roles from certificate
     *
     * @return true if ASPSP supports validation TPP roles from certificate
     */
    public boolean isCheckTppRolesFromCertificateSupported() {
        return readAspspSettings().getCommon().isCheckTppRolesFromCertificateSupported();
    }

    /**
     * Reads the supported modes of notification status services. Default and only value is 'NONE'.
     *
     * @return list of supported modes
     */
    public List<NotificationSupportedMode> getNotificationSupportedModes() {
        return readAspspSettings().getCommon().getAspspNotificationsSupported();
    }

    /**
     * Shows if the confirmation of authorisation supported.
     *
     * @return true if ASPSP supports confirmation of authorisation, false otherwise.
     */
    public boolean isAuthorisationConfirmationRequestMandated() {
        return readAspspSettings().getCommon().isAuthorisationConfirmationRequestMandated();
    }

    /**
     * Shows if the authentication data should be checked by XS2A.
     *
     * @return true if XS2A must check the authentication data, false if it is ASPSP duty.
     */
    public boolean isAuthorisationConfirmationCheckByXs2a() {
        return readAspspSettings().getCommon().isAuthorisationConfirmationCheckByXs2a();
    }

    /**
     * Indicates whether ASPSP supports validation URIs with domain from certificate.
     *
     * @return true if ASPSP supports validation URIs with domain from certificate, false otherwise.
     */
    public boolean isCheckUriComplianceToDomainSupported() {
        return readAspspSettings().getCommon().isCheckUriComplianceToDomainSupported();
    }

    /**
     * Indicates which response TPP will get in case of invalid TPP URIs
     *
     * @return WARNING if in case of invalid URIs response should contain correct response and warning,
     * REJECT if in case of invalid URIs request should be rejected with error
     */
    public TppUriCompliance getTppUriComplianceResponse() {
        return readAspspSettings().getCommon().getTppUriComplianceResponse();
    }

    /**
     * Indicates if PSU-ID is ignored by XS2A, in case it's provided in Initial request for Payment request or Establishing Consent.
     *
     * @return true if PSU-ID is ignored by XS2A, in case it's provided in Initial request for Payment request or Establishing Consent, false otherwise.
     */
    public boolean isPsuInInitialRequestIgnored() {
        return readAspspSettings().getCommon().isPsuInInitialRequestIgnored();
    }

    /**
     * Indicates if iban validation is disabled
     *
     * @return true if iban validation is disabled, false otherwise.
     */
    public boolean isIbanValidationDisabled() {
        return readAspspSettings().getCommon().isIbanValidationDisabled();
    }

    /**
     * Indicates if debtorAccount is optional in initial request
     *
     * @return true if debtorAccount is optional in initial request, false otherwise.
     */
    public boolean isDebtorAccountOptionalInInitialRequest() {
        return readAspspSettings().getPis().isDebtorAccountOptionalInInitialRequest();
    }
}
