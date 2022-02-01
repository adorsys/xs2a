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

package de.adorsys.psd2.xs2a.web.controller.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RequestUriHandler {
    public String trimEndingSlash(String input) {
        String result = input;

        while (StringUtils.endsWith(result, "/")) {
            result = StringUtils.removeEnd(result, "/");
        }
        return result;
    }

    public String handleTransactionUri(String transactionUri, String bookingStatus, Integer pageIndex) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(transactionUri)
                                                        .queryParam("bookingStatus", bookingStatus.toLowerCase());
        if (pageIndex != null) {
            uriComponentsBuilder.replaceQueryParam("pageIndex", pageIndex);
        }
        return uriComponentsBuilder.toUriString();
    }
}
