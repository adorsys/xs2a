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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;

import java.util.List;
import java.util.stream.Collectors;

public abstract class PisAbstractAuthService extends CmsAuthorisationService<PisCommonPaymentData> {
    private final CommonPaymentService commonPaymentService;

    protected PisAbstractAuthService(PsuService psuService, AspspProfileService aspspProfileService,
                                  AuthorisationService authorisationService,
                                  ConfirmationExpirationService<PisCommonPaymentData> confirmationExpirationService,
                                  CommonPaymentService commonPaymentService) {
        super(psuService, aspspProfileService, authorisationService, confirmationExpirationService);
        this.commonPaymentService = commonPaymentService;
    }

    @Override
    protected PisCommonPaymentData castToParent(Authorisable authorisable) {
        return (PisCommonPaymentData) authorisable;
    }

    protected PisCommonPaymentData convertToCommonPayment(PisCommonPaymentData pisCommonPaymentData) {
        if (pisCommonPaymentData == null || pisCommonPaymentData.getPayment() != null) {
            return pisCommonPaymentData;
        }

        List<PisPayment> pisPayments = pisCommonPaymentData.getPayments().stream()
                                           .map(commonPaymentService::mapToPisPayment)
                                           .collect(Collectors.toList());
        byte[] paymentData = commonPaymentService.buildPaymentData(pisPayments, pisCommonPaymentData.getPaymentType());
        if (paymentData != null) {
            pisCommonPaymentData.setPayment(paymentData);
            return commonPaymentService.save(pisCommonPaymentData);
        }

        return pisCommonPaymentData;
    }
}
