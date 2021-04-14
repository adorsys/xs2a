/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Value
public class Xs2aResponse<T> {
    /**
     * Business object that is returned in scope of request
     */
    T payload;

    /**
     * Optional messages that can be returned to explain an error in details. XS2A Service will use it to
     * provide the error explanation to TPP
     */
    @NotNull
    List<TppMessage> errors = new ArrayList<>();

    public boolean hasError() {
        return !errors.isEmpty() || payload == null;
    }

    public boolean isSuccessful() {
        return errors.isEmpty()  && payload != null;
    }

    public static <T> Xs2aResponse.Xs2aResponseBuilder<T> builder() {
        return new Xs2aResponse.Xs2aResponseBuilder<>();
    }

    private Xs2aResponse(Xs2aResponse.Xs2aResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.errors.addAll(builder.errors);
    }

    public static class Xs2aResponseBuilder<T> {
        private T payload;
        private List<TppMessage> errors = new ArrayList<>();

        private Xs2aResponseBuilder() {
        }

        public Xs2aResponse.Xs2aResponseBuilder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public Xs2aResponse.Xs2aResponseBuilder<T> error(@NotNull TppMessage error) {
            this.errors.add(error);
            return this;
        }

        public Xs2aResponse.Xs2aResponseBuilder<T> error(List<TppMessage> errors) {
            if (CollectionUtils.isNotEmpty(errors)) {
                this.errors.addAll(errors);
            }
            return this;
        }

        public Xs2aResponse<T> build() {

            if (payload == null && CollectionUtils.isEmpty(errors)) {
                this.error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR));
            }

            return new Xs2aResponse<>(this);
        }
    }
}
