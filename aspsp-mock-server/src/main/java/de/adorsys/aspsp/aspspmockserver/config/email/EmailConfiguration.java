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

package de.adorsys.aspsp.aspspmockserver.config.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Configuration
public class EmailConfiguration {
    @Autowired
    private EmailConfigurationProperties emailConfigurationProperties;

    //TODO Move to EmailConfigurationProperties: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/176
    @Value("#{ new Boolean('${spring.mail.properties.mail.smtp.auth}') }")
    private boolean auth;

    @Value("#{ new Boolean('${spring.mail.properties.mail.smtp.starttls.enable}') }")
    private boolean isStarttlsEnable;

    @Bean
    public JavaMailSender javaMailSender() {
        if (isParametersExist()) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(emailConfigurationProperties.getHost());
            sender.setPort(emailConfigurationProperties.getPort());
            sender.setUsername(emailConfigurationProperties.getUsername());
            sender.setPassword(emailConfigurationProperties.getPassword());
            sender.setJavaMailProperties(buildMailProperties());
            return sender;
        }
        log.warn("Email properties has not been set");
        return null;
    }

    private Properties buildMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", isStarttlsEnable);
        return props;
    }

    private boolean isParametersExist() {
        return isNotBlank(emailConfigurationProperties.getHost()) &&
                   emailConfigurationProperties.getPort() != 0 &&
                   isAuthParametersExist();
    }

    private boolean isAuthParametersExist() {
        return !auth || isNotBlank(emailConfigurationProperties.getUsername()) &&
                            isNotBlank(emailConfigurationProperties.getPassword());
    }
}
