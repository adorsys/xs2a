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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Validator to be used for validating PSU Data in initial requests to XS2A(e. g. initiate payment or create consent)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PsuDataInInitialRequestValidator implements BusinessValidator<PsuIdData> {

    private final AspspProfileServiceWrapper aspspProfileService;
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
                log.info("PSU Data validation has failed: mandated PSU ID is null");
                return ValidationResult.invalid(errorType, FORMAT_ERROR_NO_PSU_ID);
            }

            log.info("PSU Data validation has failed: mandated PSU ID is blank");
            return ValidationResult.invalid(errorType, FORMAT_ERROR_PSU_ID_BLANK);
        }

        return ValidationResult.valid();
    }
}
