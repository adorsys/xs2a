/*
package de.adorsys.aspsp.xs2a.spi.config;

import de.adorsys.aspsp.xs2a.spi.domain.security.BearerToken;
import de.adorsys.aspsp.xs2a.spi.rest.BearerTokenInterceptor;
import de.adorsys.aspsp.xs2a.spi.rest.exception.RestTemplateErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("mockspi")
public class RestConfig {
    @Value("${http-client.read-timeout.ms:10000}")
    private int readTimeout;
    @Value("${http-client.connection-timeout.ms:10000}")
    private int connectionTimeout;
    @Autowired
    private BearerToken bearerToken;

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplate rest = new RestTemplate(clientHttpRequestFactory);
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rest.getMessageConverters().add(new StringHttpMessageConverter());
        //rest.getInterceptors().add(new BearerTokenInterceptor(bearerToken));
        rest.setErrorHandler(new RestTemplateErrorHandler());
        return rest;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectionTimeout);
        return factory;
    }
}
*/
