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

package de.adorsys.psd2.xs2a.web.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiPartBoundaryBuilder {
    static final String DEFAULT_BOUNDARY = "--AaaBbbCcc";
    private static final String BOUNDARY = "boundary=";
    private static final String BOUNDARY_PLACEHOLDER = "{BOUNDARY}";
    private static final String XMLPART_PLACEHOLDER = "{XML_PART}";
    private static final String JSONPART_PLACEHOLDER = "{JSON_PART}";
    private static final String BOUNDARY_PREFIX = "--";
    private static final String TEMPLATE_PATH = "/template" + "/multipart-payment-template.txt";

    public String getMultiPartContent(HttpServletRequest request, String xmlSct, String jsonPart) {
        String contentTypeHeader = request.getHeader(HttpHeaders.CONTENT_TYPE);

        String boundary = DEFAULT_BOUNDARY;
        if (contentTypeHeader != null
                && contentTypeHeader.contains(MediaType.MULTIPART_FORM_DATA_VALUE)
                && contentTypeHeader.contains(BOUNDARY)) {
            String boundaryValue = contentTypeHeader.substring(contentTypeHeader.indexOf(BOUNDARY) + BOUNDARY.length());
            boundary = boundaryValue.startsWith(BOUNDARY_PREFIX) ? boundaryValue : BOUNDARY_PREFIX + boundaryValue;
        }
        return MultiPartBoundaryBuilderTemplateUtil.getTemplate(TEMPLATE_PATH)
                   .replace(BOUNDARY_PLACEHOLDER, boundary)
                   .replace(XMLPART_PLACEHOLDER, xmlSct)
                   .replace(JSONPART_PLACEHOLDER, jsonPart)
                   .trim();
    }
}
