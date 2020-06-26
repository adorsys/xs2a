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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.link.PaymentAuthorisationAspectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreatePisAuthorizationAspectTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final Xs2aCreatePisAuthorisationRequest REQUEST =
        new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, EMPTY_PSU_DATA, PAYMENT_PRODUCT, SINGLE, null);

    @InjectMocks
    private CreatePisAuthorizationAspect aspect;

    @Mock
    private PaymentAuthorisationAspectService paymentAuthorisationAspectService;
    @Mock
    private AuthorisationResponse authorisationResponse;

    @Test
    void createPisAuthorizationAspect() {
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                            .body(authorisationResponse)
                                            .build();
        aspect.createPisAuthorizationAspect(responseObject, REQUEST);
        verify(paymentAuthorisationAspectService).createPisAuthorizationAspect(responseObject, REQUEST);
    }
}
