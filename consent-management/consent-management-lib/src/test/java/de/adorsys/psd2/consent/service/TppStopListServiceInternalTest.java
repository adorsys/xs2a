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
import de.adorsys.psd2.xs2a.core.tpp.TppUniqueParamsHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TppStopListServiceInternalTest {
    private final String AUTHORISATION_NUMBER = "Authorisation number";
    private final String AUTHORITY_ID = "Authority id";
    private final String AUTHORISATION_NUMBER_NOT_EXISTING = "Not existing Authorisation number";
    private final String AUTHORITY_ID_NOT_EXISTING = "Not existing Authority id";
    private final String INSTANCE_ID = null;

    @InjectMocks
    private TppStopListServiceInternal tppStopListService;

    @Mock
    private TppStopListRepository tppStopListRepository;

    @Mock
    private TppStopListEntity tppStopListEntity;

    @Test
    public void checkIfTppBlocked_Fail_EmptyStopList() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(buildNotExistingTppUniqueParamsHolder());

        assertFalse(isTppBlocked);
    }

    @Test
    public void checkIfTppBlocked_Success_BlockedTpp() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListEntity.isBlocked())
            .thenReturn(false);

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(buildExistingTppUniqueParamsHolder());

        assertFalse(isTppBlocked);
    }

    @Test
    public void checkIfTppBlocked_Success_NonBlockedTpp() {
        when(tppStopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListEntity.isBlocked())
            .thenReturn(true);

        boolean isTppBlocked = tppStopListService.checkIfTppBlocked(buildNotExistingTppUniqueParamsHolder());

        assertTrue(isTppBlocked);
    }

    private TppUniqueParamsHolder buildNotExistingTppUniqueParamsHolder() {
        return new TppUniqueParamsHolder(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING);
    }

    private TppUniqueParamsHolder buildExistingTppUniqueParamsHolder() {
        return new TppUniqueParamsHolder(AUTHORISATION_NUMBER, AUTHORITY_ID);
    }

}
