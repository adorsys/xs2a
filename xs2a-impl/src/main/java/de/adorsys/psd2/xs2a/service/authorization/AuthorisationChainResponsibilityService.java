/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
