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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.header.TppDomainValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSupportedModeService {
    private static final String HEADER_PREFIX = "status=";
    private static final String MODES_SEPARATOR = ",";

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final TppDomainValidator tppDomainValidator;
    private final RequestProviderService requestProviderService;

    public NotificationModeResponseHeaders resolveNotificationHeaders(List<NotificationSupportedMode> usedModes, String tppNotificationURI) {
        List<NotificationSupportedMode> supportedModes = aspspProfileServiceWrapper.getNotificationSupportedModes();

        if (supportedModes.contains(NotificationSupportedMode.NONE)) {
            return new NotificationModeResponseHeaders(null, null);
        }

        ValidationResult validationResult = tppDomainValidator.validate(tppNotificationURI);

        if (CollectionUtils.isEmpty(usedModes)
                || validationResult.isNotValid()) {

            log.info("InR-ID: [{}], X-Request-ID: [{}]. TPP notification URI is not correct or requested modes are not supported!",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());

            return new NotificationModeResponseHeaders(false, null);
        }

        return new NotificationModeResponseHeaders(true, getModesAsString(usedModes));

    }

    public List<NotificationSupportedMode> getProcessedNotificationModes(String tppNotificationContentPreferred) {
        List<NotificationSupportedMode> modesFromRequest = parseTppNotificationContentPreferred(tppNotificationContentPreferred);

        return getProcessedNotificationModes(modesFromRequest);
    }

    private String getModesAsString(List<NotificationSupportedMode> modes) {
        StringBuilder modesStr = new StringBuilder(HEADER_PREFIX);
        modesStr.append(StringUtils.join(modes, MODES_SEPARATOR));

        return modesStr.toString();
    }

    private List<NotificationSupportedMode> getProcessedNotificationModes(List<NotificationSupportedMode> modesFromRequest) {
        List<NotificationSupportedMode> supportedModes = aspspProfileServiceWrapper.getNotificationSupportedModes();

        if (supportedModes.contains(NotificationSupportedMode.NONE)) {
            return Collections.emptyList();
        }

        return modesFromRequest.stream()
                   .filter(supportedModes::contains)
                   .collect(Collectors.toList());
    }

    private List<NotificationSupportedMode> parseTppNotificationContentPreferred(String tppNotificationContentPreferred) {

        if (StringUtils.isEmpty(tppNotificationContentPreferred)) {
            return Collections.emptyList();
        }

        String[] modes = tppNotificationContentPreferred.replace(HEADER_PREFIX, "").split(MODES_SEPARATOR);

        return Arrays.stream(modes)
                   .map(mode -> NotificationSupportedMode.getByValue(mode.trim()))
                   .filter(mode -> !mode.equals(NotificationSupportedMode.NONE))
                   .collect(Collectors.toList());
    }
}
