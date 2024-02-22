/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.link.PaymentAuthorisationAspectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdatePisPsuDataAspectTest {

    @InjectMocks
    private UpdatePisPsuDataAspect aspect;

    @Mock
    private PaymentAuthorisationAspectService paymentAuthorisationAspectService;

    @Test
    void updatePisAuthorizationAspect() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setPaymentService(SINGLE);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .body(new Xs2aUpdatePisCommonPaymentPsuDataResponse())
                                                                                       .build();
        aspect.updatePisAuthorizationAspect(responseObject, request);
        verify(paymentAuthorisationAspectService).updatePisAuthorizationAspect(responseObject, request);
    }
}
