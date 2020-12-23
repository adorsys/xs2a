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
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.PATC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;

@Slf4j
@Service
public class PisAuthService extends PisAbstractAuthService {
    private final CommonPaymentService commonPaymentService;

    @Autowired
    public PisAuthService(PsuService psuService, AspspProfileService aspspProfileService,
                          AuthorisationService authorisationService,
                          ConfirmationExpirationService<PisCommonPaymentData> confirmationExpirationService,
                          CommonPaymentService commonPaymentService) {
        super(psuService, aspspProfileService, authorisationService, confirmationExpirationService, commonPaymentService);
        this.commonPaymentService = commonPaymentService;
    }

    @Override
    public Optional<Authorisable> getNotFinalisedAuthorisationParent(String parentId) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1143
        Optional<PisCommonPaymentData> commonPaymentData = commonPaymentService.findByPaymentIdAndPaymentDataTransactionStatusIn(parentId, Arrays.asList(RCVD, PATC))
                                                               .filter(CollectionUtils::isNotEmpty)
                                                               .map(list -> list.get(0).getPaymentData())
                                                               .map(commonPaymentService::checkAndUpdateOnConfirmationExpiration)
                                                               .filter(p -> EnumSet.of(RCVD, PATC).contains(p.getTransactionStatus()));

        if (commonPaymentData.isEmpty()) {
            commonPaymentData = commonPaymentService.findByPaymentIdAndTransactionStatusIn(parentId, Arrays.asList(RCVD, PATC))
                                    .map(commonPaymentService::checkAndUpdateOnConfirmationExpiration)
                                    .filter(p -> EnumSet.of(RCVD, PATC).contains(p.getTransactionStatus()));
        }

        return commonPaymentData.map(this::convertToCommonPayment);
    }

    @Override
    public Optional<Authorisable> getAuthorisationParent(String parentId) {
        return commonPaymentService.findOneByPaymentId(parentId)
                   .map(con -> con);
    }

    @Override
    AuthorisationType getAuthorisationType() {
        return AuthorisationType.PIS_CREATION;
    }
}
