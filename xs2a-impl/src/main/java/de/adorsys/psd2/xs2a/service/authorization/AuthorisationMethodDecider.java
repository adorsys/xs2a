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

import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationMethodDecider {
    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Decides whether explicit authorisation method will be used based on bank_profile configuration and
     * tppExplicitAuthorisationPreferred, signingBasketSupported values.
     * <p>
     * Explicit authorisation will be used if:
     * <p>
     * - bank_profile configuration field 'startAuthorisationMode' is set to 'explicit';
     * - bank_profile configuration field 'startAuthorisationMode' is set to 'auto' and
     * tppExplicitAuthorisationPreferred = true and signingBasketSupported = true or in case of multilevel SCA
     *
     * @param tppExplicitAuthorisationPreferred value of tpp's choice of authorisation method
     * @param multilevelScaRequired             does response have multilevel SCA
     * @return is explicit method of authorisation will be used
     */
    public boolean isExplicitMethod(boolean tppExplicitAuthorisationPreferred, boolean multilevelScaRequired) {
        boolean explicit = false;

        StartAuthorisationMode startAuthorisationMode = aspspProfileService.getStartAuthorisationMode();
        if (StartAuthorisationMode.AUTO.equals(startAuthorisationMode)) {
            explicit = multilevelScaRequired || isSigningBasketModeActive(tppExplicitAuthorisationPreferred);
        } else {
            explicit = StartAuthorisationMode.EXPLICIT.equals(startAuthorisationMode);
        }

        log.info("{} authorisation method chosen", explicit ? "EXPLICIT" : "IMPLICIT");
        return explicit;
    }

    /**
     * Decides whether implicit authorisation method will be used based on bank_profile configuration and
     * tppExplicitAuthorisationPreferred and signingBasketSupported values.
     * <p>
     * Implicit authorisation will be used if:
     * <p>
     * - bank_profile configuration field 'startAuthorisationMode' is set to 'implicit';
     * - bank_profile configuration field 'startAuthorisationMode' is set to 'auto' and
     * tppExplicitAuthorisationPreferred = false and signingBasketSupported = false.
     * <p>
     * Implicit approach is impossible in case of multilevel SCA
     *
     * @param tppExplicitAuthorisationPreferred value of TPP's choice of authorisation method
     * @param multilevelScaRequired             does response have multilevel SCA
     * @return is implicit method of authorisation will be used
     */
    public boolean isImplicitMethod(boolean tppExplicitAuthorisationPreferred, boolean multilevelScaRequired) {
        return !isExplicitMethod(tppExplicitAuthorisationPreferred, multilevelScaRequired);
    }

    /**
     * Decides whether signing basket mode activate will be used
     * tppExplicitAuthorisationPreferred and signingBasketSupported values.
     * Signing basket mode is not active in case of tppExplicitAuthorisationPreferred is false
     *
     * @param tppExplicitAuthorisationPreferred value of TPP's choice of authorisation method
     * @return is signing basket mode activate for authorisation
     */
    public boolean isSigningBasketModeActive(boolean tppExplicitAuthorisationPreferred) {
        return tppExplicitAuthorisationPreferred && aspspProfileService.isSigningBasketSupported();
    }
}
