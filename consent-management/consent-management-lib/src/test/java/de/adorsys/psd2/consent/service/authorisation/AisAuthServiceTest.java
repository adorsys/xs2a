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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisAuthServiceTest {
    private static final String PARENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";

    @InjectMocks
    private AisAuthService service;

    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    private ConsentEntity consentEntity;

    @BeforeEach
    void init() {
        consentEntity = new ConsentEntity();
    }

    @Test
    void getInteractableAuthorisationParent_statusIsNotFinalised() {
        // Given
        consentEntity.setConsentStatus(ConsentStatus.VALID);

        when(consentJpaRepository.findByExternalId(PARENT_ID))
            .thenReturn(Optional.of(consentEntity));

        // When
        assertEquals(Optional.of(consentEntity), service.getNotFinalisedAuthorisationParent(PARENT_ID));

        // Then
        verify(consentJpaRepository, times(1)).findByExternalId(PARENT_ID);
    }

    @Test
    void getInteractableAuthorisationParent_statusIsFinalised() {
        // Given
        consentEntity.setConsentStatus(ConsentStatus.EXPIRED);

        when(consentJpaRepository.findByExternalId(PARENT_ID))
            .thenReturn(Optional.of(consentEntity));

        // When
        assertEquals(Optional.empty(), service.getNotFinalisedAuthorisationParent(PARENT_ID));

        // Then
        verify(consentJpaRepository, times(1)).findByExternalId(PARENT_ID);
    }

    @Test
    void getAuthorisationParent() {
        // Given
        when(consentJpaRepository.findByExternalId(PARENT_ID))
            .thenReturn(Optional.of(consentEntity));

        // When
        assertEquals(Optional.of(consentEntity), service.getAuthorisationParent(PARENT_ID));

        // Then
        verify(consentJpaRepository, times(1)).findByExternalId(PARENT_ID);
    }

    @Test
    void updateAuthorisable() {
        // When
        service.updateAuthorisable(consentEntity);

        // Then
        verify(consentJpaRepository, times(1)).save(consentEntity);
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration() {
        // Given
        ConsentEntity updatedEntity = new ConsentEntity();
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consentEntity))
            .thenReturn(updatedEntity);

        // When
        Authorisable response = service.checkAndUpdateOnConfirmationExpiration(consentEntity);

        // Then
        assertEquals(updatedEntity, response);
        verify(aisConsentConfirmationExpirationService).checkAndUpdateOnConfirmationExpiration(consentEntity);
    }

    @Test
    void isConfirmationExpired() {
        // Given
        when(aisConsentConfirmationExpirationService.isConfirmationExpired(consentEntity))
            .thenReturn(true);

        // When
        boolean response = service.isConfirmationExpired(consentEntity);

        // Then
        assertTrue(response);
        verify(aisConsentConfirmationExpirationService).isConfirmationExpired(consentEntity);
    }

    @Test
    void updateOnConfirmationExpiration() {
        // Given
        ConsentEntity updatedEntity = new ConsentEntity();
        when(aisConsentConfirmationExpirationService.updateOnConfirmationExpiration(consentEntity))
            .thenReturn(updatedEntity);

        // When
        Authorisable response = service.updateOnConfirmationExpiration(consentEntity);

        // Then
        assertEquals(updatedEntity, response);
        verify(aisConsentConfirmationExpirationService).updateOnConfirmationExpiration(consentEntity);
    }

    @Test
    void getAuthorisationType() {
        assertEquals(AuthorisationType.CONSENT, service.getAuthorisationType());
    }
}
