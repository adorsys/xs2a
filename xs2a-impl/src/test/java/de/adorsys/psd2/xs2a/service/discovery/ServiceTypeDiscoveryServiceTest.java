/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.discovery;

import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ServiceTypeDiscoveryServiceTest {
    private MockHttpServletRequest request;

    @InjectMocks
    private ServiceTypeDiscoveryService cut;

    @Test
    void getServiceType() {
        request = new MockHttpServletRequest("GET", "/v1/consents");
        cut = new ServiceTypeDiscoveryService(request);
        ServiceType result = cut.getServiceType();

        assertEquals("AIS", result.name());
    }

    @Test
    void getServiceTypeWithContextPath() {
        request = new MockHttpServletRequest("GET", "/xs2a/v1/consents");
        request.setContextPath("/xs2a");
        cut = new ServiceTypeDiscoveryService(request);
        ServiceType result = cut.getServiceType();

        assertEquals("AIS", result.name());
    }
}
