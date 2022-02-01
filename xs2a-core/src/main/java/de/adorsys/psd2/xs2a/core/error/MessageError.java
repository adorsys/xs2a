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

package de.adorsys.psd2.xs2a.core.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class MessageError {
    @JsonUnwrapped
    private Set<TppMessageInformation> tppMessages = new HashSet<>();
    private ErrorType errorType;

    public MessageError(ErrorType errorType, TppMessageInformation... tppMessageInformation) {
        this.errorType = errorType;
        fillTppMessage(tppMessageInformation);
    }

    public void addTppMessage(TppMessageInformation tppMessageInformation) {
        this.tppMessages.add(tppMessageInformation);
    }

    public MessageError(ErrorHolder errorHolder) {
        this.tppMessages.addAll(errorHolder.getTppMessageInformationList());
        this.errorType = errorHolder.getErrorType();
    }

    @JsonIgnore
    public TppMessageInformation getTppMessage() {
        return tppMessages.iterator().next();
    }

    private void fillTppMessage(TppMessageInformation... tppMessages) {
        if (isNotEmpty(tppMessages)) {
            this.tppMessages.addAll(Arrays.stream(tppMessages)
                                        .collect(Collectors.toSet()));
        }
    }

    private boolean isNotEmpty(TppMessageInformation... tppMessages) {
        return tppMessages != null && tppMessages.length >= 1;
    }

    @Override
    public String toString() {
        return CollectionUtils.isEmpty(tppMessages)
                   ? Optional.ofNullable(errorType)
                         .map(ErrorType::name)
                         .orElse("")
                   : tppMessages.stream()
                         .map(info -> info.getMessageErrorCode().getName())
                         .collect(Collectors.joining(", "));
    }
}
