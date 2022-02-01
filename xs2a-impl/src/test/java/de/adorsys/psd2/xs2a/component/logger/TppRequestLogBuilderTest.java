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
