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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ErrorToActionStatusMapper {

    public ActionStatus mapActionStatusError(MessageErrorCode error, boolean withBalance, TypeAccess access) {
        ActionStatus actionStatus = ActionStatus.FAILURE_ACCOUNT;
        if (error == MessageErrorCode.ACCESS_EXCEEDED) {
            actionStatus = ActionStatus.CONSENT_LIMIT_EXCEEDED;
        } else if (error == MessageErrorCode.CONSENT_EXPIRED) {
            actionStatus = ActionStatus.CONSENT_INVALID_STATUS;
        } else if (error.getName().equals(MessageErrorCode.CONSENT_UNKNOWN_400.getName()) && error.getCode() == MessageErrorCode.CONSENT_UNKNOWN_400.getCode()) {
            actionStatus = ActionStatus.CONSENT_NOT_FOUND;
        } else if (error.getName().equals(MessageErrorCode.CONSENT_INVALID.getName())) {
            if (access == TypeAccess.TRANSACTION) {
                actionStatus = ActionStatus.FAILURE_TRANSACTION;
            } else if (access == TypeAccess.BALANCE || withBalance) {
                actionStatus = ActionStatus.FAILURE_BALANCE;
            }
        }
        return actionStatus;
    }
}
