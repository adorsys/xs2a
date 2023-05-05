/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiTransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Xs2aToSpiTransactionMapperTest {
    private final Xs2aToSpiTransactionMapper mapper = new Xs2aToSpiTransactionMapperImpl();

    @Test
    void mapToSpiTransactionStatus_null() {
        //When
        SpiTransactionStatus actual = mapper.mapToSpiTransactionStatus(null);

        //Then
        assertNull(actual);
    }

    @ParameterizedTest
    @EnumSource(TransactionStatus.class)
    void mapToSpiTransactionStatus(TransactionStatus transactionStatus) {
        //When
        SpiTransactionStatus actual = mapper.mapToSpiTransactionStatus(transactionStatus);

        //Then
        assertEquals(transactionStatus.name(), actual.name());
    }
}
