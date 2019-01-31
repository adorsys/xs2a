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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorisationMethodDecider {
    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Decides whether explicit authorisation method will be used based on tppExplicitAuthorisationPreferred and signingBasketSupported values.
     * Explicit authorisation will be used in case if tppExplicitAuthorisationPreferred = true and signingBasketSupported = true or in case of multilevel SCA
     *
     * @param tppExplicitAuthorisationPreferred value of tpp's choice of authorisation method
     * @return is explicit method of authorisation will be used
     */
    public boolean isExplicitMethod(boolean tppExplicitAuthorisationPreferred, boolean multilevelScaRequired) {
        return multilevelScaRequired
                   || tppExplicitAuthorisationPreferred && aspspProfileService.isSigningBasketSupported();
    }

    /**
     * Decides whether implicit authorisation method will be used based on tppExplicitAuthorisationPreferred and signingBasketSupported values.
     * Implicit authorisation will be used in all the cases where tppExplicitAuthorisationPreferred or signingBasketSupported not equals true
     * Implicit approach is impossible in case of multilevel SCA
     *
     * @param tppExplicitAuthorisationPreferred value of tpp's choice of authorisation method
     * @return is implicit method of authorisation will be used
     */
    public boolean isImplicitMethod(boolean tppExplicitAuthorisationPreferred, boolean multilevelScaRequired) {
        return !isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);
    }
}
