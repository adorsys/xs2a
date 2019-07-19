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

package de.adorsys.psd2.xs2a.web.validator.header;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.mock.web.MockHttpServletRequest;

public class ErrorBuildingServiceMock extends ErrorBuildingService {
    private ErrorType errorType;

    public ErrorBuildingServiceMock(ErrorType errorType) {
        super(new ServiceTypeDiscoveryService(new MockHttpServletRequest()), new ServiceTypeToErrorTypeMapper(),
              null, new ObjectMapper());
        this.errorType = errorType;
    }

    @Override
    public ErrorType buildErrorType() {
        return errorType;
    }
}
