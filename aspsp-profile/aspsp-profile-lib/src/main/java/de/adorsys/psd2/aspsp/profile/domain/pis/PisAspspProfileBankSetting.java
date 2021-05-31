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

package de.adorsys.psd2.aspsp.profile.domain.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PisAspspProfileBankSetting {

    /**
     * A matrix payment-product/payment-type which allows to choose needed types
     */
    private Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix;

    /**
     * The limit of a maximum lifetime of payment transaction set in days. It is set to 0 or empty, then the maximum lifetime of transaction is infinity.
     */
    private int maxTransactionValidityDays;

    /**
     * The limit of an expiration time of not confirmed payment url set in milliseconds
     */
    private long notConfirmedPaymentExpirationTimeMs;

    /**
     * Indicates whether, the authorization of the payment cancellation is mandated by the ASPSP
     */
    private boolean paymentCancellationAuthorisationMandated;


    /**
     * A group of settings to define URL links and their lifetime for redirect approach
     */
    private PisRedirectLinkBankSetting redirectLinkToOnlineBanking;

    /**
     * Indicates for which country the payment will be validated
     */
    private String countryValidationSupported;

    private List<String> supportedTransactionStatusFormats;

    /**
     * Indicates whether debtorAccount is required in initial request
     */
    private boolean debtorAccountOptionalInInitialRequest;
}
