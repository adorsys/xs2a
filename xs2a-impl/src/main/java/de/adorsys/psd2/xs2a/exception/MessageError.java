/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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

    // TODO task: add logic to resolve resulting MessageError https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/211
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
                         .map(info -> StringUtils.defaultIfBlank(info.getText(), info.getMessageErrorCode().getName()))
                         .collect(Collectors.joining(", "));
    }
}
