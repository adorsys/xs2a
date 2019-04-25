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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

/**
 * Validator to be used for validating PSU Data in initial requests to XS2A(e. g. initiate payment or create consent)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PsuDataInInitialRequestValidator implements BusinessValidator<PsuIdData> {
    // TODO move messages to the message bundle https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/791
    private static final String MESSAGE_ERROR_PSU_ID_BLANK = "PSU-ID should not be blank";
    private static final String MESSAGE_ERROR_NO_PSU_ID = "PSU-ID is missing in request";

    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    /**
     * Validates PSU Data that was sent in initial request
     *
     * @param psuIdData PSU Data from the request
     * @return valid result if the PSU Data is valid, invalid result with appropriate error otherwise
     */
    @Override
    public @NotNull ValidationResult validate(@NotNull PsuIdData psuIdData) {
        String psuId = psuIdData.getPsuId();
        if (aspspProfileService.isPsuInInitialRequestMandated() && StringUtils.isBlank(psuId)) {
            ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());

            if (psuId == null) {
                log.info("X-Request-ID: [{}]. PSU Data validation has failed: mandated PSU ID is null",
                         requestProviderService.getRequestId());
                return ValidationResult.invalid(errorType, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU_ID));
            }

            log.info("X-Request-ID: [{}]. PSU Data validation has failed: mandated PSU ID is blank",
                     requestProviderService.getRequestId());
            return ValidationResult.invalid(errorType, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_PSU_ID_BLANK));
        }

        return ValidationResult.valid();
    }
}
