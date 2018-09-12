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

package de.adorsys.aspsp.xs2a.config;

import de.adorsys.aspsp.xs2a.config.rest.BearerToken;
import de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach;
import de.adorsys.aspsp.xs2a.service.authorization.ais.*;
import de.adorsys.aspsp.xs2a.service.authorization.pis.*;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import de.adorsys.aspsp.xs2a.service.keycloak.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.payment.*;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach.*;
import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.AUTHORIZATION_HEADER;

@Configuration
@RequiredArgsConstructor
public class ScaAuthorizationConfig {
    private final AspspProfileService aspspProfileService;
    private final KeycloakInvokerService keycloakInvokerService;

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public BearerToken getBearerToken(HttpServletRequest request) {
        return new BearerToken(getAccessToken(request));
    }

    private String getAccessToken(HttpServletRequest request) {
        ScaApproach scaApproach = getScaApproach();
        String accessToken = null;
        if (OAUTH == scaApproach) {
            accessToken = obtainAccessTokenFromHeader(request);
        } else if (REDIRECT == scaApproach) {
            accessToken = keycloakInvokerService.obtainAccessToken();
        }
        return Optional.ofNullable(accessToken)
                   .orElseThrow(IllegalArgumentException::new);
    }

    private String obtainAccessTokenFromHeader(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION_HEADER);
    }

    @Bean
    public ScaPaymentService scaPaymentService(PisConsentService pisConsentService, PaymentMapper paymentMapper, PaymentSpi paymentSpi) {
        ScaApproach scaApproach = getScaApproach();
        if (OAUTH == scaApproach) {
            return new OauthScaPaymentService(paymentMapper, paymentSpi);
        }
        if (DECOUPLED == scaApproach) {
            return new DecoupedScaPaymentService();
        }
        if (EMBEDDED == scaApproach) {
            return new EmbeddedScaPaymentService(pisConsentService);
        }
        return new RedirectScaPaymentService(pisConsentService, paymentMapper, paymentSpi);
    }

    @Bean
    public AisAuthorizationService aisAuthorizationService(AccountSpi accountSpi, AisConsentService aisConsentService, Xs2aAisConsentMapper aisConsentMapper) {
        switch (getScaApproach()) {
            case OAUTH:
                return new OauthAisAuthorizationService();
            case DECOUPLED:
                return new DecoupledAisAuthorizationService();
            case EMBEDDED:
                return new EmbeddedAisAuthorizationService(accountSpi, aisConsentService, aisConsentMapper);
            default:
                return new RedirectAisAuthorizationService();
        }
    }

    @Bean
    public PisAuthorizationService pisAuthorizationService(PisConsentService pisConsentService, Xs2aPisConsentMapper pisConsentMapper) {
        ScaApproach scaApproach = getScaApproach();
        if (OAUTH == scaApproach) {
            return new OauthPisAuthorizationService();
        }
        if (DECOUPLED == scaApproach) {
            return new DecoupledPisAuthorizationService();
        }
        if (EMBEDDED == scaApproach) {
            return new EmbeddedPisAuthorizationService(pisConsentService, pisConsentMapper);
        }
        return new RedirectPisAuthorizationService();
    }

    private ScaApproach getScaApproach() {
        return aspspProfileService.readScaApproach();
    }
}
