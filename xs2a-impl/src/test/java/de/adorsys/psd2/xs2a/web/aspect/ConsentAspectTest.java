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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.link.ConsentAspectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsentAspectTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";

    @InjectMocks
    private ConsentAspect aspect;

    @Mock
    private ConsentAspectService consentAspectService;
    @Mock
    private CreateConsentResponse createConsentResponse;
    @Mock
    private CreateConsentAuthorizationResponse createConsentAuthorisationResponse;
    @Mock
    private UpdateConsentPsuDataResponse updateConsentPsuDataResponse;
    @Mock
    private Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse;
    @Mock
    private AuthorisationResponse authorisationResponse;

    @Test
    void invokeCreateAccountConsentAspect() {
        ResponseObject<CreateConsentResponse> responseObject = ResponseObject.<CreateConsentResponse>builder()
                                                                   .body(createConsentResponse)
                                                                   .build();
        aspect.invokeCreateAccountConsentAspect(responseObject, new CreateConsentReq(), null, true);
        verify(consentAspectService).invokeCreateAccountConsentAspect(responseObject, true);
    }

    @Test
    void invokeCreateConsentPsuDataAspect() {
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(createConsentAuthorisationResponse)
                                                                   .build();
        aspect.invokeCreateConsentPsuDataAspect(responseObject, null, CONSENT_ID, "password");
        verify(consentAspectService).invokeCreateConsentPsuDataAspect(responseObject);
    }

    @Test
    void invokeUpdateConsentPsuDataAspect() {
        ResponseObject<UpdateConsentPsuDataResponse> responseObject = ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                                                          .body(updateConsentPsuDataResponse)
                                                                          .build();
        aspect.invokeUpdateConsentPsuDataAspect(responseObject, new ConsentAuthorisationsParameters());
        verify(consentAspectService).invokeUpdateConsentPsuDataAspect(responseObject);
    }

    @Test
    void createPiisConsentWithResponse() {
        ResponseObject<Xs2aConfirmationOfFundsResponse> responseObject = ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                                                                   .body(xs2aConfirmationOfFundsResponse)
                                                                   .build();
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        aspect.createPiisConsentWithResponse(responseObject, request, null, true);
        verify(consentAspectService).createPiisConsentWithResponse(responseObject, true);
    }

    @Test
    void createPiisAuthorisationAspect() {
        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                             .body(authorisationResponse)
                                                                             .build();
        aspect.createPiisAuthorisationAspect(responseObject, null, null, null);
        verify(consentAspectService).invokeCreatePiisAuthorisationAspect(responseObject);
    }
}
