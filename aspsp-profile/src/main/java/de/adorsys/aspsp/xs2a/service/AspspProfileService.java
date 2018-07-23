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
import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.BookingStatus.BOOKED;

@Service
@RequiredArgsConstructor
public class AspspProfileService {
    private final ProfileConfiguration profileConfiguration;

    /**
     * Read frequency per day
     */
    public int getFrequencyPerDay() {
        return profileConfiguration.getFrequencyPerDay();
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
     * Read combined service indicator
     */
    public boolean isCombinedServiceIndicator() {
        return profileConfiguration.isCombinedServiceIndicator();
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
     * Read List of available payment products
     */
    public List<String> getAvailablePaymentProducts() {
        return profileConfiguration.getAvailablePaymentProducts();
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
     * Read List of available payment types
     */
    public List<String> getAvailablePaymentTypes() {
        return profileConfiguration.getAvailablePaymentTypes();
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
     * Read sca approach method
     *
     * @return sca approach method which is stored in profile
     */
    public ScaApproach getScaApproach() {
        return profileConfiguration.getScaApproach();
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
     * Read if tpp signature is required or not
     */
    public boolean isTppSignatureRequired() {
        return profileConfiguration.isTppSignatureRequired();
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
     * Read Pis redirect url to Aspsp
     */
    public String getPisRedirectUrlToAspsp() {
        return profileConfiguration.getPisRedirectUrlToAspsp();
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
     * Read Ais redirect url to Aspsp
     */
    public String getAisRedirectUrlToAspsp() {
        return profileConfiguration.getAisRedirectUrlToAspsp();
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
     * Read supported multicurrency account levels
     */
    public MulticurrencyAccountLevel getMulticurrencyAccountLevel() {
        return profileConfiguration.getMulticurrencyAccountLevel();
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
     * Read list of available booking statuses
     */
    public List<BookingStatus> getAvailableBookingStatuses() {
        return profileConfiguration.getAvailableBookingStatuses();
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
}
