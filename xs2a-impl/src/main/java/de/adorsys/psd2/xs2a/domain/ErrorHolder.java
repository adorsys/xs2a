/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class ErrorHolder {
    private final List<String> messages;
    private final MessageErrorCode errorCode;

    private ErrorHolder(ErrorHolderBuilder builder) {
        this.messages = builder.messages;
        this.errorCode = builder.errorCode;
    }

    public String getMessage() {
        if (CollectionUtils.isEmpty(messages)) {
            return "";
        }
        return String.join(", ", messages);
    }

    public MessageErrorCode getErrorCode() {
        return errorCode;
    }

    public static ErrorHolderBuilder builder(MessageErrorCode errorCode) {
        return new ErrorHolderBuilder(errorCode);
    }

    public static class ErrorHolderBuilder {
        private List<String> messages;
        private MessageErrorCode errorCode;

        private ErrorHolderBuilder(MessageErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        public ErrorHolderBuilder messages(List<String> messages) {
            this.messages = messages;
            return this;
        }

        public ErrorHolder build() {
            return new ErrorHolder(this);
        }
    }
}
