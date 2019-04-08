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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.component.PaymentTypeEnumConverter;
import de.adorsys.psd2.xs2a.domain.ScaApproachHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.RequestValidatorService;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.web.interceptor.HandlerInterceptor;
import de.adorsys.psd2.xs2a.web.interceptor.HeaderValidationInterceptor;
import de.adorsys.psd2.xs2a.web.interceptor.logging.*;
import de.adorsys.psd2.xs2a.web.interceptor.tpp.TppStopListInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;

import static de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant.*;

@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebConfig extends WebMvcConfigurerAdapter {
    @Value("${application.ais.transaction.max-length}")
    private int maxNumberOfCharInTransactionJson;

    @Qualifier("xs2aCorsConfigProperties")
    private final CorsConfigurationProperties corsConfigurationProperties;
    private final TppService tppService;
    private final TppStopListService tppStopListService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ErrorMapperContainer errorMapperContainer;
    private final ObjectMapper objectMapper;
    private final HeaderValidationInterceptor headerValidationInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Bean
    public RequestValidatorService requestValidatorService() {
        return new RequestValidatorService();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Please, keep this interceptor's order, because it is important, that logging interceptors will be called before the validation ones to log all the requests (even wrong ones).
        // The interceptors are executed in the order in which they are declared for preHandle(...) and vice versa for postHandle(...).
        // Logging interceptors:
        registry.addInterceptor(new AccountLoggingInterceptor(tppService)).addPathPatterns(ACCOUNTS_PATH);
        registry.addInterceptor(new ConsentLoggingInterceptor(tppService)).addPathPatterns(CONSENTS_PATH);
        registry.addInterceptor(new FundsConfirmationLoggingInterceptor(tppService)).addPathPatterns(FUNDS_CONFIRMATION_PATH);
        registry.addInterceptor(new PaymentLoggingInterceptor(tppService)).addPathPatterns(SINGLE_PAYMENTS_PATH, BULK_PAYMENTS_PATH, PERIODIC_PAYMENTS_PATH);
        registry.addInterceptor(new SigningBasketLoggingInterceptor(tppService)).addPathPatterns(SIGNING_BASKETS_PATH);

        registry.addInterceptor(new TppStopListInterceptor(errorMapperContainer, tppService, tppStopListService, serviceTypeDiscoveryService, errorTypeMapper, objectMapper))
            .addPathPatterns(getAllXs2aEndpointPaths());

        registry.addInterceptor(headerValidationInterceptor).addPathPatterns(getAllXs2aEndpointPaths());

        registry.addInterceptor(new HandlerInterceptor(requestValidatorService(), serviceTypeDiscoveryService, errorTypeMapper, errorMapperContainer, objectMapper))
            .addPathPatterns(getAllXs2aEndpointPaths());
    }

    @Bean
    public int maxNumberOfCharInTransactionJson() {
        return maxNumberOfCharInTransactionJson;
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v1/**")
            .allowCredentials(corsConfigurationProperties.getAllowCredentials())
            .allowedOrigins(getTargetParameters(corsConfigurationProperties.getAllowedOrigins()))
            .allowedHeaders(getTargetParameters(corsConfigurationProperties.getAllowedHeaders()))
            .allowedMethods(getTargetParameters(corsConfigurationProperties.getAllowedMethods()))
            .maxAge(corsConfigurationProperties.getMaxAge());
    }

    private String[] getTargetParameters(List<String> targetParameters) {
        return targetParameters.toArray(new String[0]);
    }

    @Bean
    @RequestScope
    public TppInfoHolder getTppInfoHolder() {
        return new TppInfoHolder();
    }

    @Bean
    @RequestScope
    public ScaApproachHolder getScaApproachHolder() {
        return new ScaApproachHolder();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PaymentTypeEnumConverter());
    }
}

