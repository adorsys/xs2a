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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
