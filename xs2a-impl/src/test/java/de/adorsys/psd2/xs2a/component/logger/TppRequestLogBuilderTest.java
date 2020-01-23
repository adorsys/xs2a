/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.component.logger;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TppRequestLogBuilderTest {
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private TppInfo tppInfo;

    @InjectMocks
    private TppRequestLogBuilder tppRequestLogBuilder;

    @Test
    void withTpp_shouldAddTppIdAndIpAndRoles() {
        // When
        tppRequestLogBuilder.withTpp(tppInfo);

        // Then
        //noinspection ResultOfMethodCallIgnored
        verify(tppInfo).getAuthorisationNumber();
        verify(httpServletRequest).getRemoteAddr();
        //noinspection ResultOfMethodCallIgnored
        verify(tppInfo).getTppRoles();
        verifyNoMoreInteractions(tppInfo);
        verifyNoMoreInteractions(httpServletRequest);
    }

    @Test
    void withRequestUri_shouldAddRequestUri() {
        // When
        tppRequestLogBuilder.withRequestUri();

        // Then
        verify(httpServletRequest).getRequestURI();
        verifyNoMoreInteractions(httpServletRequest);
    }
}
