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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DecoupledPiisAuthorizationService extends AbstractPiisAuthorizationService {


    public DecoupledPiisAuthorizationService(Xs2aConsentService consentService,
                                             Xs2aAuthorisationService authorisationService,
                                             ConsentPsuDataMapper consentPsuDataMapper,
                                             Xs2aPiisConsentService piisConsentService) {
        super(consentService, authorisationService, consentPsuDataMapper, piisConsentService);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.DECOUPLED;
    }
}
