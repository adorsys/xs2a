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

import static de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach.OAUTH;
import static de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach.REDIRECT;
import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.AUTHORIZATION_HEADER;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.aspsp.xs2a.component.PaymentTypeEnumConverter;
import de.adorsys.aspsp.xs2a.config.rest.BearerToken;
import de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach;
import de.adorsys.aspsp.xs2a.service.authorization.AisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.authorization.DecoupledAisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.authorization.EmbeddedAisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.authorization.OauthAisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.authorization.RedirectAisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.consent.pis.PisConsentService;
import de.adorsys.aspsp.xs2a.service.keycloak.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.service.mapper.ObjectMapperFactory;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.payment.DecoupedScaPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.EmbeddedScaPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.OauthScaPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.ReadPaymentFactory;
import de.adorsys.aspsp.xs2a.service.payment.RedirectScaPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.ScaPaymentService;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.service.validator.RequestValidatorService;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.web.interceptor.HandlerInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig extends WebMvcConfigurerAdapter {
    @Value("${application.ais.transaction.max-length}")
    private int maxNumberOfCharInTransactionJson;

    private final AspspProfileService aspspProfileService;

    private final KeycloakInvokerService keycloakInvokerService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:error_message");
        messageSource.setCacheSeconds(3600); //reload messages every hour
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
    	return ObjectMapperFactory.instance();
    }

    @Bean
    public ParametersFactory parametersFactory(ObjectMapper objectMapper) {
        return new ParametersFactory(objectMapper);
    }

    @Bean
    public RequestValidatorService requestValidatorService() {
        return new RequestValidatorService();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor(requestValidatorService(), objectMapper(), messageSource()));
    }

    @Bean
    public int maxNumberOfCharInTransactionJson() {
        return maxNumberOfCharInTransactionJson;
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public BearerToken getBearerToken(HttpServletRequest request) {
        return new BearerToken(getAccessToken(request));
    }

    private String getAccessToken(HttpServletRequest request) {
        ScaApproach scaApproach = aspspProfileService.readScaApproach();
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
    	switch (aspspProfileService.readScaApproach()) {
		case OAUTH:
            return new OauthScaPaymentService(paymentMapper, paymentSpi);
		case DECOUPLED:
            return new DecoupedScaPaymentService();
		case EMBEDDED:
            return new EmbeddedScaPaymentService();
		default:
	        return new RedirectScaPaymentService(pisConsentService, paymentMapper, paymentSpi);
		}
    }

    @Bean
    public AisAuthorizationService authorizationService(AccountSpi accountSpi) {
    	switch (aspspProfileService.readScaApproach()) {
		case OAUTH:
			return new OauthAisAuthorizationService();
		case DECOUPLED:
			return new DecoupledAisAuthorizationService();
		case EMBEDDED:
			return new EmbeddedAisAuthorizationService(accountSpi);
		default:
			return new RedirectAisAuthorizationService();
		}
    }

    @Bean
    public ServiceLocatorFactoryBean readPaymentFactory() {
        ServiceLocatorFactoryBean serviceLocatorFactoryBean = new ServiceLocatorFactoryBean();
        serviceLocatorFactoryBean.setServiceLocatorInterface(ReadPaymentFactory.class);
        return serviceLocatorFactoryBean;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PaymentTypeEnumConverter());
    }

}

