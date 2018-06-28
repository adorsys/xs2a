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

package de.adorsys.aspsp.xs2a.config.rest.keycloack;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class KeycloackRestConfig {

    @Bean(name = "keycloackRestTemplate")
    public RestTemplate keycloackRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        RestTemplate rest = new RestTemplate(buildHttpRequestFactoryWithoutSSLvalidation());
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        rest.getMessageConverters().add(new StringHttpMessageConverter());
        rest.setErrorHandler(new KeycloackRestErrorHandler());
        return rest;
    }

    private HttpComponentsClientHttpRequestFactory buildHttpRequestFactoryWithoutSSLvalidation() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                                    .loadTrustMaterial(null, acceptingTrustStrategy)
                                    .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                                             .setSSLSocketFactory(csf)
                                             .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }
}
