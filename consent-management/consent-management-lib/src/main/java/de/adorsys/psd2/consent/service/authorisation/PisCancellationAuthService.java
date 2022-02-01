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
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PisCancellationAuthService extends PisAbstractAuthService {
    private final CommonPaymentService commonPaymentService;

    @Autowired
    public PisCancellationAuthService(PsuService psuService, AspspProfileService aspspProfileService,
                                      AuthorisationService authorisationService,
                                      ConfirmationExpirationService<PisCommonPaymentData> confirmationExpirationService,
                                      CommonPaymentService commonPaymentService) {
        super(psuService, aspspProfileService, authorisationService, confirmationExpirationService, commonPaymentService);
        this.commonPaymentService = commonPaymentService;
    }

    @Override
    public Optional<Authorisable> getNotFinalisedAuthorisationParent(String parentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1143
        Optional<PisCommonPaymentData> commonPaymentData = commonPaymentService.findByPaymentId(parentId)
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData());
        if (commonPaymentData.isEmpty()) {
            commonPaymentData = commonPaymentService.findOneByPaymentId(parentId);
        }

        return commonPaymentData
                   .filter(p -> p.getTransactionStatus().isNotFinalisedStatus())
                   .map(this::convertToCommonPayment);
    }

    @Override
    public Optional<Authorisable> getAuthorisationParent(String parentId) {
        return commonPaymentService.findOneByPaymentId(parentId)
                   .map(con -> con);
    }

    @Override
    AuthorisationType getAuthorisationType() {
        return AuthorisationType.PIS_CANCELLATION;
    }
}
