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
    private final CoreObjectsMapper coreObjectsMapper;
    private final TppMessageGenericMapper tppMessageGenericMapper;

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
                consentsConfirmationOfFundsResponse.setTppMessage(tppMessageGenericMapper.mapToTppMessageGenericList(response.getTppMessageInformation()));
                consentsConfirmationOfFundsResponse.setScaStatus(Optional.ofNullable(response.getScaStatus())
                                                                     .map(coreObjectsMapper::mapToModelScaStatus)
                                                                     .orElse(null));
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
