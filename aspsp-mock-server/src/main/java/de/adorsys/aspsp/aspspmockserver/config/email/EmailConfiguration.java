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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

import static de.adorsys.aspsp.aspspmockserver.config.email.EmailConfigurationProperties.MAIL_SMTP_AUTH_PROPERTY;
import static de.adorsys.aspsp.aspspmockserver.config.email.EmailConfigurationProperties.MAIL_SMTP_STARTTLS_ENABLED_PROPERTY;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmailConfiguration {
    private final EmailConfigurationProperties emailConfigurationProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        if (isParametersExist()) {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(emailConfigurationProperties.getHost());
            sender.setPort(parseInt(emailConfigurationProperties.getPort()));
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

        Map<String, String> properties = emailConfigurationProperties.getProperties();
        props.put(MAIL_SMTP_AUTH_PROPERTY, Boolean.parseBoolean(properties.get(MAIL_SMTP_AUTH_PROPERTY)));
        props.put(MAIL_SMTP_STARTTLS_ENABLED_PROPERTY,
            Boolean.parseBoolean(properties.get(MAIL_SMTP_STARTTLS_ENABLED_PROPERTY)));

        return props;
    }

    private boolean isParametersExist() {
        return isNotBlank(emailConfigurationProperties.getHost()) &&
                   isNotBlank(emailConfigurationProperties.getPort()) &&
                   isAuthParametersExist();
    }

    private boolean isAuthParametersExist() {
        Map<String, String> properties = emailConfigurationProperties.getProperties();
        return MapUtils.isNotEmpty(properties)
                   && !Boolean.parseBoolean(properties.get(MAIL_SMTP_AUTH_PROPERTY))
                   || isNotBlank(emailConfigurationProperties.getUsername())
                   && isNotBlank(emailConfigurationProperties.getPassword());
    }
}
