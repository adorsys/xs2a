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

package de.adorsys.psd2.xs2a.service.discovery;

import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class ServiceTypeDiscoveryService {
    private final HttpServletRequest request;

    /**
     * Gets service type from request URI by invoking ServiceTypeDiscovery
     *
     * @return ServiceType value
     */
    public ServiceType getServiceType() {
        return ServiceTypeDiscovery.getServiceType(new UrlPathHelper().getPathWithinApplication(request));
    }
}

