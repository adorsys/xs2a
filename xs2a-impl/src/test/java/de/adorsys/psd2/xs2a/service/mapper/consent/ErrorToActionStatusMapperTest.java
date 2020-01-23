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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ErrorToActionStatusMapper.class})
class ErrorToActionStatusMapperTest {

    @Autowired
    private ErrorToActionStatusMapper mapper;

    @Test
    void mapActionStatusError() {
        assertEquals(ActionStatus.CONSENT_LIMIT_EXCEEDED, mapper.mapActionStatusError(MessageErrorCode.ACCESS_EXCEEDED, false, null));
        assertEquals(ActionStatus.CONSENT_INVALID_STATUS, mapper.mapActionStatusError(MessageErrorCode.CONSENT_EXPIRED, false, null));
        assertEquals(ActionStatus.CONSENT_NOT_FOUND, mapper.mapActionStatusError(MessageErrorCode.CONSENT_UNKNOWN_400, false, null));

        assertEquals(ActionStatus.FAILURE_TRANSACTION, mapper.mapActionStatusError(MessageErrorCode.CONSENT_INVALID, false, TypeAccess.TRANSACTION));
        assertEquals(ActionStatus.FAILURE_TRANSACTION, mapper.mapActionStatusError(MessageErrorCode.CONSENT_INVALID, true, TypeAccess.TRANSACTION));

        assertEquals(ActionStatus.FAILURE_BALANCE, mapper.mapActionStatusError(MessageErrorCode.CONSENT_INVALID, true, TypeAccess.BALANCE));
        assertEquals(ActionStatus.FAILURE_BALANCE, mapper.mapActionStatusError(MessageErrorCode.CONSENT_INVALID, false, TypeAccess.BALANCE));
        assertEquals(ActionStatus.FAILURE_BALANCE, mapper.mapActionStatusError(MessageErrorCode.CONSENT_INVALID, true, null));
    }
}
