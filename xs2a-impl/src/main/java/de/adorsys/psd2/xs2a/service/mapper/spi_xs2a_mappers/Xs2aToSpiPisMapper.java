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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiFrequencyCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPisDayOfExecution;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPisExecutionRule;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPisPurposeCode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface Xs2aToSpiPisMapper {

    SpiPaymentType mapToSpiPaymentType(PaymentType paymentType);

    SpiPisExecutionRule mapToSpiPisExecutionRule(PisExecutionRule executionRule);

    SpiFrequencyCode mapToSpiFrequencyCode(FrequencyCode frequencyCode);

    SpiPisDayOfExecution mapToSpiPisDayOfExecution(PisDayOfExecution dayOfExecution);

    SpiPisPurposeCode mapToSpiPisPurposeCode(PurposeCode purposeCode);
}
