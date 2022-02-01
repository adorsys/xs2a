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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TppStopListServiceInternalTest {
    private final String AUTHORISATION_NUMBER = "Authorisation number";
    private final String AUTHORISATION_NUMBER_NOT_EXISTING = "Not existing Authorisation number";
    private final String INSTANCE_ID = null;

    @InjectMocks
    private TppStopListServiceInternal tppStopListService;

    @Mock
    private TppStopListRepository tppStopListRepository;

    @Mock
    private TppStopListEntity tppStopListEntity;

    @Test
    void checkIfTppBlocked_Fail_EmptyStopList() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        CmsResponse<Boolean> isTppBlocked = tppStopListService.checkIfTppBlocked(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID);

        assertTrue(isTppBlocked.isSuccessful());

        assertFalse(isTppBlocked.getPayload());
    }

    @Test
    void checkIfTppBlocked_Success_BlockedTpp() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListEntity.isBlocked())
            .thenReturn(false);

        CmsResponse<Boolean> isTppBlocked = tppStopListService.checkIfTppBlocked(AUTHORISATION_NUMBER, INSTANCE_ID);

        assertTrue(isTppBlocked.isSuccessful());

        assertFalse(isTppBlocked.getPayload());
    }

    @Test
    void checkIfTppBlocked_Success_NonBlockedTpp() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListEntity.isBlocked())
            .thenReturn(true);

        CmsResponse<Boolean> isTppBlocked = tppStopListService.checkIfTppBlocked(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID);

        assertTrue(isTppBlocked.isSuccessful());

        assertTrue(isTppBlocked.getPayload());
    }
}

