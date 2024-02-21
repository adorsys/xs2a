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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiMessageCategory;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiTppMessageInformation;
import de.adorsys.psd2.xs2a.web.mapper.ScaMethodsMapper;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {ScaMethodsMapper.class})
public interface SpiToXs2aTppMessageInformationMapper {

    Set<TppMessageInformation> toTppMessageInformationSet(Set<SpiTppMessageInformation> spiTppMessageInformationSet);

    List<TppMessageInformation> toTppMessageInformationList(Set<SpiTppMessageInformation> spiTppMessageInformationSet);

    MessageCategory mapToMessageCategory(SpiMessageCategory spiMessageCategory);

    MessageErrorCode mapToMessageErrorCode(SpiMessageErrorCode spiMessageErrorCode);

    default TppMessageInformation mapToTppMessage(SpiTppMessageInformation spiTppMessage) {
        if (spiTppMessage == null) {
            return null;
        }

        MessageCategory category = mapToMessageCategory(spiTppMessage.getCategory());
        MessageErrorCode errorCode = mapToMessageErrorCode(spiTppMessage.getMessageErrorCode());
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(category,
                                                                                errorCode,
                                                                                spiTppMessage.getPath(),
                                                                                spiTppMessage.getTextParameters());
        tppMessageInformation.setText(spiTppMessage.getText());
        return tppMessageInformation;
    }
}
