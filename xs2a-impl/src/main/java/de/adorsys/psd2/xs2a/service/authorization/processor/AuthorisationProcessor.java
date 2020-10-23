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

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AuthorisationProcessorService;

public abstract class AuthorisationProcessor {
    private AuthorisationProcessor nextProcessor;
    private AuthorisationProcessorServiceProvider provider;

    public AuthorisationProcessor(AuthorisationProcessorServiceProvider provider) {
        this.provider = provider;
    }

    public void setNext(AuthorisationProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    public abstract ScaStatus getScaStatus();

    protected abstract AuthorisationProcessorResponse execute(AuthorisationProcessorRequest request,
                                                              AuthorisationProcessorService processorService);

    public AuthorisationProcessorResponse apply(AuthorisationProcessorRequest request) {
        AuthorisationProcessorResponse processorResponse = process(request);

        //update authorisation
        getProcessorService(request).updateAuthorisation(request, processorResponse);
        return processorResponse;
    }

    AuthorisationProcessorResponse process(AuthorisationProcessorRequest request) {
        if (getScaStatus() == request.getScaStatus()) {
            AuthorisationProcessorService processorService = getProcessorService(request);
            return execute(request, processorService);
        } else {
            if (hasNext()) {
                return nextProcessor.process(request);
            }
        }
        return null;
    }

    AuthorisationProcessorService getProcessorService(AuthorisationProcessorRequest request) {
        return provider.getProcessorService(request);
    }

    private boolean hasNext() {
        return nextProcessor != null;
    }
}
