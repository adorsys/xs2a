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
