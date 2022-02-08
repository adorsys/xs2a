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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.RemittanceInformationStructured;
import de.adorsys.psd2.model.RemittanceInformationStructuredArray;
import de.adorsys.psd2.model.RemittanceInformationStructuredMax140;
import de.adorsys.psd2.model.RemittanceInformationUnstructuredArray;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RemittanceMapper {
    RemittanceInformationStructured mapToRemittanceInformationStructured(Remittance remittance);

    Remittance mapToRemittance(RemittanceInformationStructured remittanceInformationStructured);

    Remittance mapToRemittance(RemittanceInformationStructuredMax140 remittanceInformationStructured);

    List<String> mapToRemittanceUnstructuredList(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray);

    Remittance mapToRemittance(SpiRemittance spiRemittance);

    List<Remittance> mapToRemittanceArray(List<SpiRemittance> spiRemittanceArray);

    List<Remittance> mapToRemittanceArray(RemittanceInformationStructuredArray remittanceStructuredArray);

    SpiRemittance mapToSpiRemittance(Remittance remittance);

    List<SpiRemittance> mapToSpiRemittanceArray(List<Remittance> remittanceArray);
}
