/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(AUTHORISATION_NUMBER_NOT_EXISTING);

        assertFalse(isTppBlocked);
    }

    @Test
    void checkIfTppBlocked_Success_BlockedTpp() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListEntity.isBlocked())
            .thenReturn(false);

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(AUTHORISATION_NUMBER);

        assertFalse(isTppBlocked);
    }

    @Test
    void checkIfTppBlocked_Success_NonBlockedTpp() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListEntity.isBlocked())
            .thenReturn(true);

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(AUTHORISATION_NUMBER_NOT_EXISTING);

        assertTrue(isTppBlocked);
    }
}
