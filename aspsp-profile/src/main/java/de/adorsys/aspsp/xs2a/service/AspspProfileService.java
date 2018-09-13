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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.config.ProfileConfiguration;
import de.adorsys.aspsp.xs2a.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOOKED;
import static de.adorsys.aspsp.xs2a.domain.SupportedAccountReferenceField.IBAN;

@Service
@RequiredArgsConstructor
public class AspspProfileService {
    private final ProfileConfiguration profileConfiguration;

    /**
     * Reads all aspsp settings (frequency per day, combined service indicator, available payment products, available payment types,
     * is tpp signature required, PIS redirect URL, AIS redirect URL, multicurrency account level, is bank offered consent supported,
     * available booking statuses, supported account reference fields, consent lifetime, transaction lifetime, allPsd2 support and
     * type of authorization start) except SCA approach
     *
     * @return aspsp specific settings method which is stored in profile
     */
    public AspspSettings getAspspSettings() {
        return new AspspSettings(
            profileConfiguration.getFrequencyPerDay(),
            profileConfiguration.isCombinedServiceIndicator(),
            profileConfiguration.getAvailablePaymentProducts(),
            profileConfiguration.getAvailablePaymentTypes(),
            profileConfiguration.isTppSignatureRequired(),
            profileConfiguration.getPisRedirectUrlToAspsp(),
            profileConfiguration.getAisRedirectUrlToAspsp(),
            profileConfiguration.getMulticurrencyAccountLevel(),
            profileConfiguration.isBankOfferedConsentSupport(),
            profileConfiguration.getAvailableBookingStatuses(),
            profileConfiguration.getSupportedAccountReferenceFields(),
            profileConfiguration.getConsentLifetime(),
            profileConfiguration.getTransactionLifetime(),
            profileConfiguration.isAllPsd2Support(),
            profileConfiguration.getAuthorisationStartType(),
            profileConfiguration.isTransactionsWithoutBalancesSupported());
    }

    /**
     * Reads sca approach method
     *
     * @return sca approach method which is stored in profile
     */
    public ScaApproach getScaApproach() {
        return profileConfiguration.getScaApproach();
    }

    /**
     * Update frequency per day
     *
     * @param frequencyPerDay the new value of frequencyPerDay
     */
    public void updateFrequencyPerDay(int frequencyPerDay) {
        profileConfiguration.setFrequencyPerDay(frequencyPerDay);
    }

    /**
     * Update combined service indicator
     *
     * @param combinedServiceIndicator the new value of combinedServiceIndicator
     */
    public void updateCombinedServiceIndicator(boolean combinedServiceIndicator) {
        profileConfiguration.setCombinedServiceIndicator(combinedServiceIndicator);
    }

    /**
     * Update BankOfferedConsentSupport status
     *
     * @param bankOfferedConsentSupport BankOfferedConsentSupport status to substitute existing one
     */
    public void updateBankOfferedConsentSupport(boolean bankOfferedConsentSupport) {
        profileConfiguration.setBankOfferedConsentSupport(bankOfferedConsentSupport);
    }

    /**
     * Update available payment types
     *
     * @param availablePaymentProducts List of payment product values
     */
    public void updateAvailablePaymentProducts(List<String> availablePaymentProducts) {
        profileConfiguration.setAvailablePaymentProducts(availablePaymentProducts);
    }

    /**
     * Update available payment availablePaymentTypes
     *
     * @param availablePaymentTypes List of payment type values
     */
    public void updateAvailablePaymentTypes(List<String> availablePaymentTypes) {
        profileConfiguration.setAvailablePaymentTypes(availablePaymentTypes);
    }

    /**
     * Update sca approach
     *
     * @param scaApproach the new value of scaApproach
     */
    public void updateScaApproach(ScaApproach scaApproach) {
        profileConfiguration.setScaApproach(scaApproach);
    }

    /**
     * Update if tpp signature is required or not
     *
     * @param tppSignatureRequired the new value of tppSignatureRequired
     */
    public void updateTppSignatureRequired(boolean tppSignatureRequired) {
        profileConfiguration.setTppSignatureRequired(tppSignatureRequired);
    }

    /**
     * Update Pis redirect url to aspsp
     *
     * @param redirectUrlToAspsp the new value of Pis redirectUrlToAspsp
     */
    public void updatePisRedirectUrlToAspsp(String redirectUrlToAspsp) {
        profileConfiguration.setPisRedirectUrlToAspsp(redirectUrlToAspsp);
    }

    /**
     * Update Ais redirect url to aspsp
     *
     * @param redirectUrlToAspsp the new value of Ais redirectUrlToAspsp
     */
    public void updateAisRedirectUrlToAspsp(String redirectUrlToAspsp) {
        profileConfiguration.setAisRedirectUrlToAspsp(redirectUrlToAspsp);
    }

    /**
     * Update value of supported multicurrency account levels
     *
     * @param multicurrencyAccountLevel new value of supported multicurrency account levels
     */
    public void updateMulticurrencyAccountLevel(MulticurrencyAccountLevel multicurrencyAccountLevel) {
        profileConfiguration.setMulticurrencyAccountLevel(multicurrencyAccountLevel);
    }

    /**
     * Update list of available booking statuses
     *
     * @param availableBookingStatuses new value of available booking statuses
     */
    public void updateAvailableBookingStatuses(List<BookingStatus> availableBookingStatuses) {
        if (!availableBookingStatuses.contains(BOOKED)) {
            availableBookingStatuses.add(BOOKED);
        }
        profileConfiguration.setAvailableBookingStatuses(availableBookingStatuses);
    }

    /**
     * Update list of ASPSP supported Account Reference fields
     *
     * @param fields list of supported fields to substitute existing one
     */
    public void updateSupportedAccountReferenceFields(List<SupportedAccountReferenceField> fields) {
        if (!fields.contains(IBAN)) {
            fields.add(IBAN);
        }
        profileConfiguration.setSupportedAccountReferenceFields(fields);
    }

    /**
     * Update the value of a maximum lifetime of consent
     *
     * @param consentLifetime the value of a maximum lifetime of consent to substitute existing one
     */
    public void updateConsentLifetime(int consentLifetime) {
        profileConfiguration.setConsentLifetime(consentLifetime);
    }

    /**
     * Update the value of a maximum lifetime of transaction set in days
     *
     * @param transactionLifetime the value of a maximum lifetime of transaction to substitute existing one
     */
    public void updateTransactionLifetime(int transactionLifetime) {
        profileConfiguration.setTransactionLifetime(transactionLifetime);
    }

    /**
     * Update AllPsd2Support status
     *
     * @param allPsd2Support AllPsd2Support status to substitute existing one
     */
    public void updateAllPsd2Support(boolean allPsd2Support) {
        profileConfiguration.setAllPsd2Support(allPsd2Support);
    }

    /**
     * Update type of authorization start to implicit or explicit
     *
     * @param authorisationStartType AllPsd2Support status to substitute existing one
     */
    public void updateAuthorisationStartType(AuthorisationStartType authorisationStartType) {
        profileConfiguration.setAuthorisationStartType(authorisationStartType);
    }

    /**
     * Update the value of transactions without balances supported
     *
     * @param transactionsWithoutBalancesSupported the value of transactions without balances supported
     */
    public void updateTransactionsWithoutBalancesSupported(boolean transactionsWithoutBalancesSupported) {
        profileConfiguration.setTransactionsWithoutBalancesSupported(transactionsWithoutBalancesSupported);
    }
}
