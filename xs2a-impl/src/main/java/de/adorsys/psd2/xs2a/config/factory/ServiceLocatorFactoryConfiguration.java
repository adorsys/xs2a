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

package de.adorsys.psd2.xs2a.config.factory;

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
     * Configuration of ServiceLocatorFactoryBean bean to be used as a factory for read payment status services for different payment types through Service locator interface.
     * See <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/ServiceLocatorFactoryBean.html">Spring docs</a> for details.
     *
     * @return ServiceLocatorFactoryBean
     */
    @Bean
    public ServiceLocatorFactoryBean readPaymentStatusFactory() {
        ServiceLocatorFactoryBean serviceLocatorFactoryBean = new ServiceLocatorFactoryBean();
        serviceLocatorFactoryBean.setServiceLocatorInterface(ReadPaymentStatusFactory.class);
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
