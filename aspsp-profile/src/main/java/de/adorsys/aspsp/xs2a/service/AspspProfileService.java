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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * @param availablePaymentTypes List of payment type values
     */
    public void updateAvailablePaymentTypes(List<String> availablePaymentTypes) {
        profileConfiguration.setAvailablePaymentTypes(availablePaymentTypes);
    }

    /**
     * Read sca approach method
     */
    public String getScaApproach() {
        return profileConfiguration.getScaApproach();
    }

    /**
     * Update sca approach
     * @param scaApproach the new value of scaApproach
     */
    public void updateScaApproach(String scaApproach) {
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
     * @param tppSignatureRequired the new value of tppSignatureRequired
     */
    public void updateTppSignatureRequired(boolean tppSignatureRequired) {
        profileConfiguration.setTppSignatureRequired(tppSignatureRequired);
    }
}
