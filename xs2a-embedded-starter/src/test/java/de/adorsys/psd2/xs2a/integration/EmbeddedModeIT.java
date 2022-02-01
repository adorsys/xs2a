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

package de.adorsys.psd2.xs2a.integration;

import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.starter.Xs2aEmbeddedStarter;
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
    Xs2aInterfaceConfig.class
})
class EmbeddedModeIT {

    // Suppress "Tests should include assertions" Sonar rule as this test is not supposed to assert anything
    @SuppressWarnings("squid:S2699")
    @Test
    void startWithServicesInEmbeddedMode() {
        // Test whether application starts without any exception
    }
}
