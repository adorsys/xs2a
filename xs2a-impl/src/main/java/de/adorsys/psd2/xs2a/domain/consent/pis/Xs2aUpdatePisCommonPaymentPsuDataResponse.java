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

