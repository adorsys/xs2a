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
