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

package de.adorsys.psd2.xs2a.web.validator.methods.service;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

/**
 * Service to be used to validate 'PSU-IP-Address' header in all REST calls.
 */
@Service
@RequiredArgsConstructor
public class PsuIpAddressValidationService {

    private static final String IP_PATTERN_v4 = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
    private static final String IP_PATTERN_v6 = "^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$";
    private static final String ERROR_TEXT_NULL_IP_ADDRESS = "'PSU-IP-Address' may not be null";
    private static final String ERROR_TEXT_WRONG_IP_ADDRESS = "'PSU-IP-Address' has to be correct v.4 or v.6 IP address";

    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    /**
     * Validates the 'PSU-IP-Address' header for non-null state and correct structure.
     *
     * @param psuIpAddress String with IP address or null
     * @return ValidationResult instance with error code and text (if error occurs)
     */
    public ValidationResult validatePsuIdAddress(String psuIpAddress) {

        if (Objects.isNull(psuIpAddress)) {
            return ValidationResult.invalid(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, ERROR_TEXT_NULL_IP_ADDRESS));
        }

        if (isNonValid(psuIpAddress)) {
            return ValidationResult.invalid(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, ERROR_TEXT_WRONG_IP_ADDRESS));
        }

        return ValidationResult.valid();
    }

    private boolean isNonValid(String psuIpAddress) {
        return !psuIpAddress.matches(IP_PATTERN_v4) && !psuIpAddress.matches(IP_PATTERN_v6);
    }

    private ErrorType buildErrorType() {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());
    }

}
