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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.aspsp.xs2a.component.DateTimeDeserializer;
import de.adorsys.aspsp.xs2a.component.PaymentTypeEnumConverter;
import de.adorsys.aspsp.xs2a.config.rest.BearerToken;
import de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach;
import de.adorsys.aspsp.xs2a.service.keycloak.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.PisConsentMapper;
import de.adorsys.aspsp.xs2a.service.payment.*;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.service.validator.RequestValidatorService;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.aspsp.xs2a.web.interceptor.HandlerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.aspsp.ScaApproach.*;
import static de.adorsys.aspsp.xs2a.spi.domain.constant.AuthorizationConstant.AUTHORIZATION_HEADER;

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
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.registerModule(getDateTimeDeserializerModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module()); // add support for Optionals
        objectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        objectMapper.registerModule(new ParameterNamesModule()); // support for multiargs constructors
        return objectMapper;
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
    public ScaPaymentService scaPaymentService(ConsentSpi consentSpi, PaymentMapper paymentMapper, PaymentSpi paymentSpi, PisConsentMapper pisConsentMapper) {
        ScaApproach scaApproach = aspspProfileService.readScaApproach();
        if (OAUTH == scaApproach) {
            return new OauthScaPaymentService(paymentMapper, paymentSpi);
        } else if (DECOUPLED == scaApproach) {
            return new DecoupedScaPaymentService();
        } else if (EMBEDDED == scaApproach) {
            return new EmbeddedScaPaymentService();
        }
        return new RedirectScaPaymentService(consentSpi, paymentMapper, paymentSpi, pisConsentMapper);
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

    private SimpleModule getDateTimeDeserializerModule() {
        SimpleModule dateTimeModule = new SimpleModule();
        dateTimeModule.addDeserializer(LocalDateTime.class, new DateTimeDeserializer());
        return dateTimeModule;
    }
}

