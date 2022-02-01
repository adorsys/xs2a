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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTypeServiceTest {
    private final EventTypeService eventTypeService = new EventTypeService();

    @Test
    void getEventType_Identification() {
        //Given
        Map.of(
            EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
            EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
            EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED,
            EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED
        ).forEach((key, value) -> {
            CommonAuthorisationParameters updateAuthorisationRequest = buildUpdateAuthorisationRequest_Identification();
            //When
            EventType eventType = eventTypeService.getEventType(updateAuthorisationRequest, key);
            //Then
            assertEquals(value, eventType);
        });
    }

    @Test
    void getEventType_Authentication() {
        //Given
        Map.of(
            EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
            EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
            EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED,
            EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_AUTHENTICATION_REQUEST_RECEIVED
        ).forEach((key, value) -> {
            CommonAuthorisationParameters updateAuthorisationRequest = buildUpdateAuthorisationRequest_Authentication();
            //When
            EventType eventType = eventTypeService.getEventType(updateAuthorisationRequest, key);
            //Then
            assertEquals(value, eventType);
        });
    }

    @Test
    void getEventType_SelectAuthenticationMethod() {
        //Given
        Map.of(
            EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
            EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
            EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED,
            EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_SELECT_AUTHENTICATION_METHOD_REQUEST_RECEIVED
        ).forEach((key, value) -> {
            CommonAuthorisationParameters updateAuthorisationRequest = buildUpdateAuthorisationRequest_SelectAuthenticationMethod();
            //When
            EventType eventType = eventTypeService.getEventType(updateAuthorisationRequest, key);
            //Then
            assertEquals(value, eventType);
        });
    }

    @Test
    void getEventType_Tan() {
        //Given
        Map.of(
            EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_TAN_REQUEST_RECEIVED,
            EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_TAN_REQUEST_RECEIVED,
            EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_TAN_REQUEST_RECEIVED,
            EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_TAN_REQUEST_RECEIVED
        ).forEach((key, value) -> {
            CommonAuthorisationParameters updateAuthorisationRequest = buildUpdateAuthorisationRequest_Tan();
            //When
            EventType eventType = eventTypeService.getEventType(updateAuthorisationRequest, key);
            //Then
            assertEquals(value, eventType);
        });
    }

    @Test
    void getEventType_ConfirmationCode() {
        //Given
        Map.of(
            EventAuthorisationType.AIS, EventType.UPDATE_AIS_CONSENT_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
            EventAuthorisationType.PIIS, EventType.UPDATE_PIIS_CONSENT_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
            EventAuthorisationType.PIS, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED,
            EventAuthorisationType.PIS_CANCELLATION, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_CONFIRMATION_CODE_REQUEST_RECEIVED
        ).forEach((key, value) -> {
            CommonAuthorisationParameters updateAuthorisationRequest = buildUpdateAuthorisationRequest_ConfirmationCode();
            //When
            EventType eventType = eventTypeService.getEventType(updateAuthorisationRequest, key);
            //Then
            assertEquals(value, eventType);
        });
    }

    private CommonAuthorisationParameters buildUpdateAuthorisationRequest_Identification() {
        return new CommonAuthorisationParameters() {
            @Override
            public PsuIdData getPsuData() {
                return new PsuIdData();
            }

            @Override
            public String getBusinessObjectId() {
                return null;
            }

            @Override
            public String getAuthorisationId() {
                return null;
            }

            @Override
            public ScaStatus getScaStatus() {
                return null;
            }

            @Override
            public boolean isUpdatePsuIdentification() {
                return false;
            }

            @Override
            public String getAuthenticationMethodId() {
                return null;
            }

            @Override
            public String getScaAuthenticationData() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getConfirmationCode() {
                return null;
            }
        };
    }

    private CommonAuthorisationParameters buildUpdateAuthorisationRequest_Authentication() {
        return new CommonAuthorisationParameters() {
            @Override
            public PsuIdData getPsuData() {
                return null;
            }

            @Override
            public String getBusinessObjectId() {
                return null;
            }

            @Override
            public String getAuthorisationId() {
                return null;
            }

            @Override
            public ScaStatus getScaStatus() {
                return null;
            }

            @Override
            public boolean isUpdatePsuIdentification() {
                return false;
            }

            @Override
            public String getAuthenticationMethodId() {
                return null;
            }

            @Override
            public String getScaAuthenticationData() {
                return null;
            }

            @Override
            public String getPassword() {
                return "****";
            }

            @Override
            public String getConfirmationCode() {
                return null;
            }
        };
    }

    private CommonAuthorisationParameters buildUpdateAuthorisationRequest_SelectAuthenticationMethod() {
        return new CommonAuthorisationParameters() {
            @Override
            public PsuIdData getPsuData() {
                return null;
            }

            @Override
            public String getBusinessObjectId() {
                return null;
            }

            @Override
            public String getAuthorisationId() {
                return null;
            }

            @Override
            public ScaStatus getScaStatus() {
                return null;
            }

            @Override
            public boolean isUpdatePsuIdentification() {
                return false;
            }

            @Override
            public String getAuthenticationMethodId() {
                return "method";
            }

            @Override
            public String getScaAuthenticationData() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getConfirmationCode() {
                return null;
            }
        };
    }

    private CommonAuthorisationParameters buildUpdateAuthorisationRequest_Tan() {
        return new CommonAuthorisationParameters() {
            @Override
            public PsuIdData getPsuData() {
                return null;
            }

            @Override
            public String getBusinessObjectId() {
                return null;
            }

            @Override
            public String getAuthorisationId() {
                return null;
            }

            @Override
            public ScaStatus getScaStatus() {
                return null;
            }

            @Override
            public boolean isUpdatePsuIdentification() {
                return false;
            }

            @Override
            public String getAuthenticationMethodId() {
                return null;
            }

            @Override
            public String getScaAuthenticationData() {
                return "tan";
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getConfirmationCode() {
                return null;
            }
        };
    }

    private CommonAuthorisationParameters buildUpdateAuthorisationRequest_ConfirmationCode() {
        return new CommonAuthorisationParameters() {
            @Override
            public PsuIdData getPsuData() {
                return null;
            }

            @Override
            public String getBusinessObjectId() {
                return null;
            }

            @Override
            public String getAuthorisationId() {
                return null;
            }

            @Override
            public ScaStatus getScaStatus() {
                return null;
            }

            @Override
            public boolean isUpdatePsuIdentification() {
                return false;
            }

            @Override
            public String getAuthenticationMethodId() {
                return null;
            }

            @Override
            public String getScaAuthenticationData() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getConfirmationCode() {
                return "code";
            }
        };
    }
}
