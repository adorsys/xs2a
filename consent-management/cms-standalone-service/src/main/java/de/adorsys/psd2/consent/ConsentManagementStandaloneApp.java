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

package de.adorsys.psd2.consent;

import de.adorsys.psd2.consent.web.aspsp.config.EnableCmsAspspApiSwagger;
import de.adorsys.psd2.consent.web.psu.config.EnableCmsPsuApiSwagger;
import de.adorsys.psd2.consent.web.xs2a.config.EnableCmsXs2aApiSwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableCmsXs2aApiSwagger
@EnableCmsPsuApiSwagger
@EnableCmsAspspApiSwagger
@SpringBootApplication
@ComponentScan("de.adorsys.psd2")
public class ConsentManagementStandaloneApp {

    public static void main(String[] args) {
        SpringApplication.run(ConsentManagementStandaloneApp.class, args);
    }
}

