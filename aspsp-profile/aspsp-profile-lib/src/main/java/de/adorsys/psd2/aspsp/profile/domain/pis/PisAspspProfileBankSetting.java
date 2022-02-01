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
