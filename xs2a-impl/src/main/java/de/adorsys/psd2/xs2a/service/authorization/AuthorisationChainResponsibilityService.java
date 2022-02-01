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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.service.authorization.processor.*;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthorisationChainResponsibilityService {

    private AuthorisationProcessor receivedAuthorisationProcessor;
    private AuthorisationProcessorServiceProvider provider;

    @Autowired
    public AuthorisationChainResponsibilityService(AuthorisationProcessorServiceProvider provider) {
        this.provider = provider;
        initAuthorisationChains();
    }

    public AuthorisationProcessorResponse apply(AuthorisationProcessorRequest request) {
        return receivedAuthorisationProcessor.apply(request);
    }

    private void initAuthorisationChains() {
        receivedAuthorisationProcessor = new ReceivedAuthorisationProcessor(provider);
        AuthorisationProcessor psuIdentifiedAuthorisationProcessor = new PsuIdentifiedAuthorisationProcessor(provider);
        AuthorisationProcessor psuAuthenticatedAuthorisationProcessor = new PsuAuthenticatedAuthorisationProcessor(provider);
        AuthorisationProcessor scaMethodSelectedAuthorisationProcessor = new ScaMethodSelectedAuthorisationProcessor(provider);
        AuthorisationProcessor startedAuthorisationProcessor = new StartedAuthorisationProcessor(provider);
        AuthorisationProcessor finalisedAuthorisationProcessor = new FinalisedAuthorisationProcessor(provider);
        AuthorisationProcessor failedAuthorisationProcessor = new FailedAuthorisationProcessor(provider);
        AuthorisationProcessor exemptedAuthorisationProcessor = new ExemptedAuthorisationProcessor(provider);

        receivedAuthorisationProcessor.setNext(psuIdentifiedAuthorisationProcessor);
        psuIdentifiedAuthorisationProcessor.setNext(psuAuthenticatedAuthorisationProcessor);
        psuAuthenticatedAuthorisationProcessor.setNext(scaMethodSelectedAuthorisationProcessor);
        scaMethodSelectedAuthorisationProcessor.setNext(startedAuthorisationProcessor);
        startedAuthorisationProcessor.setNext(finalisedAuthorisationProcessor);
        finalisedAuthorisationProcessor.setNext(failedAuthorisationProcessor);
        failedAuthorisationProcessor.setNext(exemptedAuthorisationProcessor);
    }
}
