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

package de.adorsys.psd2.starter;

import de.adorsys.psd2.xs2a.config.EnableXs2aInterface;
import de.adorsys.psd2.xs2a.web.config.EnableXs2aSwagger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableXs2aSwagger
@EnableXs2aInterface
@SpringBootApplication
@EnableTransactionManagement
@EntityScan({"de.adorsys.psd2.consent.domain", "de.adorsys.psd2.event.persist.entity", "de.adorsys.psd2.report.entity"})
@EnableJpaRepositories(basePackages = {"de.adorsys.psd2.consent.repository", "de.adorsys.psd2.event", "de.adorsys.psd2.report.jpa"})
@ComponentScan(basePackages = {"de.adorsys.psd2.stub", "de.adorsys.psd2.starter.config", "de.adorsys.psd2.report"})
public class Xs2aEmbeddedStarter {
    public static void main(String[] args) {
        SpringApplication.run(Xs2aEmbeddedStarter.class, args);
    }

}
