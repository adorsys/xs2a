/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportedAccountReferenceValidator implements BusinessValidator<Collection<AccountReference>> {

    private final AspspProfileServiceWrapper aspspProfileService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Override
    public @NotNull ValidationResult validate(@NotNull Collection<AccountReference> accountReferences) {
        if (accountReferences.isEmpty()) {
            return ValidationResult.valid();
        }

        Optional<AccountReference> accountWithSeveralUsedTypes = accountReferences.stream()
                                                                     .filter(Objects::nonNull)
                                                                     .filter(ar -> ar.getUsedAccountReferenceFields().size() > 1)
                                                                     .findFirst();

        if (accountWithSeveralUsedTypes.isPresent()) {
            ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(),
                                                                 FORMAT_ERROR.getCode());
            return ValidationResult.invalid(errorType, FORMAT_ERROR_MULTIPLE_ACCOUNT_REFERENCES);
        }

        return validateAccountReference(accountReferences);
    }

    private ValidationResult validateAccountReference(Collection<AccountReference> accountReferences) {

        Set<AccountReferenceType> actualAccountReferenceType = accountReferences.stream()
                                                                   .filter(Objects::nonNull)
                                                                   .flatMap(ar -> ar.getUsedAccountReferenceFields().stream())
                                                                   .collect(Collectors.toSet());

        Set<AccountReferenceType> supportedAccountReferenceType = aspspProfileService.getSupportedAccountReferenceFields().stream()
                                                                      .map(f -> AccountReferenceType.valueOf(f.name()))
                                                                      .collect(Collectors.toSet());

        Collection<AccountReferenceType> wrongReferences = CollectionUtils.subtract(actualAccountReferenceType, supportedAccountReferenceType);

        if (CollectionUtils.isEmpty(wrongReferences)) {
            return ValidationResult.valid();
        } else {
            String wrongReferenceNames = StringUtils.join(wrongReferences, ", ");
            log.info("Supported account reference validation has failed: account reference type: {} is not supported by the ASPSP",
                     wrongReferenceNames);
            ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(),
                                                                 FORMAT_ERROR.getCode());
            return ValidationResult.invalid(errorType, TppMessageInformation.of(
                FORMAT_ERROR_ATTRIBUTE_NOT_SUPPORTED, wrongReferenceNames));
        }
    }
}
