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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class TppRoleValidationServiceTest {

    @InjectMocks
    private TppRoleValidationService tppRoleValidationService;

    @Test
    void shouldSuccess_when_correctRole() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/v1/accounts");

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.AISP), request)).isTrue();
    }

    @Test
    void shouldFail_when_wrongRole() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/v1/payments/sepa");

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.PIISP), request)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("params")
    void shouldSuccess_when_aspspRole(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);

        assertThat(tppRoleValidationService.hasAccess(buildTppInfo(TppRole.ASPSP), request)).isTrue();
    }

    private static Stream<Arguments> params() {
        return Stream.of(Arguments.arguments("GET", "/v1/consents"),
                         Arguments.arguments("POST", "/v1/payments/sepa"),
                         Arguments.arguments("GET", "/v1/accounts")
        );
    }

    private TppInfo buildTppInfo(TppRole tppRole) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setTppRoles(singletonList(tppRole));
        return tppInfo;
    }
}
