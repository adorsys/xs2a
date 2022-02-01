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

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TppResponseLogBuilderTest {
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private TppInfo tppInfo;

    @InjectMocks
    private TppResponseLogBuilder tppResponseLogBuilder;

    @Test
    void withTpp_shouldAddTppId() {
        // When
        tppResponseLogBuilder.withTpp(tppInfo);

        // Then
        //noinspection ResultOfMethodCallIgnored
        verify(tppInfo).getAuthorisationNumber();
        verifyNoMoreInteractions(tppInfo);
        verifyNoMoreInteractions(httpServletResponse);
    }

    @Test
    void withResponseStatus_shouldAddResponseStatus() {
        // When
        tppResponseLogBuilder.withResponseStatus();

        // Then
        verify(httpServletResponse).getStatus();
        verifyNoMoreInteractions(httpServletResponse);
    }
}
