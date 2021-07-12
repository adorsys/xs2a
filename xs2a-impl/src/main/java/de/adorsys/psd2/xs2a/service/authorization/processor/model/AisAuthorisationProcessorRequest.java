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

package de.adorsys.psd2.xs2a.service.authorization.processor.model;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import lombok.Value;

@Value
public class AisAuthorisationProcessorRequest extends AuthorisationProcessorRequest {

    public AisAuthorisationProcessorRequest(ScaApproach scaApproach, ScaStatus scaStatus,
                                            CommonAuthorisationParameters updateAuthorisationRequest,
                                            Authorisation authorisation) {
        super(ServiceType.AIS, scaApproach, scaStatus, updateAuthorisationRequest, authorisation);
    }
}
