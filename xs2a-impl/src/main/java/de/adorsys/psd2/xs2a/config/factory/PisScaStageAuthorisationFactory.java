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

package de.adorsys.psd2.xs2a.config.factory;

/**
 * This is specific factory intended to retrieve specific, stage-dependent SCA update authorisation services for PIS.
 * It is used ServiceLocatorFactoryBean for implementing a factory pattern.
 * See <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/ServiceLocatorFactoryBean.html">Spring docs</a> for details.
 */
public interface PisScaStageAuthorisationFactory extends ServiceFactory {
    String SERVICE_PREFIX = "PIS_";
    String CANCELLATION_SERVICE_PREFIX = "PIS_CANC_";
}
