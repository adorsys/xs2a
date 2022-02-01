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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.pis.PaymentTypeAndInfoProvider;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class GetPaymentInitiationAuthorisationScaStatusPO implements PaymentTypeAndInfoProvider {
    @NotNull
    private PisCommonPaymentResponse pisCommonPaymentResponse;

    @NotNull
    private String authorisationId;
    private PaymentType paymentType;
    private String paymentProduct;

    @Override
    public TppInfo getTppInfo() {
        return pisCommonPaymentResponse.getTppInfo();
    }
}
