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

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentConfirmationExpirationServiceTest {
    private static final String PAYMENT_ID = "some payment id";

    @InjectMocks
    private PisCommonPaymentConfirmationExpirationServiceImpl service;

    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AspspSettings aspspSettings;

    @Test
    void checkAndUpdateOnConfirmationExpiration_confirmationIsExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().minusSeconds(100));
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);

        AuthorisationEntity pisAuthorisation = new AuthorisationEntity();
        pisAuthorisation.setScaStatus(ScaStatus.RECEIVED);
        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(pisAuthorisation));

        when(aspspProfileService.getAspspSettings(pisAuthorisation.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(1000L));

        service.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(TransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.FAILED, pisAuthorisation.getScaStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration_confirmationIsNotExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().plusHours(1));

        AuthorisationEntity pisAuthorisation = new AuthorisationEntity();
        pisAuthorisation.setScaStatus(ScaStatus.RECEIVED);

        when(aspspProfileService.getAspspSettings(pisAuthorisation.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        service.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(TransactionStatus.RCVD, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.RECEIVED, pisAuthorisation.getScaStatus());
    }

    @Test
    void isPaymentDataOnConfirmationExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().minusHours(1));

        when(aspspProfileService.getAspspSettings(pisCommonPaymentData.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isConfirmationExpired(pisCommonPaymentData);

        assertTrue(actual);
    }

    @Test
    void isPaymentDataOnConfirmationNotExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().plusHours(1));

        when(aspspProfileService.getAspspSettings(pisCommonPaymentData.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isConfirmationExpired(pisCommonPaymentData);

        assertFalse(actual);
    }

    @Test
    void isConfirmationExpired_pisCommonPaymentDataIsNull() {
        assertFalse(service.isConfirmationExpired(null));
    }

    @Test
    void updateOnConfirmationExpiration() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);

        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);

        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(pisAuthorization));

        service.updateOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(TransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    void updatePaymentDataListOnConfirmationExpiration() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);

        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setScaStatus(ScaStatus.RECEIVED);

        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(pisAuthorization));

        service.updatePaymentDataListOnConfirmationExpiration(Collections.singletonList(pisCommonPaymentData));

        assertEquals(TransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(ScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).saveAll(Collections.singletonList(pisCommonPaymentData));
    }

    @NotNull
    private PisAspspProfileSetting getPisAspspProfileSetting(long notConfirmedPaymentExpirationTimeMs) {
        return new PisAspspProfileSetting(new HashMap<>(), 0, notConfirmedPaymentExpirationTimeMs,
                                          true, null, "", null, false);
    }
}
