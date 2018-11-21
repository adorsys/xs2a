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

package de.adorsys.psd2.xs2a.config;

import de.adorsys.psd2.validator.certificate.util.TppRole;
import de.adorsys.psd2.xs2a.web.filter.QwacCertificateFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    QwacCertificateFilter qwacCertificateFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs", "/webjars/**", "/swagger.json", "/psd2-api-*.yaml")
            .permitAll()
            .antMatchers("/api/v1/accounts/**", "/v1/accounts/**")
            .hasRole(TppRole.AISP.name())
            .antMatchers("/api/v1/consents/**", "/v1/consents/**")
            .hasRole(TppRole.AISP.name())
            .antMatchers("/api/v1/funds-confirmations/**", "/v1/funds-confirmations/**")
            .hasRole(TppRole.PIISP.name())
            .antMatchers("/api/v1/bulk-payments/**", "/v1/bulk-payments/**")
            .hasRole(TppRole.PISP.name())
            .antMatchers("/api/v1/payments/**", "/v1/payments/**")
            .hasRole(TppRole.PISP.name())
            .antMatchers("/api/v1/periodic-payments/**", "/v1/periodic-payments/**")
            .hasRole(TppRole.PISP.name())
            .anyRequest()
            .authenticated()
            .and().addFilterBefore(qwacCertificateFilter, BasicAuthenticationFilter.class);
    }
}
