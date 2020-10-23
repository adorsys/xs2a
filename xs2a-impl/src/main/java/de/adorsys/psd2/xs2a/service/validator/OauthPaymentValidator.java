/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class OauthPaymentValidator extends OauthValidator<PisCommonPaymentResponse> {
    private static final MessageError MESSAGE_ERROR = new MessageError(ErrorType.PIS_403, TppMessageInformation.of(MessageErrorCode.FORBIDDEN));

    private static final Set<TransactionStatus> NOT_ALLOWED_STATUSES_FOR_GET_REQUESTS =
        EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PDNG, TransactionStatus.PATC);

    public OauthPaymentValidator(RequestProviderService requestProviderService,
                                 AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                 ScaApproachResolver scaApproachResolver) {
        super(requestProviderService, aspspProfileServiceWrapper, scaApproachResolver);
    }

    @Override
    protected boolean checkObjectForTokenAbsence(PisCommonPaymentResponse pisCommonPayment) {
        return !NOT_ALLOWED_STATUSES_FOR_GET_REQUESTS.contains(pisCommonPayment.getTransactionStatus());
    }

    @Override
    protected MessageError getMessageError() {
        return MESSAGE_ERROR;
    }
}
