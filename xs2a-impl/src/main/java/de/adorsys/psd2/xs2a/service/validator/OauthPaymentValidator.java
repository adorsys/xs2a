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
