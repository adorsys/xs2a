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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentConfirmationExpirationServiceTest {

    @InjectMocks
    private PisCommonPaymentConfirmationExpirationService service;

    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AspspSettings aspspSettings;

    @Test
    public void checkAndUpdatePaymentDataOnConfirmationExpiration_confirmationIsNotExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().minusSeconds(100));
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisCommonPaymentData.setAuthorizations(Collections.singletonList(pisAuthorization));

        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(1000L));

        service.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(TransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    public void checkAndUpdatePaymentDataOnConfirmationExpiration_confirmationIsExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().plusHours(1));
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisCommonPaymentData.setAuthorizations(Collections.singletonList(pisAuthorization));

        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        service.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(TransactionStatus.RCVD, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.RECEIVED, pisAuthorization.getScaStatus());
    }

    @Test
    public void isPaymentDataOnConfirmationExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().minusHours(1));

        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isPaymentDataOnConfirmationExpired(pisCommonPaymentData);

        assertTrue(actual);
    }

    @Test
    public void isPaymentDataOnConfirmationNotExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().plusHours(1));

        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isPaymentDataOnConfirmationExpired(pisCommonPaymentData);

        assertFalse(actual);
    }

    @Test
    public void isPaymentDataOnConfirmationExpired_pisCommonPaymentDataIsNull() {
        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isPaymentDataOnConfirmationExpired(null);
        assertFalse(actual);
    }

    @Test
    public void updatePaymentDataOnConfirmationExpiration() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisCommonPaymentData.setAuthorizations(Collections.singletonList(pisAuthorization));

        service.updatePaymentDataOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(TransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    public void updatePaymentDataListOnConfirmationExpiration() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);
        pisCommonPaymentData.setAuthorizations(Collections.singletonList(pisAuthorization));

        service.updatePaymentDataListOnConfirmationExpiration(Collections.singletonList(pisCommonPaymentData));

        assertEquals(TransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).saveAll(Collections.singletonList(pisCommonPaymentData));
    }

    @NotNull
    private PisAspspProfileSetting getPisAspspProfileSetting(long notConfirmedPaymentExpirationTimeMs) {
        return new PisAspspProfileSetting(new HashMap<>(), 0, notConfirmedPaymentExpirationTimeMs,
                                          true, null, "", null);
    }
}
