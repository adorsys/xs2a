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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmbeddedPiisAuthorizationService extends AbstractPiisAuthorizationService {


    public EmbeddedPiisAuthorizationService(Xs2aConsentService consentService,
                                            Xs2aAuthorisationService authorisationService,
                                            ConsentPsuDataMapper consentPsuDataMapper,
                                            Xs2aPiisConsentService piisConsentService,
                                            Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper) {
        super(consentService, authorisationService, consentPsuDataMapper, piisConsentService, xs2aConsentAuthorisationMapper);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.EMBEDDED;
    }
}
