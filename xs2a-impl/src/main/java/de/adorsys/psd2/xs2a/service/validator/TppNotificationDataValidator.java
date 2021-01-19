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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TppNotificationDataValidator implements BusinessValidator<TppNotificationData> {
    private final TppDomainValidator tppDomainValidator;

    @Override
    public @NotNull ValidationResult validate(@NotNull TppNotificationData tppNotificationData) {
        return ValidationResult.valid();
    }

    @Override
    public @NotNull Set<TppMessageInformation> buildWarningMessages(@NotNull TppNotificationData tppNotificationData) {
        if (CollectionUtils.isNotEmpty(tppNotificationData.getNotificationModes())
                && StringUtils.isNotBlank(tppNotificationData.getTppNotificationUri())) {

            return tppDomainValidator.buildWarningMessages(tppNotificationData.getTppNotificationUri());
        }

        return Collections.emptySet();
    }
}
