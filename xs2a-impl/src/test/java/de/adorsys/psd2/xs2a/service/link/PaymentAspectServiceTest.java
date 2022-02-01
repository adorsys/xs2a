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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.PaymentInitiationLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAspectServiceTest {
    @InjectMocks
    private PaymentAspectService service;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private PaymentInitiationResponse paymentInitiationResponse;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PaymentInitiationParameters requestParameters;
    @Mock
    private RequestProviderService requestProviderService;

    private ResponseObject responseObject;

    @Test
    void createPaymentAspect_success() {
        AspspSettings aspspSettings = new JsonReader().getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().isForceXs2aBaseLinksUrl());
        when(aspspProfileServiceWrapper.getXs2aBaseLinksUrl()).thenReturn(aspspSettings.getCommon().getXs2aBaseLinksUrl());

        when(requestParameters.isTppExplicitAuthorisationPreferred()).thenReturn(true);
        when(paymentInitiationResponse.isMultilevelScaRequired()).thenReturn(true);
        when(authorisationMethodDecider.isExplicitMethod(true, true)).thenReturn(true);
        when(paymentInitiationResponse.getTransactionStatus()).thenReturn(TransactionStatus.RJCT);

        responseObject = ResponseObject.<PaymentInitiationResponse>builder()
                             .body(paymentInitiationResponse)
                             .build();
        ResponseObject actualResponse = service.createPaymentAspect(responseObject, requestParameters);

        verify(paymentInitiationResponse, times(1)).setLinks(any(PaymentInitiationLinks.class));

        assertFalse(actualResponse.hasError());
    }

    @Test
    void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {

        // When
        responseObject = ResponseObject.builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject actualResponse = service.createPaymentAspect(responseObject, requestParameters);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CONSENT_UNKNOWN_400, actualResponse.getError().getTppMessage().getMessageErrorCode());
    }
}
