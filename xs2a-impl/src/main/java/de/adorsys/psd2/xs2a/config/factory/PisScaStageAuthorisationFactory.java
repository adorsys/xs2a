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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

/**
 * This is specific factory intended to retrieve specific, stage-dependent SCA update authorisation services for PIS.
 * It is used ServiceLocatorFactoryBean for implementing a factory pattern.
 * See <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/ServiceLocatorFactoryBean.html">Spring docs</a> for details.
 */
public interface PisScaStageAuthorisationFactory extends ServiceFactory {
    String CANCELLATION_SERVICE_STATUS_PATTERN = "PIS_CANCELLATION_%s_%s";
    String SERVICE_STATUS_PATTERN = "PIS_%s_%s";

    static String getCancellationServiceName(ScaApproach scaApproach, ScaStatus scaStatus) {
        return String.format(CANCELLATION_SERVICE_STATUS_PATTERN, scaApproach, scaStatus);
    }

    static String getServiceName(ScaApproach scaApproach, ScaStatus scaStatus) {
        return String.format(SERVICE_STATUS_PATTERN, scaApproach, scaStatus);
    }
}
