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

package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.RedirectPisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ScaAuthorisationConfigRedirectTest {

    @Bean
    public ScaPaymentService scaPaymentService(OauthScaPaymentService oauthScaPaymentService,
                                               RedirectScaPaymentService redirectScaPaymentService,
                                               EmbeddedScaPaymentService embeddedScaPaymentService,
                                               DecoupledScaPaymentService decoupledScaPaymentService) {

        return redirectScaPaymentService;
    }

    @Bean
    public AisAuthorizationService aisAuthorizationService(Xs2aAisConsentService aisConsentService,
                                                           Xs2aAisConsentMapper aisConsentMapper,
                                                           AisScaStageAuthorisationFactory scaStageAuthorisationFactory) {
        return new RedirectAisAuthorizationService(aisConsentService);
    }

    @Bean
    public PisScaAuthorisationService pisScaAuthorisationService(PisAuthorisationService authorisationService,
                                                                 Xs2aPisCommonPaymentMapper pisCommonPaymentMapper) {
        return new RedirectPisScaAuthorisationService(authorisationService, pisCommonPaymentMapper);
    }
}
