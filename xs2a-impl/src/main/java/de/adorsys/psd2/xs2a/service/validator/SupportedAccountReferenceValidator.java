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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportedAccountReferenceValidator implements BusinessValidator<Collection<AccountReference>> {
    // TODO move messages to the message bundle https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/791
    private static final String MESSAGE_ERROR_ATTRIBUTE_NOT_SUPPORTED = "Attribute %s is not supported by the ASPSP";

    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Override
    public @NotNull ValidationResult validate(@NotNull Collection<AccountReference> accountReferences) {
        if (accountReferences.isEmpty()) {
            return ValidationResult.valid();
        }

        List<SupportedAccountReferenceField> supportedAccountReferenceFields = aspspProfileService.getSupportedAccountReferenceFields();
        Optional<ValidationResult> failedValidationResult = accountReferences.stream()
                                                                .map(ar -> validateAccountReference(ar, supportedAccountReferenceFields))
                                                                .filter(ValidationResult::isNotValid)
                                                                .findFirst();

        return failedValidationResult.orElseGet(ValidationResult::valid);

    }

    private ValidationResult validateAccountReference(AccountReference accountReference,
                                                      Collection<SupportedAccountReferenceField> supportedFields) {
        AccountReferenceType usedAccountReferenceType = accountReference.getUsedAccountReferenceSelector().getAccountReferenceType();
        boolean referenceSupported = supportedFields.stream()
                                         .map(f -> AccountReferenceType.valueOf(f.name()))
                                         .anyMatch(usedAccountReferenceType::equals);

        if (referenceSupported) {
            return ValidationResult.valid();
        }

        log.info("X-Request-ID: [{}]. Supported account reference validation has failed: account reference type {} is not supported by the ASPSP",
                 requestProviderService.getRequestId(), usedAccountReferenceType);
        ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(),
                                                             FORMAT_ERROR.getCode());
        return ValidationResult.invalid(errorType,
                                        TppMessageInformation.of(FORMAT_ERROR,
                                                                 String.format(MESSAGE_ERROR_ATTRIBUTE_NOT_SUPPORTED, usedAccountReferenceType.toString())));
    }
}
