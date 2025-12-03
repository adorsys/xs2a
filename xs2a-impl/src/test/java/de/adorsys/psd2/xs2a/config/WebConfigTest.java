/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.HttpMessageConverter;

import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.component.logger.request.RequestResponseLogger;
import de.adorsys.psd2.xs2a.config.converter.MappingJackson2TextMessageConverter;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.web.PathParameterExtractor;
import de.adorsys.psd2.xs2a.web.advice.Xs2aRestExceptionHandler;
import de.adorsys.psd2.xs2a.web.interceptor.validator.PaymentParametersValidationInterceptor;
import de.adorsys.psd2.xs2a.web.interceptor.validator.RequestValidationInterceptor;
import de.adorsys.psd2.xs2a.web.interceptor.validator.VopParametersValidationInterceptor;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private CorsConfigurationProperties corsConfigurationProperties;

    @Mock
    private TppService tppService;

    @Mock
    private TppStopListService tppStopListService;

    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;

    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Mock
    private ErrorMapperContainer errorMapperContainer;

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;

    @Mock
    private RequestValidationInterceptor requestValidationInterceptor;

    @Mock
    private RedirectIdService redirectIdService;

    @Mock
    private RequestResponseLogger requestResponseLogger;

    @Mock
    private LoggingContextService loggingContextService;

    @Mock
    private PathParameterExtractor pathParameterExtractor;

    @Mock
    private Xs2aRestExceptionHandler xs2aRestExceptionHandler;

    @Mock
    private PaymentParametersValidationInterceptor paymentParametersValidationInterceptor;

    @Mock
    private VopParametersValidationInterceptor vopParametersValidationInterceptor;

    @Test
    void extendMessageConverters() {
        WebConfig webConfig = new WebConfig(corsConfigurationProperties, tppService,
                                            tppStopListService, serviceTypeDiscoveryService, errorTypeMapper,
                                            errorMapperContainer, xs2aObjectMapper, requestValidationInterceptor,
                                            redirectIdService, requestResponseLogger, loggingContextService,
                                            pathParameterExtractor, xs2aRestExceptionHandler, paymentParametersValidationInterceptor,
                                            vopParametersValidationInterceptor);
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        webConfig.extendMessageConverters(messageConverters);

        assertEquals(1, messageConverters.size());
        assertTrue(messageConverters.get(0) instanceof MappingJackson2TextMessageConverter);
    }
}
