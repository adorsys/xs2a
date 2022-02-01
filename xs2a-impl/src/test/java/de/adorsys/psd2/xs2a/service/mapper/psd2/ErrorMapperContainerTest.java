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

package de.adorsys.psd2.xs2a.service.mapper.psd2;

import de.adorsys.psd2.model.Error400NGPIS;
import de.adorsys.psd2.model.TppMessage400PIS;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.mapper.psd2.pis.PIS400ErrorMapper;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorMapperContainerTest {
    private static final String FORMAT_ERROR = "FORMAT_ERROR";

    @Mock
    private MessageService messageService;

    @Test
    void getErrorBody_pis400ErrorMapper() {
        // Given
        PIS400ErrorMapper pis400ErrorMapper = new PIS400ErrorMapper();
        pis400ErrorMapper.messageService = messageService;
        when(messageService.getMessage(any())).thenReturn(FORMAT_ERROR);

        ErrorMapperContainer errorMapperContainer = new ErrorMapperContainer(pis400ErrorMapper, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null, null,
                                                                             null, null);

        errorMapperContainer.fillErrorMapperContainer();

        MessageError messageError = new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));

        // When
        ErrorMapperContainer.ErrorBody actual = errorMapperContainer.getErrorBody(messageError);
        ErrorMapperContainer.ErrorBody expected = getErrorBody();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    private ErrorMapperContainer.ErrorBody getErrorBody() {
        Error400NGPIS error400NGPIS = new Error400NGPIS();
        TppMessage400PIS tppMessage = new TppMessage400PIS();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setCode(FORMAT_ERROR);
        tppMessage.setText(FORMAT_ERROR);
        error400NGPIS.setTppMessages(Collections.singletonList(tppMessage));
        return new ErrorMapperContainer.ErrorBody(error400NGPIS, HttpStatus.BAD_REQUEST);
    }
}
