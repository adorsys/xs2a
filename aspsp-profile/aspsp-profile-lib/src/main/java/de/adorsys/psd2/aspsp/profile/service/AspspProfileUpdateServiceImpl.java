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

import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.aspsp.profile.domain.BookingStatus.BOOKED;
import static de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField.IBAN;

@Service
@RequiredArgsConstructor
public class AspspProfileUpdateServiceImpl implements AspspProfileUpdateService {
    private final ProfileConfiguration profileConfiguration;

    /**
     * Update frequency per day
     *
     * @param frequencyPerDay the new value of frequencyPerDay
     */
    @Override
    public void updateFrequencyPerDay(int frequencyPerDay) {
        profileConfiguration.setFrequencyPerDay(frequencyPerDay);
    }

    /**
     * Update combined service indicator
     *
     * @param combinedServiceIndicator the new value of combinedServiceIndicator
     */
    @Override
    public void updateCombinedServiceIndicator(boolean combinedServiceIndicator) {
        profileConfiguration.setCombinedServiceIndicator(combinedServiceIndicator);
    }

    /**
     * Update BankOfferedConsentSupport status
     *
     * @param bankOfferedConsentSupport BankOfferedConsentSupport status to substitute existing one
     */
    @Override
    public void updateBankOfferedConsentSupport(boolean bankOfferedConsentSupport) {
        profileConfiguration.setBankOfferedConsentSupport(bankOfferedConsentSupport);
    }

    /**
     * Update available payment types
     *
     * @param availablePaymentProducts List of payment product values
     */
    @Override
    public void updateAvailablePaymentProducts(List<String> availablePaymentProducts) {
        profileConfiguration.setAvailablePaymentProducts(availablePaymentProducts);
    }

    /**
     * Update available payment availablePaymentTypes
     *
     * @param availablePaymentTypes List of payment type values
     */
    @Override
    public void updateAvailablePaymentTypes(List<String> availablePaymentTypes) {
        profileConfiguration.setAvailablePaymentTypes(availablePaymentTypes);
    }

    /**
     * Update sca approach
     *
     * @param scaApproach the new value of scaApproach
     */
    @Override
    public void updateScaApproach(ScaApproach scaApproach) {
        profileConfiguration.setScaApproach(scaApproach);
    }

    /**
     * Update if tpp signature is required or not
     *
     * @param tppSignatureRequired the new value of tppSignatureRequired
     */
    @Override
    public void updateTppSignatureRequired(boolean tppSignatureRequired) {
        profileConfiguration.setTppSignatureRequired(tppSignatureRequired);
    }

    /**
     * Update Pis redirect url to aspsp
     *
     * @param redirectUrlToAspsp the new value of Pis redirectUrlToAspsp
     */
    @Override
    public void updatePisRedirectUrlToAspsp(String redirectUrlToAspsp) {
        profileConfiguration.setPisRedirectUrlToAspsp(redirectUrlToAspsp);
    }

    /**
     * Update Ais redirect url to aspsp
     *
     * @param redirectUrlToAspsp the new value of Ais redirectUrlToAspsp
     */
    @Override
    public void updateAisRedirectUrlToAspsp(String redirectUrlToAspsp) {
        profileConfiguration.setAisRedirectUrlToAspsp(redirectUrlToAspsp);
    }

    /**
     * Update value of supported multicurrency account levels
     *
     * @param multicurrencyAccountLevel new value of supported multicurrency account levels
     */
    @Override
    public void updateMulticurrencyAccountLevel(MulticurrencyAccountLevel multicurrencyAccountLevel) {
        profileConfiguration.setMulticurrencyAccountLevel(multicurrencyAccountLevel);
    }

    /**
     * Update list of available booking statuses
     *
     * @param availableBookingStatuses new value of available booking statuses
     */
    @Override
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
    @Override
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
    @Override
    public void updateConsentLifetime(int consentLifetime) {
        profileConfiguration.setConsentLifetime(consentLifetime);
    }

    /**
     * Update the value of a maximum lifetime of transaction set in days
     *
     * @param transactionLifetime the value of a maximum lifetime of transaction to substitute existing one
     */
    @Override
    public void updateTransactionLifetime(int transactionLifetime) {
        profileConfiguration.setTransactionLifetime(transactionLifetime);
    }

    /**
     * Update AllPsd2Support status
     *
     * @param allPsd2Support AllPsd2Support status to substitute existing one
     */
    @Override
    public void updateAllPsd2Support(boolean allPsd2Support) {
        profileConfiguration.setAllPsd2Support(allPsd2Support);
    }

    /**
     * Update type of authorization start to implicit or explicit
     *
     * @param authorisationStartType AllPsd2Support status to substitute existing one
     */
    @Override
    public void updateAuthorisationStartType(AuthorisationStartType authorisationStartType) {
        profileConfiguration.setAuthorisationStartType(authorisationStartType);
    }

    /**
     * Update the value of transactions without balances supported
     *
     * @param transactionsWithoutBalancesSupported the value of transactions without balances supported
     */
    @Override
    public void updateTransactionsWithoutBalancesSupported(boolean transactionsWithoutBalancesSupported) {
        profileConfiguration.setTransactionsWithoutBalancesSupported(transactionsWithoutBalancesSupported);
    }

    /**
     * Update the value of signing basket support
     *
     * @param signingBasketSupported the value of signing basket support
     */
    @Override
    public void updateSigningBasketSupported(boolean signingBasketSupported) {
        profileConfiguration.setSigningBasketSupported(signingBasketSupported);
    }

    /**
     * Update the value of payment cancellation authorization mandated
     *
     * @param paymentCancellationAuthorizationMandated the value of payment cancellation authorization mandated
     */
    @Override
    public void updatePaymentCancellationAuthorizationMandated(boolean paymentCancellationAuthorizationMandated) {
        profileConfiguration.setPaymentCancellationAuthorizationMandated(paymentCancellationAuthorizationMandated);
    }
}
