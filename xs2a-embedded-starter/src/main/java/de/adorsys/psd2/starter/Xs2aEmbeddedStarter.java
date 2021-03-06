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
