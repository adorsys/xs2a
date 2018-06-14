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

package de.adorsys.aspsp.xs2a.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AspspProfileRemoteUrls {

    @Value("${aspsp-profile.baseurl:http://localhost:48080/api/v1}")
    private String aspspProfileBaseUrl;

    /**
     * Returns URL-string in order to get list of available payment products
     *
     * @return String
     */
    public String getAvailablePaymentProducts() {
        return aspspProfileBaseUrl + "/aspsp-profile/available-payment-products";
    }

    /**
     * Returns URL-string in order to get list of available payment types
     *
     * @return String
     */
    public String getAvailablePaymentTypes() {
        return aspspProfileBaseUrl + "/aspsp-profile/available-payment-types";
    }

    /**
     * Returns URL-string in order to get frequency per day
     *
     * @return String
     */
    public String getFrequencyPerDay() {
        return aspspProfileBaseUrl + "/aspsp-profile/frequency-per-day";
    }

    /**
     * Returns URL-string in order to get combined service indicator
     *
     * @return String
     */
    public String getCombinedServiceIndicator() {
        return aspspProfileBaseUrl + "/aspsp-profile/combined-service-indicator";
    }

    /**
     * Returns URL-string in order to get sca approach
     *
     * @return String
     */
    public String getScaApproach() {
        return aspspProfileBaseUrl + "/aspsp-profile/sca-approach";
    }

}
