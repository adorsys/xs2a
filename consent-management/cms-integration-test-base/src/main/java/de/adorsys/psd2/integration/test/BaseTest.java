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

package de.adorsys.psd2.integration.test;

import de.adorsys.xs2a.reader.JsonReader;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@ActiveProfiles("integration-test")
@SuppressWarnings("rawtypes")
public class BaseTest extends AbstractContainerDatabaseTest {

    protected JsonReader jsonReader = new JsonReader();

    @Container
    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11")
                                                                       .withDatabaseName("consent")
                                                                       .withUsername("admin")
                                                                       .withPassword("secret");

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            if (!postgreSQLContainer.isRunning()) {
                postgreSQLContainer.start();
            }
            TestPropertyValues.of(
                "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                "spring.jpa.show_sql=false"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    protected void clearData() {
        performQuery(postgreSQLContainer, jsonReader.getStringFromFile("db/scripts/clear-data.sql"));
    }
}
