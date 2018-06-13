package de.adorsys.aspsp.xs2a.spi.config;

import de.adorsys.aspsp.xs2a.spi.domain.security.BearerToken;
import de.adorsys.aspsp.xs2a.spi.rest.BearerTokenInterceptor;
import de.adorsys.aspsp.xs2a.spi.rest.exception.AspspProfileRestTemplateErrorHandler;
import de.adorsys.aspsp.xs2a.spi.rest.exception.RestTemplateErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@Profile("mockspi")
public class RestClientConfig {
    @Value("${http-client.read-timeout.ms:10000}")
    private int readTimeout;
    @Value("${http-client.connection-timeout.ms:10000}")
    private int connectionTimeout;

    @Autowired
    private BearerToken bearerToken;

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RestTemplate restTemplate(){
        RestTemplate rest = new RestTemplate(clientHttpRequestFactory());
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rest.getMessageConverters().add(new StringHttpMessageConverter());
        rest.getInterceptors().add(new BearerTokenInterceptor(bearerToken.getToken()));
        rest.setErrorHandler(new RestTemplateErrorHandler());
        return rest;
    }

    @Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RestTemplate aspspProfileRestTemplate(){
        RestTemplate rest = new RestTemplate(clientHttpRequestFactory());
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rest.getMessageConverters().add(new StringHttpMessageConverter());
        rest.setErrorHandler(new AspspProfileRestTemplateErrorHandler());
        return rest;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectionTimeout);
        return factory;
    }
}
