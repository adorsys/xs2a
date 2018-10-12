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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Spi interface to be used for AIS consent initiating and revoking, and authorising process through AuthorisationSpi interface.
 */
public interface AisConsentSpi extends AuthorisationSpi<SpiAccountConsent> {

    default SpiResponse<VoidResponse> initiateAisConsent(@NotNull SpiPsuData psuData, SpiAccountConsent accountConsent, AspspConsentData initialAspspConsentData) {
        return SpiResponse.<VoidResponse>builder()
            .fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    default SpiResponse<VoidResponse> revokeAisConsent(@NotNull SpiPsuData psuData, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData){
        return SpiResponse.<VoidResponse>builder()
                   .fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
