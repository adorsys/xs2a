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

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
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
}
