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
