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

package de.adorsys.psd2.xs2a.domain.consent.pis;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Xs2aUpdatePisCommonPaymentPsuDataResponse extends AuthorisationProcessorResponse implements CancellationAuthorisationResponse {
    private Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo;

    public Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, String paymentId, String authorisationId, PsuIdData psuData) {
        this(scaStatus, paymentId, authorisationId, psuData, null);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, String paymentId, String authorisationId,
                                                     PsuIdData psuData, Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo) {
        this.scaStatus = scaStatus;
        this.paymentId = paymentId;
        this.authorisationId = authorisationId;
        this.psuData = psuData;
        this.xs2aCurrencyConversionInfo = xs2aCurrencyConversionInfo;
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus scaStatus, ErrorHolder errorHolder, String paymentId, String authorisationId, PsuIdData psuData) {
        this(scaStatus, paymentId, authorisationId, psuData);
        this.errorHolder = errorHolder;
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse(ErrorHolder errorHolder, String paymentId, String authorisationId, PsuIdData psuData) {
        this(ScaStatus.FAILED, paymentId, authorisationId, psuData);
        this.errorHolder = errorHolder;
    }

    @NotNull
    @Override
    public String getAuthorisationId() {
        return authorisationId;
    }

    @NotNull
    @Override
    public AuthorisationResponseType getAuthorisationResponseType() {
        return AuthorisationResponseType.UPDATE;
    }
}

