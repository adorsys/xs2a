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

package de.adorsys.psd2.xs2a.config;

import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.validator.signature.DigestVerifier;
import de.adorsys.psd2.validator.signature.SignatureVerifier;
import de.adorsys.psd2.validator.signature.impl.DigestVerifierImpl;
import de.adorsys.psd2.validator.signature.impl.SignatureVerifierImpl;
import de.adorsys.psd2.xs2a.component.PaymentTypeEnumConverter;
import de.adorsys.psd2.xs2a.component.logger.request.RequestResponseLogger;
import de.adorsys.psd2.xs2a.config.converter.MappingJackson2TextMessageConverter;
import de.adorsys.psd2.xs2a.domain.InternalRequestIdHolder;
import de.adorsys.psd2.xs2a.domain.RedirectIdHolder;
import de.adorsys.psd2.xs2a.domain.ScaApproachHolder;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoHolder;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.advice.Xs2aRestExceptionHandler;
import de.adorsys.psd2.xs2a.web.interceptor.logging.*;
import de.adorsys.psd2.xs2a.web.interceptor.tpp.TppStopListInterceptor;
import de.adorsys.psd2.xs2a.web.interceptor.validator.PaymentParametersValidationInterceptor;
import de.adorsys.psd2.xs2a.web.interceptor.validator.RequestValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;

import static de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant.*;

@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WebConfig implements WebMvcConfigurer {

    @Qualifier("xs2aCorsConfigProperties")
    private final CorsConfigurationProperties corsConfigurationProperties;
    private final TppService tppService;
    private final TppStopListService tppStopListService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ErrorMapperContainer errorMapperContainer;
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final RequestValidationInterceptor requestValidationInterceptor;
    private final RedirectIdService redirectIdService;
    private final RequestResponseLogger requestResponseLogger;
    private final LoggingContextService loggingContextService;
    private final PathParameterExtractor pathParameterExtractor;
    private final Xs2aRestExceptionHandler xs2aRestExceptionHandler;
    private final PaymentParametersValidationInterceptor paymentParametersValidationInterceptor;

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, xs2aRestExceptionHandler);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Please, keep this interceptor's order, because it is important, that logging interceptors will be called before the validation ones to log all the requests (even wrong ones).
        // The interceptors are executed in the order in which they are declared for preHandle(...) and vice versa for postHandle(...).
        // Logging interceptors:
        registry.addInterceptor(new AccountLoggingInterceptor(tppService, loggingContextService, pathParameterExtractor)).addPathPatterns(ACCOUNTS_PATH, BENEFICIARIES_PATH, CARD_ACCOUNTS_PATH);
        registry.addInterceptor(new ConsentLoggingInterceptor(tppService, redirectIdService, loggingContextService, pathParameterExtractor)).addPathPatterns(CONSENTS_PATH);
        registry.addInterceptor(new FundsConfirmationLoggingInterceptor(tppService)).addPathPatterns(FUNDS_CONFIRMATION_PATH);
        registry.addInterceptor(new PaymentLoggingInterceptor(tppService, redirectIdService, loggingContextService, pathParameterExtractor)).addPathPatterns(SINGLE_PAYMENTS_PATH, BULK_PAYMENTS_PATH, PERIODIC_PAYMENTS_PATH);
        registry.addInterceptor(new SigningBasketLoggingInterceptor(tppService, redirectIdService, pathParameterExtractor)).addPathPatterns(SIGNING_BASKETS_PATH);
        registry.addInterceptor(new RequestResponseLoggingInterceptor(requestResponseLogger)).addPathPatterns(getAllXs2aEndpointPaths());
        registry.addInterceptor(new TppStopListInterceptor(errorMapperContainer, tppService, tppStopListService, serviceTypeDiscoveryService, errorTypeMapper, xs2aObjectMapper))
            .addPathPatterns(getAllXs2aEndpointPaths());

        // This interceptor cannot use some definite path from constants, as payment services have nothing common in
        // their URLs (and 'payment-service' or 'payment-type' can be broken).
        registry.addInterceptor(paymentParametersValidationInterceptor).addPathPatterns(GLOBAL_PATH);
        registry.addInterceptor(requestValidationInterceptor).addPathPatterns(getAllXs2aEndpointPaths());
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

    @Bean
    @RequestScope
    public RedirectIdHolder getRedirectIdHolder() {
        return new RedirectIdHolder();
    }

    @Bean
    @RequestScope
    public InternalRequestIdHolder getInternalRequestIdHolder() {
        return new InternalRequestIdHolder();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PaymentTypeEnumConverter());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2TextMessageConverter(xs2aObjectMapper));
    }

    @Bean
    public DigestVerifier digestVerifier() {
        return new DigestVerifierImpl();
    }

    @Bean
    public SignatureVerifier signatureVerifier() {
        return new SignatureVerifierImpl();
    }
}
