/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
