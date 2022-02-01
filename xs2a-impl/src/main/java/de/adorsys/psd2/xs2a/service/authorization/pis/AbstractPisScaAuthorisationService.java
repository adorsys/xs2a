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
