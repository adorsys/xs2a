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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationClosingServiceTest {
    private static final String PSU_ID = "psu-id-1";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final PsuIdData EMPTY_PSU_ID_DATA = new PsuIdData(null, null, null, null, null);
    private static final PsuData PSU_DATA = new PsuData(PSU_ID, null, null, null, null);
    private static final PsuData WRONG_PSU_DATA = new PsuData("wrong psu-id", null, null, null, null);
    private static final String PARENT_ID = "parent id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String PREVIOUS_AUTHORISATION_ID = "previous authorisation id";
    private static final AuthorisationType AUTHORISATION_TYPE = AuthorisationType.AIS;
    private static final AuthorisationType WRONG_AUTHORISATION_TYPE = AuthorisationType.PIS_CREATION;

    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private AuthServiceResolver authServiceResolver;
    @Mock
    private AuthService authService;
    @Mock
    private AuthorisationRepository authorisationRepository;

    @InjectMocks
    private AuthorisationClosingService authorisationClosingService;

    @Test
    void closePreviousAuthorisationsByParent() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity previousAisAuthorisation = new AuthorisationEntity();
        previousAisAuthorisation.setAuthorisationType(AUTHORISATION_TYPE);
        previousAisAuthorisation.setPsuData(PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID)).thenReturn(Collections.singletonList(previousAisAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        ArgumentCaptor<AuthorisationEntity> authorisationsCaptor = ArgumentCaptor.forClass(AuthorisationEntity.class);

        // When
        authorisationClosingService.closePreviousAuthorisationsByParent(PARENT_ID, AUTHORISATION_TYPE, PSU_ID_DATA);

        // Then
        verify(authorisationRepository).save(authorisationsCaptor.capture());

        AuthorisationEntity capturedAuthorisation = authorisationsCaptor.getValue();
        assertEquals(ScaStatus.FAILED, capturedAuthorisation.getScaStatus());
    }

    @Test
    void closePreviousAuthorisationsByParent_noAuthorisations() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);
        when(authService.getAuthorisationsByParentId(PARENT_ID)).thenReturn(Collections.emptyList());
        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByParent(PARENT_ID, AUTHORISATION_TYPE, PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByParent_nullPsu() {
        // When
        authorisationClosingService.closePreviousAuthorisationsByParent(PARENT_ID, AUTHORISATION_TYPE, null);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByParent_emptyPsu() {
        // When
        authorisationClosingService.closePreviousAuthorisationsByParent(PARENT_ID, AUTHORISATION_TYPE, EMPTY_PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByParent_shouldSkipWrongType() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity previousAuthorisation = new AuthorisationEntity();
        previousAuthorisation.setAuthorisationType(WRONG_AUTHORISATION_TYPE);
        previousAuthorisation.setPsuData(PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID)).thenReturn(Collections.singletonList(previousAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByParent(PARENT_ID, AUTHORISATION_TYPE, PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByParent_shouldSkipWrongPsu() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity previousAuthorisation = new AuthorisationEntity();
        previousAuthorisation.setAuthorisationType(AUTHORISATION_TYPE);
        previousAuthorisation.setPsuData(WRONG_PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID)).thenReturn(Collections.singletonList(previousAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByParent(PARENT_ID, AUTHORISATION_TYPE, PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByAuthorisation() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity currentAuthorisation = buildAuthorisationEntity(AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);
        AuthorisationEntity previousAuthorisation = buildAuthorisationEntity(PREVIOUS_AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID))
            .thenReturn(Arrays.asList(currentAuthorisation, previousAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        ArgumentCaptor<AuthorisationEntity> authorisationsCaptor = ArgumentCaptor.forClass(AuthorisationEntity.class);

        // When
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(currentAuthorisation, PSU_ID_DATA);

        // Then
        verify(authorisationRepository).save(authorisationsCaptor.capture());

        AuthorisationEntity capturedAuthorisation = authorisationsCaptor.getValue();
        assertEquals(ScaStatus.FAILED, capturedAuthorisation.getScaStatus());
        assertEquals(PREVIOUS_AUTHORISATION_ID, capturedAuthorisation.getExternalId());
    }

    @Test
    void closePreviousAuthorisationsByAuthorisation_noOtherAuthorisations() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity currentAuthorisation = buildAuthorisationEntity(AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID))
            .thenReturn(Collections.singletonList(currentAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(currentAuthorisation, PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByAuthorisation_nullPsu() {
        // Given
        AuthorisationEntity currentAuthorisation = buildAuthorisationEntity(AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(currentAuthorisation, null);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByAuthorisation_emptyPsu() {
        // Given
        AuthorisationEntity currentAuthorisation = buildAuthorisationEntity(AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(currentAuthorisation, EMPTY_PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByAuthorisation_shouldSkipWrongType() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity currentAuthorisation = buildAuthorisationEntity(AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);
        AuthorisationEntity previousAuthorisation = buildAuthorisationEntity(PREVIOUS_AUTHORISATION_ID, WRONG_AUTHORISATION_TYPE, PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID))
            .thenReturn(Arrays.asList(currentAuthorisation, previousAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(currentAuthorisation, PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void closePreviousAuthorisationsByAuthorisation_shouldSkipWrongPsu() {
        // Given
        when(authServiceResolver.getAuthService(AUTHORISATION_TYPE)).thenReturn(authService);

        AuthorisationEntity currentAuthorisation = buildAuthorisationEntity(AUTHORISATION_ID, AUTHORISATION_TYPE, PSU_DATA);
        AuthorisationEntity previousAuthorisation = buildAuthorisationEntity(PREVIOUS_AUTHORISATION_ID, AUTHORISATION_TYPE, WRONG_PSU_DATA);
        when(authService.getAuthorisationsByParentId(PARENT_ID))
            .thenReturn(Arrays.asList(currentAuthorisation, previousAuthorisation));

        when(psuDataMapper.mapToPsuData(PSU_ID_DATA)).thenReturn(PSU_DATA);

        // When
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(currentAuthorisation, PSU_ID_DATA);

        // Then
        verify(authorisationRepository, never()).save(any());
    }

    @NotNull
    private AuthorisationEntity buildAuthorisationEntity(String externalId, AuthorisationType authorisationType, PsuData psuData) {
        AuthorisationEntity authorisation = new AuthorisationEntity();
        authorisation.setExternalId(externalId);
        authorisation.setParentExternalId(PARENT_ID);
        authorisation.setAuthorisationType(authorisationType);
        authorisation.setPsuData(psuData);
        return authorisation;
    }
}
