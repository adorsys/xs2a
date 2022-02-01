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

package de.adorsys.psd2.xs2a.core.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("PMD.ExcessivePublicCount")
public enum MessageErrorCode {
    SERVICE_NOT_SUPPORTED(406), // Requested service or it's part is not supported by ASPSP
    ROLE_INVALID(401), // The TPP does not have the correct PSD2 role to access this service
    CERTIFICATE_INVALID(401),  // The contents of the signature/corporate seal certificate are not matching PSD2 general PSD2 or attribute requirements

    // TPP certificate doesn’t match the initial request
    CERTIFICATE_INVALID_TPP(401) {
        @Override
        public String getName() {
            return CERTIFICATE_INVALID_NAME;
        }
    },
    // You don't have access to this resource
    CERTIFICATE_INVALID_NO_ACCESS(401) {
        @Override
        public String getName() {
            return CERTIFICATE_INVALID_NAME;
        }
    },
    CERTIFICATE_EXPIRED(401),  // Signature/corporate seal certificate is expired
    CERTIFICATE_BLOCKED(401),  // Signature/corporate seal certificate has been blocked by the ASPSP
    CERTIFICATE_REVOKED(401),  // Signature/corporate seal certificate has been revoked by QSTP
    CERTIFICATE_MISSING(401),  // Signature/corporate seal certificate was not available in the request but is mandated for the corresponding
    SIGNATURE_INVALID(401),  // Application layer eIDAS Signature for TPP authentication is not correct
    SIGNATURE_MISSING(401),  // Application layer eIDAS Signature for TPP authentication is mandated by the ASPSP but is missing

    FORBIDDEN(403), // Token is not valid for the addressed service/resource
    // This service is not reachable for the addressed PSU due to incorrect flow
    FORBIDDEN_INCORRECT_FLOW(403) {
        @Override
        public String getName() {
            return "FORBIDDEN";
        }
    },

    FORMAT_ERROR(400),  // Format of certain request fields are not matching the XS2A requirements
    FORMAT_ERROR_IMPLICIT_SB(400){
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    }, // 'TPP-Explicit-Authorisation-Preferred' header should be true for signing basket
    FORMAT_ERROR_OVERSIZE_SB(400){
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    }, // Number of entries in Signing Basket should not exceed more than %s

    // Please provide the PSU identification data
    FORMAT_ERROR_NO_PSU(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // PSU-ID is missing in request
    FORMAT_ERROR_NO_PSU_ID(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // PSU-ID should not be blank
    FORMAT_ERROR_PSU_ID_BLANK(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Only one account reference parameter is allowed
    FORMAT_ERROR_MULTIPLE_ACCOUNT_REFERENCES(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Attribute %s is not supported by the ASPSP
    FORMAT_ERROR_ATTRIBUTE_NOT_SUPPORTED(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Query parameter '%s' is before '%s'
    FORMAT_ERROR_DATE_PERIOD_INVALID(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Only one delta report query parameter can be present in request
    FORMAT_ERROR_MULTIPLE_DELTA_REPORT(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Header '%s' is missing in request
    FORMAT_ERROR_ABSENT_HEADER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Header '%s' should not be null
    FORMAT_ERROR_NULL_HEADER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Header '%s' should not be blank
    FORMAT_ERROR_BLANK_HEADER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Header 'psu-ip-address' has to be correct v.4 or v.6 IP address
    FORMAT_ERROR_WRONG_IP_ADDRESS(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // URIs don't comply with domain from certificate
    FORMAT_ERROR_INVALID_DOMAIN(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Invalid notification content preferred mode: '%s'
    FORMAT_ERROR_INVALID_NOTIFICATION_MODE(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Header 'x-request-id'/'psu-device-id' has to be represented by standard 36-char UUID representation
    FORMAT_ERROR_WRONG_HEADER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Payment not found
    FORMAT_ERROR_PAYMENT_NOT_FOUND(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value 'dayOfExecution' should be a number of day in month
    FORMAT_ERROR_INVALID_DAY_OF_EXECUTION(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value 'monthsOfExecution' should be non empty array of maximum 11 distinct numbers
    FORMAT_ERROR_INVALID_SIZE_MONTHS_OF_EXECUTION(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Values of 'monthsOfExecution' should be numbers of months in year
    FORMAT_ERROR_INVALID_MONTHS_OF_EXECUTION(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Query parameter '%s' is missing in request
    FORMAT_ERROR_ABSENT_PARAMETER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Query parameter '%s' has invalid value
    FORMAT_ERROR_INVALID_PARAMETER_VALUE(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Query parameter '%s' should not be blank
    FORMAT_ERROR_BLANK_PARAMETER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Invalid '%s' format
    FORMAT_ERROR_INVALID_FIELD(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // The field '%s' is not expected in the request
    FORMAT_ERROR_EXTRA_FIELD(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value '%s' cannot be empty
    FORMAT_ERROR_EMPTY_FIELD(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value '%s' should not be more than %s symbols
    FORMAT_ERROR_OVERSIZE_FIELD(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Cannot deserialize the request body
    FORMAT_ERROR_DESERIALIZATION_FAIL(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value '%s' should not be null
    FORMAT_ERROR_NULL_VALUE(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value '%s' has wrong format
    FORMAT_ERROR_WRONG_FORMAT_VALUE(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Wrong format for '%s': value should be %s '%s' format
    FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Consent object can not contain both list of accounts and the flag allPsd2 or availableAccounts
    FORMAT_ERROR_CONSENT_INCORRECT(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value '%s' should not be in the past
    FORMAT_ERROR_DATE_IN_THE_PAST(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value 'frequencyPerDay' should not be lower than 1
    FORMAT_ERROR_INVALID_FREQUENCY(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value '%s' is required
    FORMAT_ERROR_VALUE_REQUIRED(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Value 'address.country' should be ISO 3166 ALPHA2 country code
    FORMAT_ERROR_ADDRESS_COUNTRY_INCORRECT(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Wrong format for '%s': value should be boolean format
    FORMAT_ERROR_BOOLEAN_VALUE(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Header '%s' should not be more than %s symbols
    FORMAT_ERROR_OVERSIZE_HEADER(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Path parameter 'download-id' has to be represented in Base64
    FORMAT_ERROR_PATH_PARAMETER_INVALID(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Addressed account is unknown to the ASPSP or not associated to the PSU
    FORMAT_ERROR_UNKNOWN_ACCOUNT(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Getting SCA methods failed
    FORMAT_ERROR_SCA_METHODS(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Payment not executed. Transaction status is: %s. SCA status: %s
    FORMAT_ERROR_PAYMENT_NOT_EXECUTED(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Couldn’t execute payment cancellation
    FORMAT_ERROR_CANCELLATION(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Unknown response type
    FORMAT_ERROR_RESPONSE_TYPE(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    // Invalid Bulk Entry format
    FORMAT_ERROR_BULK(400) {
        @Override
        public String getName() {
            return FORMAT_ERROR_NAME;
        }
    },
    RESOURCE_BLOCKED(400), // Payment is finalised already and cannot be cancelled
    // The addressed resource is not addressable by this request, since it is blocked e.g. by a grouping in a signing basket
    RESOURCE_BLOCKED_SB(400) {
        @Override
        public String getName() {
            return RESOURCE_BLOCKED.getName();
        }
    },
    PSU_CREDENTIALS_INVALID(401),  // The PSU-ID cannot be matched by the addressed ASPSP or is blocked, or a password resp. OTP was not correct

    // Couldn’t execute payment cancellation
    PSU_CREDENTIALS_INVALID_FOR_CANCELLATION(401) {
        @Override
        public String getName() {
            return "PSU_CREDENTIALS_INVALID";
        }
    },

    CORPORATE_ID_INVALID(401),  // The PSU-Corporate-ID cannot be matched by the addressed ASPSP
    CONSENT_INVALID(401),  // The consent was created by this TPP but is not valid for the addressed service/resource

    // Consent was revoked by PSU
    CONSENT_INVALID_REVOKED(401) {
        @Override
        public String getName() {
            return "CONSENT_INVALID";
        }
    },

    CONSENT_EXPIRED(401),  // The consent was created by this TPP but has expired and needs to be renewed
    TOKEN_UNKNOWN(401),  // The OAuth2 token cannot be matched by the ASPSP relative to the TPP
    TOKEN_INVALID(401),  // The OAuth2 token is associated to the TPP but is not valid for the addressed service/resource
    TOKEN_EXPIRED(401),  // The OAuth2 token is associated to the TPP but has expired and needs to be renewed

    TIMESTAMP_INVALID(400),  // Timestamp not in accepted time period
    PERIOD_INVALID(400),  // Requested time period out of bound

    // Date values has wrong order
    PERIOD_INVALID_WRONG_ORDER(400) {
        @Override
        public String getName() {
            return "PERIOD_INVALID";
        }
    },

    SCA_METHOD_UNKNOWN(400),  // Addressed SCA method in the AuthenticationObject Method Select Request is unknown or cannot be matched by the ASPSP with the PSU

    // Process mismatch. PSU does not have any SCA method
    SCA_METHOD_UNKNOWN_PROCESS_MISMATCH(400) {
        @Override
        public String getName() {
            return "SCA_METHOD_UNKNOWN";
        }
    },
    TRANSACTION_ID_INVALID(400),  // The TPP-Transaction-ID is not matching the temporary resource

    // PIS specific error codes
    PRODUCT_INVALID(403),  // The addressed payment product is not available for the PSU

    // Payment product invalid for addressed payment
    PRODUCT_INVALID_FOR_PAYMENT(403) {
        @Override
        public String getName() {
            return "PRODUCT_INVALID";
        }
    },

    PRODUCT_UNKNOWN(404),  // The addressed payment product is not supported by the ASPSP

    // Wrong payment product: %s
    PRODUCT_UNKNOWN_WRONG_PAYMENT_PRODUCT(404) {
        @Override
        public String getName() {
            return "PRODUCT_UNKNOWN";
        }
    },

    PAYMENT_FAILED(400),  // The payment initiation POST request failed during the initial process

    // Unknown payment type: %s
    PAYMENT_FAILED_TYPE_UNKNOWN(400) {
        @Override
        public String getName() {
            return "PAYMENT_FAILED";
        }
    },
    // Couldn't get payment by ID
    PAYMENT_FAILED_INCORRECT_ID(400) {
        @Override
        public String getName() {
            return "PAYMENT_FAILED";
        }
    },

    KID_MISSING(401),  // The payment initiation has failed due to a missing KID
    EXECUTION_DATE_INVALID(400), // The requested execution date is not a valid execution date for the ASPSP

    // Value 'requestedExecutionDate' should not be in the past
    EXECUTION_DATE_INVALID_IN_THE_PAST(400) {
        @Override
        public String getName() {
            return "EXECUTION_DATE_INVALID";
        }
    },

    CARD_INVALID(400), // Addressed card number is unknown to the ASPSP or not associated to the PSU
    NO_PIIS_ACTIVATION(400), // The PSU has not activated the addressed account for the usage of the PIIS associated with the TPP

    // Signing Basket Specific Error Codes
    REFERENCE_MIX_INVALID(400), // The used combination of referenced objects is not supported in the ASPSPs signing basket function.
    REFERENCE_STATUS_INVALID(409), // At least one of the references is already fully authorised.

    // AIS specific error code
    SESSIONS_NOT_SUPPORTED(400),  // Sessions are not supported by ASPSP
    ACCESS_EXCEEDED(429),  // The access on the account has been exceeding the consented multiplicity per day
    REQUESTED_FORMATS_INVALID(406),  // The requested formats in the Accept header entry are not matching the formats offered by the ASPSP

    // 400 - The addressed service is not valid for the addressed resources or the submitted data because of payload
    SERVICE_INVALID_400(400) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // 400 - Service invalid for addressed payment
    SERVICE_INVALID_400_FOR_PAYMENT(400) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // 400 - Global Consent is not supported by ASPSP
    SERVICE_INVALID_400_FOR_GLOBAL_CONSENT(400) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // 405 - The addressed service is not valid for the addressed resources or the submitted data
    SERVICE_INVALID_405(405) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // 405 - Signing basket is not supported by ASPSP
    SERVICE_INVALID_405_SB(405) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // 405 - Wrong payment service
    SERVICE_INVALID_405_FOR_PAYMENT(405) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // 405 - HTTP method '%s' is not supported
    SERVICE_INVALID_405_METHOD_NOT_SUPPORTED(405) {
        @Override
        public String getName() {
            return SERVICE_INVALID_NAME;
        }
    },
    // This service is not reachable for the addressed PSU due to a channel independent blocking by the ASPSP
    SERVICE_BLOCKED(403),

    // The consent-ID cannot be matched by the ASPSP relative to the TPP because of path
    CONSENT_UNKNOWN_403(403) {
        @Override
        public String getName() {
            return CONSENT_UNKNOWN_NAME;
        }
    },
    // TPP certificate doesn’t match the initial request
    CONSENT_UNKNOWN_403_INCORRECT_CERTIFICATE(403) {
        @Override
        public String getName() {
            return CONSENT_UNKNOWN_NAME;
        }
    },
    // The consent-ID cannot be matched by the ASPSP relative to the TPP because of payload
    CONSENT_UNKNOWN_400(400) {
        @Override
        public String getName() {
            return CONSENT_UNKNOWN_NAME;
        }
    },
    // TPP certificate doesn’t match the initial request
    CONSENT_UNKNOWN_400_INCORRECT_CERTIFICATE(400) {
        @Override
        public String getName() {
            return CONSENT_UNKNOWN_NAME;
        }
    },
    // Unknown TPP access type: %s
    CONSENT_UNKNOWN_400_UNKNOWN_ACCESS_TYPE(400) {
        @Override
        public String getName() {
            return CONSENT_UNKNOWN_NAME;
        }
    },
    // TPP access type should not be null
    CONSENT_UNKNOWN_400_NULL_ACCESS_TYPE(400) {
        @Override
        public String getName() {
            return CONSENT_UNKNOWN_NAME;
        }
    },
    // The addressed resource is unknown relative to the TPP because of account-id in path
    RESOURCE_UNKNOWN_404(404) {
        @Override
        public String getName() {
            return RESOURCE_UNKNOWN_NAME;
        }
    },
    // Payment not found
    RESOURCE_UNKNOWN_404_NO_PAYMENT(404) {
        @Override
        public String getName() {
            return RESOURCE_UNKNOWN_NAME;
        }
    },
    // PIS authorisation is not found
    RESOURCE_UNKNOWN_404_NO_AUTHORISATION(404) {
        @Override
        public String getName() {
            return RESOURCE_UNKNOWN_NAME;
        }
    },
    // PIS cancellation authorisation is not found
    RESOURCE_UNKNOWN_404_NO_CANC_AUTHORISATION(404) {
        @Override
        public String getName() {
            return RESOURCE_UNKNOWN_NAME;
        }
    },
    // The addressed resource is unknown relative to the TPP because of other resource in path
    RESOURCE_UNKNOWN_403(403) {
        @Override
        public String getName() {
            return RESOURCE_UNKNOWN_NAME;
        }
    },
    // The addressed resource is unknown relative to the TPP because of payload
    RESOURCE_UNKNOWN_400(400) {
        @Override
        public String getName() {
            return RESOURCE_UNKNOWN_NAME;
        }
    },
    // The addressed resource is associated with the TPP but has expired, not addressable anymore because of path
    RESOURCE_EXPIRED_403(403) {
        @Override
        public String getName() {
            return RESOURCE_EXPIRED_NAME;
        }
    },
    // The addressed resource is associated with the TPP but has expired, not addressable anymore because of payload
    RESOURCE_EXPIRED_400(400) {
        @Override
        public String getName() {
            return RESOURCE_EXPIRED_NAME;
        }
    },

    PARAMETER_NOT_SUPPORTED(400), // The parameter is not supported by the API provider

    // bookingStatus '%s' is not supported by ASPSP
    PARAMETER_NOT_SUPPORTED_BOOKING_STATUS(400) {
        @Override
        public String getName() {
            return PARAMETER_NOT_SUPPORTED_STRING;
        }
    },
    // Parameter 'entryReferenceFrom' is not supported by ASPSP
    PARAMETER_NOT_SUPPORTED_ENTRY_REFERENCE_FROM(400) {
        @Override
        public String getName() {
            return PARAMETER_NOT_SUPPORTED_STRING;
        }
    },
    // Parameter 'deltaList' is not supported by ASPSP
    PARAMETER_NOT_SUPPORTED_DELTA_LIST(400) {
        @Override
        public String getName() {
            return PARAMETER_NOT_SUPPORTED_STRING;
        }
    },
    // Wrong payment type: %s
    PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE(400) {
        @Override
        public String getName() {
            return PARAMETER_NOT_SUPPORTED_STRING;
        }
    },

    BEARER_TOKEN_EMPTY(400), // Token must not be empty
    INTERNAL_SERVER_ERROR(500), // Internal Server Error
    UNAUTHORIZED(401), // The TPP or the PSU is not correctly authorized to perform the request

    // Please provide the PSU identification data
    UNAUTHORIZED_NO_PSU(401) {
        @Override
        public String getName() {
            return UNAUTHORIZED_STRING;
        }
    },
    // Couldn’t authorise payment cancellation
    UNAUTHORIZED_CANCELLATION(401) {
        @Override
        public String getName() {
            return UNAUTHORIZED_STRING;
        }
    },
    // Please retrieve token first from %s
    UNAUTHORIZED_NO_TOKEN(401) {
        @Override
        public String getName() {
            return UNAUTHORIZED_STRING;
        }
    },

    PARAMETER_NOT_CONSISTENT(400),
    CONTENT_TYPE_NOT_SUPPORTED(406), // The required response content-type is not supported by ASPSP
    UNSUPPORTED_MEDIA_TYPE(415), // Unsupported Media Type
    CANCELLATION_INVALID(405), // Payment initiation cannot be cancelled due to legal or other operational reasons
    SERVICE_UNAVAILABLE(503), // Service is unavailable
    STATUS_INVALID(409), // The addressed resource does not allow additional authorisation
    FUNDS_CONFIRMATION_FAILED(400), // The funds confirmation request failed
    SCA_INVALID(400), // SCA of the resource failed during confirmation of authorisation

    CONSENT_VALIDATION_FAILED(500);

    private static final String CERTIFICATE_INVALID_NAME = "CERTIFICATE_INVALID";
    private static final String FORMAT_ERROR_NAME = "FORMAT_ERROR";
    private static final String SERVICE_INVALID_NAME = "SERVICE_INVALID";
    private static final String CONSENT_UNKNOWN_NAME = "CONSENT_UNKNOWN";
    private static final String RESOURCE_UNKNOWN_NAME = "RESOURCE_UNKNOWN";
    private static final String RESOURCE_EXPIRED_NAME = "RESOURCE_EXPIRED";
    private static final String PARAMETER_NOT_SUPPORTED_STRING = "PARAMETER_NOT_SUPPORTED";
    private static final String UNAUTHORIZED_STRING = "UNAUTHORIZED";
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
