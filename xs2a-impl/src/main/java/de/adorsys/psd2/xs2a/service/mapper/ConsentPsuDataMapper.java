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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConsentPsuDataMapper {
    public ConsentAuthorisationsParameters mapToUpdateConsentPsuDataReq(CommonAuthorisationParameters request,
                                                                        AuthorisationProcessorResponse response) {
        return Optional.ofNullable(response)
                   .map(data -> {
                       ConsentAuthorisationsParameters req = new ConsentAuthorisationsParameters();
                       req.setPsuData(response.getPsuData());
                       req.setConsentId(request.getBusinessObjectId());
                       req.setAuthorizationId(request.getAuthorisationId());
                       req.setAuthenticationMethodId(Optional.ofNullable(data.getChosenScaMethod())
                                                         .map(AuthenticationObject::getAuthenticationMethodId)
                                                         .orElse(null));
                       req.setScaAuthenticationData(request.getScaAuthenticationData());
                       req.setScaStatus(data.getScaStatus());
                       req.setAuthorisationType(AuthorisationType.CONSENT);
                       return req;
                   })
                   .orElse(null);
    }
}
