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

package de.adorsys.psd2.consent.integration.config;

import de.adorsys.psd2.consent.web.aspsp.config.EnableCmsAspspApiSwagger;
import de.adorsys.psd2.consent.web.psu.config.EnableCmsPsuApiSwagger;
import de.adorsys.psd2.consent.web.xs2a.config.EnableCmsXs2aApiSwagger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value = "de.adorsys.psd2",
    excludeFilters = @ComponentScan.Filter({
        EnableCmsXs2aApiSwagger.class,
        EnableCmsPsuApiSwagger.class,
        EnableCmsAspspApiSwagger.class
    }))
public class IntegrationTestConfiguration {
}
