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

package de.adorsys.psd2.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum MessageErrorCode {
    CERTIFICATE_INVALID(401),  // "The contents of the signature/corporate seal certificate are not matching PSD2 general PSD2 or attribute requirements
    CERTIFICATE_EXPIRED(401),  //Signature/corporate seal certificate is expired
    CERTIFICATE_BLOCKED(401),  //Signature/corporate seal certificate has been blocked by the ASPSP
    CERTIFICATE_REVOKED(401),  //Signature/corporate seal certificate has been revoked by QSTP
    CERTIFICATE_MISSING(401),  //Signature/corporate seal certificate was not available in the request but is mandated for the corresponding
    SIGNATURE_INVALID(401),  //Application layer eIDAS Signature for TPP authentication is not correct
    SIGNATURE_MISSING(401),  //Application layer eIDAS Signature for TPP authentication is mandated by the ASPSP but is missing
    FORMAT_ERROR(400),  //Format of certain request fields are not matching the XS2A requirements. An explicit path to the corresponding field might be added in the return message
    RESOURCE_BLOCKED(400), //The addressed resource is not addressable by this request, since it is blocked e.g. by a grouping in a signing basket
    PSU_CREDENTIALS_INVALID(401),  // The PSU-ID cannot be matched by the addressed ASPSP or is blocked, or a password resp. OTP was not correct. Additional information might be added
    CORPORATE_ID_INVALID(401),  //The PSU-Corporate-ID cannot be matched by the addressed ASPSP
    CONSENT_INVALID(401),  //The consent was created by this TPP but is not valid for the addressed service/resource
    CONSENT_EXPIRED(401),  //The consent was created by this TPP but has expired and needs to be renewed
    TOKEN_UNKNOWN(401),  //The OAuth2 token cannot be matched by the ASPSP relative to the TPP
    TOKEN_INVALID(401),  //The OAuth2 token is associated to the TPP but is not valid for the addressed service/resource
    TOKEN_EXPIRED(401),  //The OAuth2 token is associated to the TPP but has expired and needs to be renewed

    TIMESTAMP_INVALID(400),  //Timestamp not in accepted time period
    PERIOD_INVALID(400),  //Requested time period out of bound
    SCA_METHOD_UNKNOWN(400),  //Addressed SCA method in the AuthenticationObject Method Select Request is unknown or cannot be matched by the ASPSP with the PSU
    TRANSACTION_ID_INVALID(400),  //The TPP-Transaction-ID is not matching the temporary resource

    //PIS specific error codes
    PRODUCT_INVALID(403),  //The addressed payment product is not available for the PSU
    PRODUCT_UNKNOWN(404),  //The addressed payment product is not supported by the ASPSP
    PAYMENT_FAILED(400),  //The payment initiation POST request failed during the initial process. Additional information may be provided by the ASPSP
    REQUIRED_KID_MISSING(401),  //The payment initiation has failed due to a missing KID. This is a specific message code for the Norwegian market, where ASPSP can require the payer to transmit the KID
    EXECUTION_DATE_INVALID(400), //The requested execution date is not a valid execution date for the ASPSP.
    CARD_INVALID(400), //Addressed card number is unknown to the ASPSP or not associated to the PSU.
    NO_PIIS_ACTIVATION(400), //The PSU has not activated the addressed account for the usage of the PIIS associated with the TPP.

    // AIS specific error code
    //todo task: https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/38
    SESSIONS_NOT_SUPPORTED(400),  //The combined service flag may not be used with this ASPSP
    ACCESS_EXCEEDED(429),  //The access on the account has been exceeding the consented multiplicity per day
    REQUESTED_FORMATS_INVALID(401),  //The requested formats in the Accept header entry are not matching the formats offered by the ASPSP.");

    //SERVICE_INVALID : The addressed service is not valid for the addressed resources or the submitted data
    SERVICE_INVALID_401(401) {
        @Override
        public String getName() {
            return "SERVICE_INVALID";
        }
    },  //401 - if payload
    SERVICE_INVALID_405(405) {
        @Override
        public String getName() {
            return "SERVICE_INVALID";
        }
    },  //405 -if http method
    SERVICE_BLOCKED(403),  //This service is not reachable for the addressed PSU due to a channel independent blocking by the ASPSP. Additional information might be given by the ASPSP

    //CONSENT_UNKNOWN: The consent-ID cannot be matched by the ASPSP relative to the TPP
    CONSENT_UNKNOWN_403(403) {
        @Override
        public String getName() {
            return "CONSENT_UNKNOWN";
        }
    },  //403 - if path
    CONSENT_UNKNOWN_400(400) {
        @Override
        public String getName() {
            return "CONSENT_UNKNOWN";
        }
    },  //400 - if payload

    //RESOURCE_UNKNOWN_404: The addressed resource is unknown relative to the TPP
    RESOURCE_UNKNOWN_404(404) {
        @Override
        public String getName() {
            return "RESOURCE_UNKNOWN";
        }
    }, // 404 - if account-id in path
    RESOURCE_UNKNOWN_403(403) {
        @Override
        public String getName() {
            return "RESOURCE_UNKNOWN";
        }
    }, // 403 - if other resource in path
    RESOURCE_UNKNOWN_400(400) {
        @Override
        public String getName() {
            return "RESOURCE_UNKNOWN";
        }
    }, // 400 - if payload

    // RESOURCE_EXPIRED : The addressed resource is associated with the TPP but has expired, not addressable anymore
    RESOURCE_EXPIRED_403(403) {
        @Override
        public String getName() {
            return "RESOURCE_EXPIRED";
        }
    }, // 403 if path
    RESOURCE_EXPIRED_400(400) {
        @Override
        public String getName() {
            return "RESOURCE_EXPIRED";
        }
    }, // 400 if payload
    PARAMETER_NOT_SUPPORTED(400),
    BEARER_TOKEN_EMPTY(400),
    INTERNAL_SERVER_ERROR(500),
    UNAUTHORIZED(401),
    CONTENT_TYPE_NOT_SUPPORTED(406),
    UNSUPPORTED_MEDIA_TYPE(415)
    ;

    private static Map<String, MessageErrorCode> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(errorCode -> container.put(errorCode.getName(), errorCode));
    }

    @ApiModelProperty(value = "code", example = "400")
    private int code;

    @JsonCreator
    MessageErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getName() {
        return this.name();
    }

    @JsonIgnore
    public static Optional<MessageErrorCode> getByName(String name) {
        return Optional.ofNullable(container.get(name));
    }
}
