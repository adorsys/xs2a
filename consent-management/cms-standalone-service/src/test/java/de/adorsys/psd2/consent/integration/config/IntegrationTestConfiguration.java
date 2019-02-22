/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.integration.config;

import de.adorsys.psd2.consent.config.EnableCmsSwagger;
import de.adorsys.psd2.consent.config.HibernateListenerConfig;
import de.adorsys.psd2.consent.config.ServiceInstanceIdEventListener;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Configuration
@ComponentScan(value = "de.adorsys.psd2",
    excludeFilters = @ComponentScan.Filter(EnableCmsSwagger.class))
public class IntegrationTestConfiguration {
}
