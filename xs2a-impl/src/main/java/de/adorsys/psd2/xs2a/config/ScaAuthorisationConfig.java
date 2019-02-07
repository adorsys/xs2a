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

package de.adorsys.psd2.xs2a.config;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.service.authorization.ais.*;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

@Configuration
@RequiredArgsConstructor
public class ScaAuthorisationConfig {

    @Bean
    public ScaPaymentService scaPaymentService(OauthScaPaymentService oauthScaPaymentService,
                                               RedirectScaPaymentService redirectScaPaymentService,
                                               EmbeddedScaPaymentService embeddedScaPaymentService,
                                               DecoupledScaPaymentService decoupledScaPaymentService) {
        ScaApproach scaApproach = getScaApproach();
        if (OAUTH == scaApproach) {
            return oauthScaPaymentService;
        }
        if (DECOUPLED == scaApproach) {
            return decoupledScaPaymentService;
        }
        if (EMBEDDED == scaApproach) {
            return embeddedScaPaymentService;
        }
        return redirectScaPaymentService;
    }

    // TODO will be implemented in https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/597
    private ScaApproach getScaApproach() {
        return REDIRECT;
    }
}
