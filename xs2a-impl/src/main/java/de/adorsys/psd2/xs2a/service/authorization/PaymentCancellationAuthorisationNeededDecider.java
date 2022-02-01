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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCancellationAuthorisationNeededDecider {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    /**
     * Decides whether authorisation start is needed according to bank profile setting and spi response boolean field
     * No authorisation start takes place only when both 'paymentCancellationAuthorisationMandated' and 'startAuthorisationRequired' are false
     *
     * @param authorisationByAspspRequired does ASPSP requires authorisation start
     * @return is no SCA is needed
     */
    public boolean isNoScaRequired(boolean authorisationByAspspRequired) {
        return !isScaRequired(authorisationByAspspRequired);
    }

    /**
     * Decides whether authorisation start is needed according to bank profile setting and spi response boolean field
     * Authorisation start occurs when at least one of 'paymentCancellationAuthorisationMandated' and 'startAuthorisationRequired' fields is true
     *
     * @param authorisationByAspspRequired does ASPSP requires authorisation start
     * @return is no SCA is needed
     */
    public boolean isScaRequired(boolean authorisationByAspspRequired) {
        return authorisationByAspspRequired
                   || aspspProfileServiceWrapper.isPaymentCancellationAuthorisationMandated();
    }
}
