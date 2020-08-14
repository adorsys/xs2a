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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.core.data.piis.PiisConsentData;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PiisConsentModelMapper {
    private final HrefLinkMapper hrefLinkMapper;
    private final AccountModelMapper accountModelMapper;
    private final ConsentModelMapper consentModelMapper;

    public CreatePiisConsentRequest toCreatePiisConsentRequest(ConsentsConfirmationOfFunds consentsConfirmationOfFunds) {
        return Optional.ofNullable(consentsConfirmationOfFunds)
                   .map(c ->
                            new CreatePiisConsentRequest(
                                consentModelMapper.mapToAccountReference(consentsConfirmationOfFunds.getAccount()),
                                consentsConfirmationOfFunds.getCardNumber(),
                                consentsConfirmationOfFunds.getCardExpiryDate(),
                                consentsConfirmationOfFunds.getCardInformation(),
                                consentsConfirmationOfFunds.getRegistrationInformation()
                            ))
                   .orElse(null);
    }

    public ConsentsConfirmationOfFundsResponse mapToConsentsConfirmationOfFundsResponse(Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse) {
        return Optional.ofNullable(xs2aConfirmationOfFundsResponse).map(
            response -> {
                ConsentsConfirmationOfFundsResponse consentsConfirmationOfFundsResponse = new ConsentsConfirmationOfFundsResponse();
                consentsConfirmationOfFundsResponse.setConsentId(response.getConsentId());
                consentsConfirmationOfFundsResponse.setConsentStatus(ConsentStatus.fromValue(response.getConsentStatus()));
                consentsConfirmationOfFundsResponse.setLinks(hrefLinkMapper.mapToLinksMap(response.getLinks()));
                consentsConfirmationOfFundsResponse.setPsuMessage(response.getPsuMessage());
                return consentsConfirmationOfFundsResponse;
            }
        ).orElse(null);
    }

    public ConsentConfirmationOfFundsContentResponse mapToConsentConfirmationOfFundsContentResponse(PiisConsent piisConsent) {

        PiisConsentData consentData = piisConsent.getConsentData();

        ConsentConfirmationOfFundsContentResponse consentConfirmationOfFundsContentResponse = new ConsentConfirmationOfFundsContentResponse();
        consentConfirmationOfFundsContentResponse.setAccount(accountModelMapper.mapToAccountReference(piisConsent.getAccountReference()));
        consentConfirmationOfFundsContentResponse.setConsentStatus(ConsentStatus.fromValue(piisConsent.getConsentStatus().getValue()));
        consentConfirmationOfFundsContentResponse.setCardInformation(consentData.getCardInformation());
        consentConfirmationOfFundsContentResponse.setCardExpiryDate(consentData.getCardExpiryDate());
        consentConfirmationOfFundsContentResponse.setCardNumber(consentData.getCardNumber());
        consentConfirmationOfFundsContentResponse.setRegistrationInformation(consentData.getRegistrationInformation());

        return consentConfirmationOfFundsContentResponse;
    }

    public ConsentConfirmationOfFundsStatusResponse mapToConsentConfirmationOfFundsStatusResponse(ConsentStatusResponse consentStatusResponse) {
        ConsentConfirmationOfFundsStatusResponse consentConfirmationOfFundsStatusResponse = new ConsentConfirmationOfFundsStatusResponse();
        consentConfirmationOfFundsStatusResponse.setConsentStatus(ConsentStatus.fromValue(consentStatusResponse.getConsentStatus()));
        return consentConfirmationOfFundsStatusResponse;
    }
}
