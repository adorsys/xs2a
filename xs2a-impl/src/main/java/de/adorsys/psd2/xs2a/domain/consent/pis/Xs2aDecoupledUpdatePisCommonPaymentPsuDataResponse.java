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

package de.adorsys.psd2.xs2a.domain.consent.pis;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;

/**
 * Xs2aUpdatePisCommonPaymentPsuDataResponse extension to be used ONLY when switching from Embedded to Decoupled approach during SCA method selection
 */

public class Xs2aDecoupledUpdatePisCommonPaymentPsuDataResponse extends Xs2aUpdatePisCommonPaymentPsuDataResponse {

    public Xs2aDecoupledUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, String paymentId, String authorisationId, PsuIdData psuData) {
        super(scaStatus, paymentId, authorisationId, psuData);
    }

    /**
     * Returns <code>null</code> instead of chosenScaMethod when switching from Embedded to Decoupled approach during
     * SCA method selection as this value should not be provided in the response body according to the specification
     *
     * @return <code>null</code>
     */
    @Override
    public Xs2aAuthenticationObject getChosenScaMethodForPsd2Response() {
        return null;
    }
}
