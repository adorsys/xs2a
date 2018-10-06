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

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse.VoidResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.v2.AisConsentSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

// TODO implement all the methods https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
@Component
@Slf4j
public class AisConsentSpiImpl implements AisConsentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String TEST_MESSAGE = "Test message";

    @Override
    public SpiResponse<VoidResponse> initiateAisConsent(SpiAccountConsent accountConsent, AspspConsentData initialAspspConsentData) {
        log.info("AisConsentSpi initiateAisConsent() mock implementation");
        return SpiResponse.<VoidResponse>builder()
                   .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))     // added to test purposes
                   .message(Collections.singletonList(TEST_MESSAGE))                                      // added to test purposes
                   .success();
    }

    @Override
    public SpiResponse<VoidResponse> revokeAisConsent(SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        return null;
    }

    @Override
    public SpiResponse verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        return null;
    }
}
