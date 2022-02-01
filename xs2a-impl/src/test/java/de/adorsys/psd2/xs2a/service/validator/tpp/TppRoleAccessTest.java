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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TppRoleAccessTest {

    @ParameterizedTest
    @MethodSource("testData")
    void hasAccessForPath(String targetPath, List<TppRole> tppRoles, boolean expected) {
        assertEquals(expected, TppRoleAccess.hasAccessForPath(tppRoles, targetPath));
    }

    private static Stream<Arguments> testData() {
        return Stream.of(Arguments.arguments("/v1/accounts/test", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/accounts/", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/accounts1", List.of(TppRole.PIISP, TppRole.PISP), true),
                         Arguments.arguments("/v1/accounts", List.of(TppRole.PIISP, TppRole.PISP), false),

                         Arguments.arguments("/v1/consents/test", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/consents/", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/consents1", List.of(TppRole.PIISP, TppRole.PISP), true),
                         Arguments.arguments("/v1/consents", List.of(TppRole.PIISP, TppRole.PISP), false),

                         Arguments.arguments("/v1/card-accounts/test", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/card-accounts/", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/card-accounts1", List.of(TppRole.PIISP, TppRole.PISP), true),
                         Arguments.arguments("/v1/card-accounts", List.of(TppRole.PIISP, TppRole.PISP), false),

                         Arguments.arguments("/v1/trusted-beneficiaries/test", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/trusted-beneficiaries", List.of(TppRole.AISP), true),
                         Arguments.arguments("/v1/trusted-beneficiaries1", List.of(TppRole.PIISP, TppRole.PISP), true),
                         Arguments.arguments("/v1/trusted-beneficiaries", List.of(TppRole.PIISP, TppRole.PISP), false),

                         Arguments.arguments("/v1/payments/test", List.of(TppRole.PISP), true),
                         Arguments.arguments("/v1/payments", List.of(TppRole.PISP), true),
                         Arguments.arguments("/v1/payments1", List.of(TppRole.PIISP, TppRole.AISP), true),
                         Arguments.arguments("/v1/payments", List.of(TppRole.PIISP, TppRole.AISP), false),

                         Arguments.arguments("/v1/bulk-payments/test", List.of(TppRole.PISP), true),
                         Arguments.arguments("/v1/bulk-payments", List.of(TppRole.PISP), true),
                         Arguments.arguments("/v1/bulk-payments1", List.of(TppRole.PIISP, TppRole.AISP), true),
                         Arguments.arguments("/v1/bulk-payments", List.of(TppRole.PIISP, TppRole.AISP), false),

                         Arguments.arguments("/v1/periodic-payments/test", List.of(TppRole.PISP), true),
                         Arguments.arguments("/v1/periodic-payments", List.of(TppRole.PISP), true),
                         Arguments.arguments("/v1/periodic-payments1", List.of(TppRole.PIISP, TppRole.AISP), true),
                         Arguments.arguments("/v1/periodic-payments", List.of(TppRole.PIISP, TppRole.AISP), false),

                         Arguments.arguments("/v1/funds-confirmations/test", List.of(TppRole.PIISP), true),
                         Arguments.arguments("/v1/funds-confirmations", List.of(TppRole.PIISP), true),
                         Arguments.arguments("/v1/funds-confirmations1", List.of(TppRole.PISP, TppRole.AISP), true),
                         Arguments.arguments("/v1/funds-confirmations", List.of(TppRole.PISP, TppRole.AISP), false),

                         Arguments.arguments("/v2/consents/est", List.of(TppRole.PIISP), true),
                         Arguments.arguments("/v2/consents/", List.of(TppRole.PIISP), true),
                         Arguments.arguments("/v2/consents1", List.of(TppRole.PISP, TppRole.AISP), true),
                         Arguments.arguments("/v2/consents", List.of(TppRole.PISP, TppRole.AISP), false)
        );
    }
}
