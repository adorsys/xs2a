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

package de.adorsys.psd2.xs2a.service.authorization.processor;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AuthorisationProcessorServiceProvider {
    private ApplicationContext applicationContext;

    public AuthorisationProcessorServiceProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public AuthorisationProcessorService getProcessorService(AuthorisationProcessorRequest request) {
        if (request.getServiceType() == ServiceType.AIS) {
            return applicationContext.getBean(AisAuthorisationProcessorServiceImpl.class);
        } else if (request.getServiceType() == ServiceType.PIS &&
                       request.getAuthorisation().getAuthorisationType() == AuthorisationType.PIS_CREATION) {
            return applicationContext.getBean(PisAuthorisationProcessorServiceImpl.class);
        } else if (request.getServiceType() == ServiceType.PIS &&
                       request.getAuthorisation().getAuthorisationType() == AuthorisationType.PIS_CANCELLATION) {
            return applicationContext.getBean(PisCancellationAuthorisationProcessorServiceImpl.class);
        } else if (request.getServiceType() == ServiceType.PIIS) {
            return applicationContext.getBean(PiisAuthorisationProcessorServiceImpl.class);
        }
        throw new IllegalArgumentException("Authorisation processor service is unknown: " + request);
    }
}
