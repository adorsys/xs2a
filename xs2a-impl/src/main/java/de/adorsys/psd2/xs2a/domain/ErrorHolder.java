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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorHolder {
    private final List<TppMessageInformation> tppMessageInformationList;
    private final ErrorType errorType;

    private ErrorHolder(ErrorHolderBuilder builder) {
        this.tppMessageInformationList = builder.tppMessageInformationList;
        this.errorType = builder.errorType;
    }

    // TODO: Remove the method in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392, use TppMessageInformation instead
    @Deprecated
    public String getMessage() {
        return tppMessageInformationList.stream()
                   .map(TppMessageInformation::getText)
                   .collect(Collectors.joining(", "));
    }

    // TODO: Remove the method in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392, use TppMessageInformation instead
    @Deprecated
    public MessageErrorCode getErrorCode() {
        return getFirstTppMessage().getMessageErrorCode();
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

    // TODO: Remove the method in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392, use builder with error type instead
    @Deprecated
    public static ErrorHolderBuilder builder(MessageErrorCode errorCode) {
        return new ErrorHolderBuilder(errorCode);
    }

    private TppMessageInformation getFirstTppMessage() {
        return tppMessageInformationList.get(0);
    }

    public static class ErrorHolderBuilder {
        private List<TppMessageInformation> tppMessageInformationList = new ArrayList<>();
        private ErrorType errorType;
        private List<String> messages;
        private MessageErrorCode errorCode;

        private ErrorHolderBuilder(ErrorType errorType) {
            this.errorType = errorType;
        }

        private ErrorHolderBuilder(MessageErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        // TODO: Remove the method in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392, use TppMessageInformation instead
        @Deprecated
        public ErrorHolderBuilder messages(List<String> messages) {
            this.messages = messages;
            return this;
        }

        public ErrorHolderBuilder tppMessages(TppMessageInformation... tppMessages) {
            this.tppMessageInformationList = Arrays.asList(tppMessages);
            return this;
        }

        // TODO: Remove the method in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392, use builder instead
        @Deprecated
        public ErrorHolderBuilder errorType(ErrorType errorType) {
            this.errorType = errorType;
            return this;
        }

        public ErrorHolder build() {
            if (tppMessageInformationList.isEmpty()) {
                tppMessageInformationList = generateTppMessages(messages);
            }

            return new ErrorHolder(this);
        }

        // TODO: Remove the method in scope of https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392 when it won't be possible to create holder without tpp messages
        @Deprecated
        private List<TppMessageInformation> generateTppMessages(List<String> messages) {
            if (CollectionUtils.isEmpty(messages)) {
                return Collections.singletonList(TppMessageInformation.of(errorCode));
            }

            return messages.stream()
                       .map(m -> TppMessageInformation.of(errorCode, m))
                       .collect(Collectors.toList());
        }
    }
}
