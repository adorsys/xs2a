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

package de.adorsys.aspsp.xs2a.config.factory;

import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceLocatorFactoryConfiguration {

    @Bean
    public ServiceLocatorFactoryBean readPaymentFactory() {
        ServiceLocatorFactoryBean serviceLocatorFactoryBean = new ServiceLocatorFactoryBean();
        serviceLocatorFactoryBean.setServiceLocatorInterface(ReadPaymentFactory.class);
        return serviceLocatorFactoryBean;
    }

    /**
     * Configuration of ServiceLocatorFactoryBean bean to be used as a factory for PIS SCA authorisation stages through Service locator interface.
     * See <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/ServiceLocatorFactoryBean.html">Spring docs</a> for details.
     *
     * @return ServiceLocatorFactoryBean
     */
    @Bean
    public ServiceLocatorFactoryBean pisScaUpdateAuthorisationFactory() {
        ServiceLocatorFactoryBean serviceLocatorFactoryBean = new ServiceLocatorFactoryBean();
        serviceLocatorFactoryBean.setServiceLocatorInterface(PisScaStageAuthorisationFactory.class);
        return serviceLocatorFactoryBean;
    }

    /**
     * Configuration of ServiceLocatorFactoryBean bean to be used as a factory for AIS SCA authorisation stages through Service locator interface.
     * See <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/ServiceLocatorFactoryBean.html">Spring docs</a> for details.
     *
     * @return ServiceLocatorFactoryBean
     */
    @Bean
    public ServiceLocatorFactoryBean aisScaUpdateAuthorisationFactory() {
        ServiceLocatorFactoryBean serviceLocatorFactoryBean = new ServiceLocatorFactoryBean();
        serviceLocatorFactoryBean.setServiceLocatorInterface(AisScaStageAuthorisationFactory.class);
        return serviceLocatorFactoryBean;
    }
}
