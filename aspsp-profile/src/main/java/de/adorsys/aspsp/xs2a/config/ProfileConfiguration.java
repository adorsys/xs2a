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


import de.adorsys.aspsp.xs2a.domain.MulticurrencyAccountLevel;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ProfileConfiguration {
    private final static boolean isDelayedPaymentTypeAllowedAlways = true;

    /**
     * This field indicates the requested maximum frequency for an access per day
     */
    private int frequencyPerDay;

    /**
     * If "true" indicates that a payment initiation service will be addressed in the same "session"
     */
    private boolean combinedServiceIndicator;

    /**
     * List of payment products supported by ASPSP
     */
    private List<String> availablePaymentProducts;

    /**
     * List of payment types supported by ASPSP
     */
    private List<String> availablePaymentTypes;

    /**
     * SCA Approach supported by ASPSP
     */
    private ScaApproach scaApproach;

    /**
     * A signature of the request by the TPP on application level.
     * If the value is `true`, the signature is mandated by ASPSP.
     * If the value is `false`, the signature can be omitted.
     */
    private boolean tppSignatureRequired;

    /**
     * URL to ASPSP service in order to to work with PIS
     */
    private String pisRedirectUrlToAspsp;

    /**
     * URL to ASPSP service in order to work with AIS
     */
    private String aisRedirectUrlToAspsp;

    /**
     * Multicurrency account types supported by ASPSP
     */
    private MulticurrencyAccountLevel multicurrencyAccountLevel;

}
