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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
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

    public NotificationModeResponseHeaders resolveNotificationHeaders(List<NotificationSupportedMode> usedModes) {
        List<NotificationSupportedMode> supportedModes = aspspProfileServiceWrapper.getNotificationSupportedModes();

        if (supportedModes.contains(NotificationSupportedMode.NONE)) {
            return new NotificationModeResponseHeaders(null, null);
        }

        if (CollectionUtils.isEmpty(usedModes)) {

            log.info("TPP notification URI is not correct or requested modes are not supported!");

            return new NotificationModeResponseHeaders(false, null);
        }

        return new NotificationModeResponseHeaders(true, getModesAsString(usedModes));

    }

    public TppNotificationData getTppNotificationData(String tppNotificationContentPreferred, String tppNotificationUri) {
        List<NotificationSupportedMode> modesFromRequest = parseTppNotificationContentPreferred(tppNotificationContentPreferred);

        return new TppNotificationData(getProcessedNotificationModes(modesFromRequest), tppNotificationUri);
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
