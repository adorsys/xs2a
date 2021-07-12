/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public abstract class AbstractPisScaAuthorisationService implements PisScaAuthorisationService {
    private final PisAuthorisationService authorisationService;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;

    @Override
    public Optional<Xs2aCreatePisAuthorisationResponse> createCommonPaymentAuthorisation(Xs2aCreateAuthorisationRequest request, PaymentType paymentType) {
        return pisCommonPaymentMapper.mapToXsa2CreatePisAuthorisationResponse(authorisationService.createPisAuthorisation(request), paymentType);
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentPsuData(PaymentAuthorisationParameters request) {
        return authorisationService.updatePisAuthorisation(request, getScaApproachServiceType());
    }

    @Override
    public void updateAuthorisation(CommonAuthorisationParameters request, AuthorisationProcessorResponse response) {
        authorisationService.updateAuthorisation(request, response);
    }

    @Override
    public void updateCancellationAuthorisation(CommonAuthorisationParameters request, AuthorisationProcessorResponse response) {
        authorisationService.updateCancellationAuthorisation(request, response);
    }

    @Override
    public Optional<Xs2aCreatePisCancellationAuthorisationResponse> createCommonPaymentCancellationAuthorisation(Xs2aCreateAuthorisationRequest createAuthorisationRequest, PaymentType paymentType) {
        return pisCommonPaymentMapper.mapToXs2aCreatePisCancellationAuthorisationResponse(authorisationService.createPisAuthorisationCancellation(createAuthorisationRequest), paymentType);
    }

    @Override
    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> getCancellationAuthorisationSubResources(String paymentId) {
        return authorisationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(Xs2aPaymentCancellationAuthorisationSubResource::new);
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updateCommonPaymentCancellationPsuData(PaymentAuthorisationParameters request) {
        return authorisationService.updatePisCancellationAuthorisation(request, getScaApproachServiceType());
    }

    @Override
    public Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String paymentId) {
        return authorisationService.getAuthorisationSubResources(paymentId)
                   .map(Xs2aAuthorisationSubResources::new);
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        return authorisationService.getAuthorisationScaStatus(paymentId, authorisationId);
    }

    @Override
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String authorisationId) {
        return authorisationService.getCancellationAuthorisationScaStatus(paymentId, authorisationId);
    }
}
