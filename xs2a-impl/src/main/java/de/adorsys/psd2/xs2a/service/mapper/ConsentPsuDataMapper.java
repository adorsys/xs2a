/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
