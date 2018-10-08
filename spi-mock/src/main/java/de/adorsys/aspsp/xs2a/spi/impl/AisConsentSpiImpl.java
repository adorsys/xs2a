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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO implement all the methods https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/364
@Component
public class AisConsentSpiImpl implements AisConsentSpi {

    @Override
    public SpiResponse<Void> initiateAisConsent(SpiAccountConsent accountConsent) {
        return null;
    }

    @Override
    public SpiResponse<Void> revokeAisConsent(SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
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
