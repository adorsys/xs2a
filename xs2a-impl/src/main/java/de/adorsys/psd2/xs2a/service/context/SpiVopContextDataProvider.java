/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.security.SecuringBodyMessage;
import de.adorsys.psd2.xs2a.core.security.SigningHttpMessage;
import de.adorsys.psd2.xs2a.service.RequestVopProviderService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSecuringBodyMessageDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSigningHttpMessageDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiVopContextData;
import de.adorsys.psd2.xs2a.spi.domain.vop.security.SpiSecuringBodyMessageData;
import de.adorsys.psd2.xs2a.spi.domain.vop.security.SpiSigningHttpMessageData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Provider service for creating SpiVopContextData instances for VOP (Verification of Party) operations.
 * This service follows the same pattern as SpiContextDataProvider but is specifically designed for VOP requests
 * with additional security and contract information.
 */
@RequiredArgsConstructor
@Service
public class SpiVopContextDataProvider {
    private final RequestVopProviderService requestVopProviderService;
    private final Xs2aToSpiSigningHttpMessageDataMapper xs2aToSpiSigningHttpMessageDataMapper;
    private final Xs2aToSpiSecuringBodyMessageDataMapper xs2aToSpiSecuringBodyMessageDataMapper;

    /**
     * Provides basic VOP context data without signing or securing message information.
     *
     * @return SpiVopContextData with basic request information
     */
    public SpiVopContextData provide() {
        return provideWithSigningHttpMessage(requestVopProviderService.getSigningHttpMessageData());
    }

    /**
     * Provides VOP context data with signing HTTP message information.
     *
     * @param sigHttpMsg the signing HTTP message containing digest and signature
     * @return SpiVopContextData with signing message information
     */
    public SpiVopContextData provideWithSigningHttpMessage(SigningHttpMessage sigHttpMsg) {
        var securingBodyMsg = requestVopProviderService.getSecuringBodyMessageData();
        return provide(sigHttpMsg, securingBodyMsg);
    }

    /**
     * Provides complete VOP context data with both signing and securing message information.
     *
     * @param sigHttpMsg the signing HTTP message containing digest and signature
     * @param secBodyMsg the securing body message containing signature and encryption profiles
     * @return SpiVopContextData with complete signing and securing information
     */
    public SpiVopContextData provide(SigningHttpMessage sigHttpMsg, SecuringBodyMessage secBodyMsg) {
        SpiSigningHttpMessageData spiSigningHttpMessage = xs2aToSpiSigningHttpMessageDataMapper.mapToSpiSigningHttpMessageData(sigHttpMsg);
        SpiSecuringBodyMessageData spiSecuringBodyMessage = xs2aToSpiSecuringBodyMessageDataMapper.mapToSpiSecuringBodyMessageData(secBodyMsg);

        var contextData = new SpiVopContextData();
        contextData.setXRequestId(requestVopProviderService.getRequestId());
        contextData.setInternalRequestId(requestVopProviderService.getInternalRequestId());
        contextData.setSpiSigningHttpMessage(spiSigningHttpMessage);
        contextData.setSpiSecuringBodyMessage(spiSecuringBodyMessage);
        contextData.setApIContractID(requestVopProviderService.getApiContractId());
        contextData.setAcceptLanguage(requestVopProviderService.getAcceptLanguageHeader());
        return contextData;
    }
}
