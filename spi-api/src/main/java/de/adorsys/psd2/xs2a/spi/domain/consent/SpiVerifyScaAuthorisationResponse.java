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

package de.adorsys.psd2.xs2a.spi.domain.consent;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class SpiVerifyScaAuthorisationResponse {
    @NotNull
    private ConsentStatus consentStatus;
    private SpiAuthorisationStatus spiAuthorisationStatus;

    public SpiVerifyScaAuthorisationResponse(@NotNull ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
        spiAuthorisationStatus = null;
    }

    public SpiVerifyScaAuthorisationResponse(@NotNull ConsentStatus consentStatus, SpiAuthorisationStatus spiAuthorisationStatus) {
        this.consentStatus = consentStatus;
        this.spiAuthorisationStatus = spiAuthorisationStatus;
    }
}
