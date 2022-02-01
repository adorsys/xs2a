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

package de.adorsys.psd2.xs2a.service.event;

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EventTypeService {
    public EventType getEventType(CommonAuthorisationParameters updateAuthorisationRequest, EventAuthorisationType eventAuthorisationType) {
        return getUpdateEventType(updateAuthorisationRequest).getEventType(eventAuthorisationType);
    }

    private UpdateEventType getUpdateEventType(CommonAuthorisationParameters updateAuthorisationRequest) {
        if (updateAuthorisationRequest.getConfirmationCode() != null) {
            return UpdateEventType.CONFIRMATION_CODE;
        }

        if (updateAuthorisationRequest.getScaAuthenticationData() != null) {
            return UpdateEventType.TAN;
        }

        if (updateAuthorisationRequest.getAuthenticationMethodId() != null) {
            return UpdateEventType.SELECT_AUTHENTICATION_METHOD;
        }

        if (updateAuthorisationRequest.getPassword() != null) {
            return UpdateEventType.AUTHENTICATION;
        }

        return UpdateEventType.IDENTIFICATION;
    }

    private enum UpdateEventType {
        IDENTIFICATION(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED
            )
        ), AUTHENTICATION(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED
            )
        ), SELECT_AUTHENTICATION_METHOD(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED
            )
        ), TAN(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_TAN_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_TAN_REQUEST_RECEIVED
            )
        ), CONFIRMATION_CODE(
            Map.of(
                EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.SB, EventType.UPDATE_SB_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
                EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED
            )
        );

        private Map<EventAuthorisationType, EventType> eventServiceTypes;

        UpdateEventType(Map<EventAuthorisationType, EventType> eventServiceTypes) {
            this.eventServiceTypes = eventServiceTypes;
        }

        public EventType getEventType(EventAuthorisationType eventAuthorisationType) {
            return eventServiceTypes.get(eventAuthorisationType);
        }
    }
}
