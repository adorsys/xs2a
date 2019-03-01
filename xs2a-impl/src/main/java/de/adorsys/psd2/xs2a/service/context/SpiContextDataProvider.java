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

    public SpiContextData provideWithPsuIdData(PsuIdData psuIdData) {
        TppInfo tppInfo = tppService.getTppInfo();
        return provide(psuIdData, tppInfo);
    }

    public SpiContextData provide(PsuIdData psuIdData, TppInfo tppInfo) {
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuIdData);
        return new SpiContextData(spiPsuData, tppInfo, requestProviderService.getRequestId());
    }
}
