/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.starter.Xs2aEmbeddedStarter;
import de.adorsys.psd2.starter.config.validation.PaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.config.CorsConfigurationProperties;
import de.adorsys.psd2.xs2a.config.WebConfig;
import de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant;
import de.adorsys.psd2.xs2a.config.Xs2aInterfaceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@AutoConfigureTestDatabase
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"integration-test", "mock-qwac"})
@SpringBootTest(classes = Xs2aEmbeddedStarter.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class,
    PaymentValidationConfigImpl.class
})
class EmbeddedModeIT {

    // Suppress "Tests should include assertions" Sonar rule as this test is not supposed to assert anything
    @SuppressWarnings("squid:S2699")
    @Test
    void startWithServicesInEmbeddedMode() {
        // Test whether application starts without any exception
    }
}
