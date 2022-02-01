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


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.config.PisPaymentRemoteUrls;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentAfterSpiServiceRemoteTest {
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String URL = "some url";

    @InjectMocks
    private UpdatePaymentAfterSpiServiceRemote updatePaymentAfterSpiServiceRemote;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private PisPaymentRemoteUrls pisPaymentRemoteUrls;

    @Test
    void updatePaymentStatus() {
        when(pisPaymentRemoteUrls.updatePaymentStatus()).thenReturn(URL);
        when(consentRestTemplate.exchange(eq(URL), eq(HttpMethod.PUT), isNull(), eq(Void.class), eq(ENCRYPTED_PAYMENT_ID), eq(TransactionStatus.ACSP.name())))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertTrue(updatePaymentAfterSpiServiceRemote.updatePaymentStatus(ENCRYPTED_PAYMENT_ID, TransactionStatus.ACSP).getPayload());
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_success() {
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");

        when(pisPaymentRemoteUrls.updatePaymentCancellationRedirectURIs()).thenReturn(URL);
        when(consentRestTemplate.exchange(eq(URL), eq(HttpMethod.PUT), httpEntityCaptor.capture(), eq(Void.class), eq(ENCRYPTED_PAYMENT_ID))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertTrue(updatePaymentAfterSpiServiceRemote.updatePaymentCancellationTppRedirectUri(ENCRYPTED_PAYMENT_ID, tppRedirectUri).getPayload());

        assertEquals(2, httpEntityCaptor.getValue().getHeaders().size());
        assertEquals("ok_url", httpEntityCaptor.getValue().getHeaders().get("tpp-redirect-uri").get(0));
        assertEquals("nok_url", httpEntityCaptor.getValue().getHeaders().get("tpp-nok-redirect-uri").get(0));
    }
}
