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

package de.adorsys.psd2.xs2a.integration.builder.ais;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;

import java.util.Collections;
import java.util.Currency;

public class SpiInitiateAisConsentResponseBuilder {
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], "some consent id");
    private static final String CORRECT_IBAN = "DE123456789";

    public static SpiResponse<SpiInitiateAisConsentResponse> buildAisConsent(boolean multiLevelSca) {
        SpiInitiateAisConsentResponse response = new SpiInitiateAisConsentResponse(getSpiAccountAccess(), multiLevelSca, "");
        return buildSpiResponse(response);
    }


    private static SpiAccountAccess getSpiAccountAccess() {
        return new SpiAccountAccess(Collections.singletonList(new SpiAccountReference(null, CORRECT_IBAN, null, null, null, null, Currency.getInstance("EUR"))), null, null, null, null, null);
    }

    private static <T> SpiResponse<T> buildSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }
}
