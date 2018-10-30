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

package de.adorsys.psd2.consent.server.config;

import de.adorsys.psd2.consent.server.service.security.provider.AesEcbCryptoProviderImpl;
import de.adorsys.psd2.consent.server.service.security.provider.CryptoProvider;
import de.adorsys.psd2.consent.server.service.security.provider.JweCryptoProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SecurityConfig {
    @Value("${server_key}")
    String serverKey;

    @Bean(value = "serverKey")
    public String serverKey() {
        return serverKey;
    }

    @Bean(value = "cryptoProviderId")
    public CryptoProvider cryptoProviderId() {
        return new AesEcbCryptoProviderImpl();
    }

    @Bean(value = "cryptoProviderConsentData")
    public CryptoProvider cryptoProviderConsentData() {
        return new JweCryptoProviderImpl();
    }
}
