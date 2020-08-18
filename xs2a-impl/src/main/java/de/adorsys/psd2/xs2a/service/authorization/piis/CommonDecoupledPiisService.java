/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.authorization.CommonDecoupledConsentService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.springframework.stereotype.Service;

@Service
public class CommonDecoupledPiisService extends CommonDecoupledConsentService<SpiPiisConsent> {
    private final PiisConsentSpi piisConsentSpi;

    public CommonDecoupledPiisService(SpiErrorMapper spiErrorMapper,
                                      SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                      SpiContextDataProvider spiContextDataProvider,
                                      Xs2aAuthorisationService authorisationService,
                                      PiisConsentSpi piisConsentSpi) {
        super(spiErrorMapper, aspspConsentDataProviderFactory, spiContextDataProvider, authorisationService);
        this.piisConsentSpi = piisConsentSpi;
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.PIIS;
    }

    @Override
    protected SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(SpiContextData spiContextData, String authorisationId, String authenticationMethodId, SpiPiisConsent spiConsent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.startScaDecoupled(spiContextData, authorisationId, authenticationMethodId, spiConsent, spiAspspConsentDataProvider);
    }
}
