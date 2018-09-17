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

package de.adorsys.aspsp.xs2a.domain.profile;

import lombok.Value;

import java.util.List;

@Value
public class AspspSettings {
    private int frequencyPerDay;
    private boolean combinedServiceIndicator;
    private List<String> availablePaymentProducts;
    private List<String> availablePaymentTypes;
    private boolean tppSignatureRequired;
    private String pisRedirectUrlToAspsp;
    private String aisRedirectUrlToAspsp;
    private String multicurrencyAccountLevel;
    private boolean bankOfferedConsentSupport;
    private List<String> availableBookingStatuses;
    private List<String> supportedAccountReferenceFields;
    private int consentLifetime;
    private int transactionLifetime;
    private boolean allPsd2Support;
    private String authorisationStartType;
    private boolean transactionsWithoutBalancesSupported;
}
