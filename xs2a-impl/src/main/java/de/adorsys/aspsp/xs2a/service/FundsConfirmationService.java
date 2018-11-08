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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiFundsConfirmationRequestMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.PiisConsentValidationService;
import de.adorsys.psd2.consent.api.piis.CmsPiisValidationInfo;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final AspspProfileServiceWrapper profileService;
    private final FundsConfirmationSpi fundsConfirmationSpi;
    private final FundsConfirmationConsentDataService fundsConfirmationConsentDataService;
    private final FundsConfirmationPsuDataService fundsConfirmationPsuDataService;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;
    private final Xs2aToSpiFundsConfirmationRequestMapper xs2aToSpiFundsConfirmationRequestMapper;
    private final PiisConsentValidationService piisConsentValidationService;
    private final PiisConsentService piisConsentService;

    /**
     * Checks if the account balance is sufficient for requested operation
     *
     * @param request Contains the requested balanceAmount in order to comparing with available balanceAmount on account
     * @return Response with result 'true' if there are enough funds on the account, 'false' if not
     */
    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        String consentId = null;

        if (profileService.isPiisConsentSupported()) {
            AccountReferenceSelector selector = request.getPsuAccount().getUsedAccountReferenceSelector();
            List<CmsPiisValidationInfo> response = piisConsentService.getPiisConsentListByAccountIdentifier(request.getPsuAccount().getCurrency(), selector, selector.getAccountReferenceValue(request.getPsuAccount()));
            ResponseObject<String> validationResult = piisConsentValidationService.validatePiisConsentData(response);

            if (validationResult.hasError()) {
                return ResponseObject.<FundsConfirmationResponse>builder()
                           .fail(validationResult.getError())
                           .build();
            }

            consentId = validationResult.getBody();
        }

        SpiFundsConfirmationRequest spiRequest = xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(request);
        AspspConsentData aspspConsentData = fundsConfirmationConsentDataService.getAspspConsentDataByConsentId(consentId); //TODO Rework it after service implementation https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/379
        PsuIdData psuData = fundsConfirmationPsuDataService.getPsuDataByConsentId(consentId);  //TODO Rework it after service implementation https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/379
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);

        SpiResponse<Boolean> fundsSufficientCheck = fundsConfirmationSpi.performFundsSufficientCheck(
            spiPsuData,
            consentId,
            spiRequest,
            aspspConsentData
        );

        aspspConsentData = fundsSufficientCheck.getAspspConsentData();
        fundsConfirmationConsentDataService.updateAspspConsentData(aspspConsentData);

        if (fundsSufficientCheck.hasError()) {
            return ResponseObject.<FundsConfirmationResponse>builder()
                       .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                       .build();
        }

        FundsConfirmationResponse fundsConfirmationResponse = new FundsConfirmationResponse(BooleanUtils.isTrue(fundsSufficientCheck.getPayload()));

        return ResponseObject.<FundsConfirmationResponse>builder()
                   .body(fundsConfirmationResponse)
                   .build();
    }
}
