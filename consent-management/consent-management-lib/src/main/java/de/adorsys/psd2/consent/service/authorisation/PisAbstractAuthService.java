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

    public PisAbstractAuthService(PsuService psuService, AspspProfileService aspspProfileService,
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
