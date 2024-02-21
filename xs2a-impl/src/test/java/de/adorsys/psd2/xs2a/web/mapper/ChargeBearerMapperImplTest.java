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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.core.payment.model.ChargeBearer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChargeBearerMapperImplTest {
    private final ChargeBearerMapperImpl mapper = new ChargeBearerMapperImpl();

    @Test
    void mapToChargeBearer_returnsNull() {
        de.adorsys.psd2.model.ChargeBearer chargeBearer = mapper.mapToChargeBearer((ChargeBearer) null);
        assertNull(chargeBearer);
    }

    @Test
    void mapToChargeBearer_returnsNullCode() {
        ChargeBearer chargeBearer = mapper.mapToChargeBearer((de.adorsys.psd2.model.ChargeBearer) null);
        assertNull(chargeBearer);
    }

    @ParameterizedTest
    @EnumSource(ChargeBearer.class)
    void mapToPurposeCode_success(ChargeBearer chargeBearer) {
        de.adorsys.psd2.model.ChargeBearer actual = mapper.mapToChargeBearer(chargeBearer);
        assertEquals(chargeBearer.name(), actual.name());
    }

    @ParameterizedTest
    @EnumSource(de.adorsys.psd2.model.ChargeBearer.class)
    void mapToPurposeCode_success(de.adorsys.psd2.model.ChargeBearer chargeBearer) {
        ChargeBearer actual = mapper.mapToChargeBearer(chargeBearer);
        assertEquals(chargeBearer.name(), actual.name());
    }
}
