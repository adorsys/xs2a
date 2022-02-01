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


package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisPaymentControllerTest {
    private static final String PAYMENT_ID = "33333-999999999";
    private static final String TPP_OK_REDIRECT_ORI = "TPP-Redirect-URI-cancel";
    private static final String TPP_NOK_REDIRECT_ORI = "TPP-Nok-Redirect-URI-cancel";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentService;
    @Mock
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;
    @InjectMocks
    private PisPaymentController pisPaymentController;

    @Captor
    private ArgumentCaptor<TppRedirectUri> tppRedirectUriCaptor;

    @Captor
    private ArgumentCaptor<String> cancellationInternalRequestIdCaptor;

    @Test
    void updatePaymentCancellationTppRedirectUri() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(eq(PAYMENT_ID), tppRedirectUriCaptor.capture()))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, TPP_OK_REDIRECT_ORI, TPP_NOK_REDIRECT_ORI);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(TPP_OK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getUri());
        assertEquals(TPP_NOK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getNokUri());
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_Fail() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(eq(PAYMENT_ID), tppRedirectUriCaptor.capture()))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, TPP_OK_REDIRECT_ORI, TPP_NOK_REDIRECT_ORI);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());


        assertEquals(TPP_OK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getUri());
        assertEquals(TPP_NOK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getNokUri());
    }

    @Test
    void updatePaymentCancellationInternalRequestId() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(eq(PAYMENT_ID), cancellationInternalRequestIdCaptor.capture()))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationInternalRequestId(PAYMENT_ID, INTERNAL_REQUEST_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(INTERNAL_REQUEST_ID, cancellationInternalRequestIdCaptor.getValue());
    }

    @Test
    void updatePaymentCancellationInternalRequestId_Fail() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(eq(PAYMENT_ID), cancellationInternalRequestIdCaptor.capture()))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationInternalRequestId(PAYMENT_ID, INTERNAL_REQUEST_ID);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals(INTERNAL_REQUEST_ID, cancellationInternalRequestIdCaptor.getValue());
    }
}
