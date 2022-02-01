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

package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class SpiContextDataProvider {
    private final TppService tppService;
    private final RequestProviderService requestProviderService;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;

    public SpiContextData provide() {
        return provideWithPsuIdData(requestProviderService.getPsuIdData());
    }

    public SpiContextData provideWithPsuIdData(PsuIdData psuIdData) {
        TppInfo tppInfo = tppService.getTppInfo();
        return provide(psuIdData, tppInfo);
    }

    public SpiContextData provide(PsuIdData psuIdData, TppInfo tppInfo) {
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuIdData);
        return new SpiContextData(spiPsuData, tppInfo, requestProviderService.getRequestId(),
                                  requestProviderService.getInternalRequestId(), requestProviderService.getOAuth2Token(),
                                  requestProviderService.getTppBrandLoggingInformationHeader(),
                                  requestProviderService.getTppRejectionNoFundsPreferred(),
                                  requestProviderService.resolveTppRedirectPreferred().orElse(null),
                                  requestProviderService.resolveTppDecoupledPreferred().orElse(null));
    }
}
