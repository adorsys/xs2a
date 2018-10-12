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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

// TODO implement all the methods https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
@Component
@Slf4j
public class AisConsentSpiImpl implements AisConsentSpi {

    // Test data is used there for testing purposes to have the possibility to see if AisConsentSpiImpl is being invoked from xs2a.
    // TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String TEST_MESSAGE = "Test message";

    @Override
    public SpiResponse<VoidResponse> initiateAisConsent(@NotNull SpiPsuData spiPsuData, SpiAccountConsent accountConsent, AspspConsentData initialAspspConsentData) {
        log.info("AisConsentSpi initiateAisConsent() mock implementation");
        return SpiResponse.<VoidResponse>builder()
                   .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))     // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                   .message(Collections.singletonList(TEST_MESSAGE))                                      // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                   .success();
    }

    @Override
    public SpiResponse<VoidResponse> revokeAisConsent(@NotNull SpiPsuData spiPsuData, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        log.info("AisConsentSpi revokeAisConsent() mock implementation");
        return SpiResponse.<VoidResponse>builder()
                   .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                   .message(Collections.singletonList(TEST_MESSAGE))                                      // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                   .success();
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiPsuData psuData, String password,
                                                            SpiAccountConsent businessObject,
                                                            AspspConsentData aspspConsentData
                                                           ) {
        return SpiResponse.<SpiAuthorisationStatus>builder()
                   .fail(SpiResponseStatus.NOT_SUPPORTED);

    }

    @Override
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(@NotNull SpiPsuData psuData,
                                                                      SpiAccountConsent businessObject,
                                                                      AspspConsentData aspspConsentData
                                                                     ) {
        return SpiResponse.<List<SpiScaMethod>>builder()
                   .fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    public @NotNull SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiPsuData psuData,
                                                                                     @NotNull SpiScaMethod scaMethod,
                                                                                     @NotNull SpiAccountConsent businessObject,
                                                                                     @NotNull AspspConsentData aspspConsentData
                                                                                    ) {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .fail(SpiResponseStatus.NOT_SUPPORTED);
    }

    @Override
    public @NotNull SpiResponse<VoidResponse> verifyAuthorisationCodeAndExecuteRequest(@NotNull SpiPsuData psuData,
                                                                         @NotNull SpiScaConfirmation spiScaConfirmation,
                                                                         @NotNull SpiAccountConsent businessObject,
                                                                         @NotNull AspspConsentData aspspConsentData
                                                                        ) {
        return SpiResponse.<VoidResponse>builder()
                   .fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
