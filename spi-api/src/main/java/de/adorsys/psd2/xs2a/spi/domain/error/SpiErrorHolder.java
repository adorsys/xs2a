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

package de.adorsys.psd2.xs2a.spi.domain.error;

import de.adorsys.psd2.xs2a.spi.domain.response.SpiTppMessageInformation;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpiErrorHolder {
    private final List<SpiTppMessageInformation> tppMessageInformationList;
    private final SpiErrorType errorType;

    private SpiErrorHolder(ErrorHolderBuilder builder) {
        this.tppMessageInformationList = builder.tppMessageInformationList;
        this.errorType = builder.errorType;
    }

    public SpiErrorType getErrorType() {
        return errorType;
    }

    public List<SpiTppMessageInformation> getTppMessageInformationList() {
        return tppMessageInformationList;
    }

    public static ErrorHolderBuilder builder(SpiErrorType errorType) {
        return new ErrorHolderBuilder(errorType);
    }

    public static class ErrorHolderBuilder {
        private List<SpiTppMessageInformation> tppMessageInformationList = new ArrayList<>();
        private SpiErrorType errorType;

        private ErrorHolderBuilder(SpiErrorType errorType) {
            this.errorType = errorType;
        }

        public ErrorHolderBuilder tppMessages(SpiTppMessageInformation... tppMessages) {
            this.tppMessageInformationList = Arrays.asList(tppMessages);
            return this;
        }

        public SpiErrorHolder build() {
            return new SpiErrorHolder(this);
        }
    }

    @Override
    public String toString() {
        return CollectionUtils.isEmpty(tppMessageInformationList)
                   ? Optional.ofNullable(errorType)
                         .map(SpiErrorType::name)
                         .orElse("")
                   : tppMessageInformationList.stream()
                         .map(t -> t.getMessageErrorCode().getName())
                         .collect(Collectors.joining(", "));
    }

    public Optional<SpiMessageErrorCode> getFirstErrorCode() {
        SpiTppMessageInformation first = tppMessageInformationList.get(0);
        if (first == null) {
            return Optional.empty();
        }

        if (first.getMessageErrorCode() == null) {
            return Optional.empty();
        }

        return Optional.of(first.getMessageErrorCode());
    }

}
