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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisPaymentControllerTest {
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
    public void updatePaymentCancellationTppRedirectUri() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(eq(PAYMENT_ID), tppRedirectUriCaptor.capture())).thenReturn(true);

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, TPP_OK_REDIRECT_ORI, TPP_NOK_REDIRECT_ORI);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assert.assertEquals(TPP_OK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getUri());
        Assert.assertEquals(TPP_NOK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getNokUri());
    }

    @Test
    public void updatePaymentCancellationTppRedirectUri_Fail() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(eq(PAYMENT_ID), tppRedirectUriCaptor.capture())).thenReturn(false);

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, TPP_OK_REDIRECT_ORI, TPP_NOK_REDIRECT_ORI);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Assert.assertEquals(TPP_OK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getUri());
        Assert.assertEquals(TPP_NOK_REDIRECT_ORI, tppRedirectUriCaptor.getValue().getNokUri());
    }

    @Test
    public void updatePaymentCancellationInternalRequestId() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(eq(PAYMENT_ID), cancellationInternalRequestIdCaptor.capture())).thenReturn(true);

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationInternalRequestId(PAYMENT_ID, INTERNAL_REQUEST_ID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assert.assertEquals(INTERNAL_REQUEST_ID, cancellationInternalRequestIdCaptor.getValue());
    }

    @Test
    public void updatePaymentCancellationInternalRequestId_Fail() {
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(eq(PAYMENT_ID), cancellationInternalRequestIdCaptor.capture())).thenReturn(false);

        ResponseEntity<Void> response = pisPaymentController.updatePaymentCancellationInternalRequestId(PAYMENT_ID, INTERNAL_REQUEST_ID);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Assert.assertEquals(INTERNAL_REQUEST_ID, cancellationInternalRequestIdCaptor.getValue());
    }
}
