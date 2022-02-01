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

package de.adorsys.psd2.xs2a.core.domain;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ErrorHolder {
    private final List<TppMessageInformation> tppMessageInformationList;
    private final ErrorType errorType;

    private ErrorHolder(ErrorHolderBuilder builder) {
        this.tppMessageInformationList = builder.tppMessageInformationList;
        this.errorType = builder.errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public List<TppMessageInformation> getTppMessageInformationList() {
        return tppMessageInformationList;
    }

    public static ErrorHolderBuilder builder(ErrorType errorType) {
        return new ErrorHolderBuilder(errorType);
    }

    public static class ErrorHolderBuilder {
        private List<TppMessageInformation> tppMessageInformationList = new ArrayList<>();
        private ErrorType errorType;

        private ErrorHolderBuilder(ErrorType errorType) {
            this.errorType = errorType;
        }

        public ErrorHolderBuilder tppMessages(TppMessageInformation... tppMessages) {
            this.tppMessageInformationList = Arrays.asList(tppMessages);
            return this;
        }

        public ErrorHolder build() {
            return new ErrorHolder(this);
        }
    }

    @Override
    public String toString() {
        return CollectionUtils.isEmpty(tppMessageInformationList)
                   ? Optional.ofNullable(errorType)
                         .map(ErrorType::name)
                         .orElse("")
                   : tppMessageInformationList.stream()
                         .map(t -> t.getMessageErrorCode().getName())
                         .collect(Collectors.joining(", "));
    }

    public Optional<MessageErrorCode> getFirstErrorCode() {
        TppMessageInformation first = tppMessageInformationList.get(0);
        if (first == null) {
            return Optional.empty();
        }

        if (first.getMessageErrorCode() == null) {
            return Optional.empty();
        }

        return Optional.of(first.getMessageErrorCode());
    }

}
