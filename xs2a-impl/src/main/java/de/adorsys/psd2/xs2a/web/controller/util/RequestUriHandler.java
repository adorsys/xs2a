/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
                                                        .queryParam("bookingStatus", bookingStatus);
        if (pageIndex != null) {
            uriComponentsBuilder.replaceQueryParam("pageIndex", pageIndex);
        }
        return uriComponentsBuilder.toUriString();
    }
}
